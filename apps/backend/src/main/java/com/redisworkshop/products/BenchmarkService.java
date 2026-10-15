package com.redisworkshop.products;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

@Service
public class BenchmarkService {

    /** Rondas medidas; se reporta la MEDIANA (más robusta que una sola pasada ruidosa). */
    private static final int ROUNDS = 3;

    /** Términos RECURRENTES: la misma búsqueda se repite → la caché acierta. */
    private static final List<String> SEARCH_TERMS =
            List.of("widget", "gadget", "gizmo", "doohickey", "thingamajig");

    private final ProductService productService;

    public BenchmarkService(ProductService productService) {
        this.productService = productService;
    }

    public BenchmarkResult run(int iterations, boolean cached, int concurrency) {
        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        try {
            // Warmup: calienta la JVM (JIT) y se descarta del cómputo.
            drain(submit(pool, Math.min(iterations, 100), cached));

            List<BenchmarkResult> rounds = new ArrayList<>(ROUNDS);
            for (int r = 0; r < ROUNDS; r++) {
                long wallStart = System.nanoTime();
                List<Future<Long>> futures = submit(pool, iterations, cached);
                long wallNanos = System.nanoTime() - wallStart;
                rounds.add(summarize(iterations, concurrency, cached, wallNanos, collect(futures)));
            }

            return median(rounds);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Benchmark interrumpido", ex);
        } finally {
            pool.shutdownNow();
        }
    }

    /** Mediana por métrica entre las rondas (resultado "típico", no un pico). */
    private BenchmarkResult median(List<BenchmarkResult> rounds) {
        BenchmarkResult first = rounds.get(0);
        return new BenchmarkResult(
                first.mode(),
                first.iterations(),
                first.concurrency(),
                ROUNDS,
                first.count(),
                median(rounds.stream().mapToDouble(r -> r.totalMs()).toArray()),
                median(rounds.stream().mapToDouble(r -> r.avgMs()).toArray()),
                median(rounds.stream().mapToDouble(r -> r.minMs()).toArray()),
                median(rounds.stream().mapToDouble(r -> r.maxMs()).toArray()),
                median(rounds.stream().mapToDouble(r -> r.p95Ms()).toArray()),
                median(rounds.stream().mapToDouble(r -> r.throughput()).toArray()));
    }

    private double median(double[] values) {
        double[] sorted = Arrays.copyOf(values, values.length);
        Arrays.sort(sorted);
        int n = sorted.length;
        return (n % 2 == 1) ? sorted[n / 2] : (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0;
    }

    private List<Future<Long>> submit(ExecutorService pool, int times, boolean cached)
            throws InterruptedException {
        List<Callable<Long>> tasks = new ArrayList<>(times);
        for (int i = 0; i < times; i++) {
            tasks.add(() -> timedRead(cached));
        }
        return pool.invokeAll(tasks);
    }

    private long timedRead(boolean cached) {
        String q = SEARCH_TERMS.get(ThreadLocalRandom.current().nextInt(SEARCH_TERMS.size()));
        long start = System.nanoTime();
        if (cached) {
            productService.search(q);
        } else {
            productService.searchNoCache(q);
        }
        return System.nanoTime() - start;
    }

    private void drain(List<Future<Long>> futures) {
        for (Future<Long> future : futures) {
            try {
                future.get();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (ExecutionException ignored) {
                // descartamos el fallo del warmup
            }
        }
    }

    private long[] collect(List<Future<Long>> futures) {
        long[] latencies = new long[futures.size()];
        for (int i = 0; i < futures.size(); i++) {
            try {
                latencies[i] = futures.get(i).get();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                latencies[i] = -1;
            } catch (ExecutionException ex) {
                latencies[i] = -1;
            }
        }
        return latencies;
    }

    private BenchmarkResult summarize(int iterations, int concurrency, boolean cached, long wallNanos,
            long[] latencies) {
        long[] values = Arrays.stream(latencies).filter((v) -> v >= 0).sorted().toArray();
        String mode = cached ? "cache" : "nocache";
        int count = values.length;

        if (count == 0) {
            return new BenchmarkResult(mode, iterations, concurrency, 1, 0, 0, 0, 0, 0, 0, 0);
        }

        double avg = Arrays.stream(values).average().orElse(0) / 1_000_000.0;
        double min = values[0] / 1_000_000.0;
        double max = values[count - 1] / 1_000_000.0;
        int p95Index = Math.max(0, (int) Math.ceil(0.95 * count) - 1);
        double p95 = values[p95Index] / 1_000_000.0;
        double totalMs = wallNanos / 1_000_000.0;
        double throughput = count / (wallNanos / 1_000_000_000.0);

        return new BenchmarkResult(mode, iterations, concurrency, 1, count, totalMs, avg, min, max, p95,
                throughput);
    }
}