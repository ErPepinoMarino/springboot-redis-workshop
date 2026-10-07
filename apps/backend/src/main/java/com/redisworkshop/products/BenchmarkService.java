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

    private final ProductService productService;

    public BenchmarkService(ProductService productService) {
        this.productService = productService;
    }

    public BenchmarkResult run(int iterations, boolean cached, int concurrency) {
        List<Long> ids = productService.ids();
        if (ids.isEmpty()) {
            return new BenchmarkResult(cached ? "cache" : "nocache", iterations, concurrency, 0, 0, 0, 0,
                    0, 0, 0);
        }

        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        try {
            drain(submit(pool, ids, Math.min(iterations, 100), cached));

            long wallStart = System.nanoTime();
            List<Future<Long>> futures = submit(pool, ids, iterations, cached);
            long wallNanos = System.nanoTime() - wallStart;

            return summarize(iterations, concurrency, cached, wallNanos, collect(futures));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Benchmark interrumpido", ex);
        } finally {
            pool.shutdownNow();
        }
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
            return new BenchmarkResult(mode, iterations, concurrency, 0, 0, 0, 0, 0, 0, 0);
        }

        double avg = Arrays.stream(values).average().orElse(0) / 1_000_000.0;
        double min = values[0] / 1_000_000.0;
        double max = values[count - 1] / 1_000_000.0;
        int p95Index = Math.max(0, (int) Math.ceil(0.95 * count) - 1);
        double p95 = values[p95Index] / 1_000_000.0;
        double totalMs = wallNanos / 1_000_000.0;
        double throughput = count / (wallNanos / 1_000_000_000.0);

        return new BenchmarkResult(mode, iterations, concurrency, count, totalMs, avg, min, max, p95,
                throughput);
    }
}