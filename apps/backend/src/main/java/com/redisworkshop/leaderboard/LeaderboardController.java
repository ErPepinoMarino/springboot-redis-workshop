package com.redisworkshop.leaderboard;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {

    private final LeaderboardBenchmarkService benchmarkService;

    public LeaderboardController(LeaderboardBenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }

    @PostMapping("/benchmark")
    LeaderboardBenchmarkResult benchmark(
            @RequestParam(defaultValue = LeaderboardConstants.DEFAULT_PLAYERS) int players,
            @RequestParam(defaultValue = LeaderboardConstants.DEFAULT_CONCURRENCY) int concurrency,
            @RequestParam(defaultValue = LeaderboardConstants.DEFAULT_DURATION) int duration) {
        int safePlayers = Math.max(1, Math.min(players, LeaderboardConstants.MAX_PLAYERS));
        int safeConcurrency = Math.max(1, Math.min(concurrency, LeaderboardConstants.MAX_CONCURRENCY));
        int safeDuration = Math.max(1, Math.min(duration, LeaderboardConstants.MAX_DURATION));
        return benchmarkService.run(safePlayers, safeConcurrency, safeDuration);
    }
}