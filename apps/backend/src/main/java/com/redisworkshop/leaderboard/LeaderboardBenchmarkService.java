package com.redisworkshop.leaderboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class LeaderboardBenchmarkService {

    private final LeaderboardService redis;
    private final PlayerScoreRepository repository;

    public LeaderboardBenchmarkService(LeaderboardService redis, PlayerScoreRepository repository) {
        this.redis = redis;
        this.repository = repository;
    }

        public LeaderboardBenchmarkResult run(int players, int concurrency, int durationSeconds) {
        seed(players);

        long redisSuccesses = load(concurrency, players, durationSeconds,
                (event) -> redis.incrementScore("player-" + event[0], event[1]));
        long postgresSuccesses = load(concurrency, players, durationSeconds,
                (event) -> repository.addPoints("player-" + event[0], event[1]));

        List<LeaderboardEntry> redisTop = redis.top(LeaderboardConstants.TOP);
        List<LeaderboardEntry> postgresTop = repository
                .findOrdered(PageRequest.of(0, LeaderboardConstants.TOP)).stream()
                .map((p) -> new LeaderboardEntry(p.getUsername(), p.getScore()))
                .toList();

        return new LeaderboardBenchmarkResult(players, concurrency, durationSeconds, redisSuccesses,
                postgresSuccesses, redisTop, postgresTop);
    }

    private long load(int concurrency, int players, int durationSeconds, Consumer<int[]> operation) {
        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        CountDownLatch start = new CountDownLatch(1);
        AtomicLong successes = new AtomicLong();
        long deadline = System.nanoTime() + durationSeconds * 1_000_000_000L;

        List<Future<?>> futures = new ArrayList<>(concurrency);
        for (int w = 0; w < concurrency; w++) {
            futures.add(pool.submit(() -> {
                start.await();
                ThreadLocalRandom random = ThreadLocalRandom.current();
                while (System.nanoTime() < deadline) {
                    int[] event = { random.nextInt(1, players + 1),
                            random.nextInt(1, LeaderboardConstants.SCORE_RANGE + 1) };
                    operation.accept(event);
                    successes.incrementAndGet();
                }
                return null;
            }));
        }

        start.countDown();
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException ignored) {
            }
        }
        pool.shutdownNow();

        return successes.get();
    }

    private void seed(int players) {
        Set<String> existing = new HashSet<>();
        for (PlayerScore player : repository.findAll()) {
            existing.add(player.getUsername());
        }

        List<PlayerScore> missing = new ArrayList<>();
        for (int i = 1; i <= players; i++) {
            String member = "player-" + i;
            if (!existing.contains(member)) {
                missing.add(new PlayerScore(member, 0));
            }
        }
        if (!missing.isEmpty()) {
            repository.saveAll(missing);
        }
    }
}