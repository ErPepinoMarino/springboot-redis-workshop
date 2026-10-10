package com.redisworkshop.leaderboard;

import java.util.List;

public record LeaderboardBenchmarkResult(
        int players,
        int concurrency,
        int durationSeconds,
        long redisSuccesses,
        long postgresSuccesses,
        List<LeaderboardEntry> redisTop,
        List<LeaderboardEntry> postgresTop) {
}