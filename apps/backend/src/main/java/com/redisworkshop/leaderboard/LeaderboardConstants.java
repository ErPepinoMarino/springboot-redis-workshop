package com.redisworkshop.leaderboard;

public final class LeaderboardConstants {

    public static final String DEFAULT_PLAYERS = "1000";
    public static final String DEFAULT_CONCURRENCY = "10";
    public static final String DEFAULT_DURATION = "3";

    public static final int MAX_PLAYERS = 1_000_000;
    public static final int MAX_CONCURRENCY = 200;
    public static final int MAX_DURATION = 60;

    public static final int TOP = 50;
    public static final int SCORE_RANGE = 1000;

    private LeaderboardConstants() {
    }
}
