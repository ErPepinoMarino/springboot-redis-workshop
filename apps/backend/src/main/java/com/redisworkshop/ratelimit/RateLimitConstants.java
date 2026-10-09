package com.redisworkshop.ratelimit;

public final class RateLimitConstants {

    public static final String DEFAULT_REQUESTS = "1000";
    public static final String DEFAULT_CAPACITY = "20";
    public static final String DEFAULT_RATE = "20";
    public static final String DEFAULT_QUEUE = "180";

    public static final int MAX_REQUESTS = 1_000_000;
    public static final int MAX_CAPACITY = 100_000;
    public static final int MAX_RATE = 100_000;
    public static final int MAX_QUEUE = 1_000_000;

    private RateLimitConstants() {
    }
}