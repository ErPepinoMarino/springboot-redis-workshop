package com.redisworkshop.leaderboard;

import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
public class LeaderboardService {

    private static final String KEY = "leaderboard";

    private final StringRedisTemplate redis;

    public LeaderboardService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public double incrementScore(String member, double delta) {
        Double newScore = redis.opsForZSet().incrementScore(KEY, member, delta);
        return newScore == null ? delta : newScore;
    }

    public List<LeaderboardEntry> top(int n) {
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redis.opsForZSet().reverseRangeWithScores(KEY, 0, n - 1);
        if (tuples == null) {
            return List.of();
        }
        return tuples.stream()
                .map((tuple) -> new LeaderboardEntry(tuple.getValue(),
                        tuple.getScore() == null ? 0 : tuple.getScore()))
                .toList();
    }
}