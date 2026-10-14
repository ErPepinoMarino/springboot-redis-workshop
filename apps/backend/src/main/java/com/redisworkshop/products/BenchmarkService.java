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
import java.util.function.ToDoubleFunction;

import org.springframework.stereotype.Service;

@Service
public class BenchmarkService {

    /** Rondas medidas; se reporta la MEDIANA (más robusta que una sola pasada ruidosa). */
    private static final int ROUNDS = 3;

    private final ProductService productService;

    public BenchmarkService(ProductService productService) {
        this.productService = productService;
    }

    public BenchmarkResult run(int iterations, boolean cached, int concurrency) {
        List<Long> ids = productService.ids();
        if (ids.isEmpty()) {
            return new BenchmarkResult(cached ? "cache" : "nocache", iterations, concurrency, 0, 0, 0, 0,
                    0, 0, 0, 0);
        }

        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        try {
            // Warmup: calienta la JVM (JIT) y se descarta del cómputo.
            drain(submit(pool, ids, Math.min(iterations, 100), cached));

            List<BenchmarkResult> rounds = new ArrayList<>(ROUNDS);
            for (int r = 0; r < ROUNDS; r++) {
                long wallStart = System.nanoTime();
                List<Future<Long>> futures = submit(pool, ids, iterations, cached);
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

    /** Mediana por métrica entre las rondas (reporta el resultado "típico", no un pico). */
    private BenchmarkResult median(List<BenchmarkResult> rounds) {
        BenchmarkResult first = rounds.get(0);
        return new BenchmarkResult(
                first.mode(),
                first.iterations(),
                first.concurrency(),
                ROUNDS,
                first.count(),
                median(rounds, BenchmarkResult::totalMs),
                median(rounds, BenchmarkResult::avgMs),
                median(rounds, BenchmarkResult::minMs),
                median(rounds, BenchmarkResult::maxMs),
                median(rounds, BenchmarkResult::p95Ms),
                median(rounds, BenchmarkResult::throughput));
    }

    private double median(List<BenchmarkResult> rounds, ToDoubleFunction<BenchmarkResult> metric) {
        double[] values = rounds.stream().mapToDouble(metric).sorted().toArray();
        int n = values.length;
        return (n % 2 == 1) ? values[n / 2] : (values[n / 2 - 1] + values[n / 2]) / 2.0;
    }

    private List<Future<Long>> submit(ExecutorService pool, List<Long> ids, int times, boolean cached)
            throws InterruptedException {
        List<Callable<Long>> tasks = new ArrayList<>(times);
        for (int i = 0; i < times; i++) {
            tasks.add(() -> timedRead(randomId(ids), cached));
        }
        return pool.invokeAll(tasks);
    }

    private long randomId(List<Long> ids) {
        return ids.get(ThreadLocalRandom.current().nextInt(ids.size()));
    }

    private long timedRead(long id, boolean cached) {
        long start = System.nanoTime();
        if (cached) {
            productService.findById(id);
        } else {
            productService.findByIdNoCache(id);
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
