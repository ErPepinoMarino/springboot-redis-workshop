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

    // --- Pub/Sub + SSE de la simulación ---
    public static final String CHANNEL = "ratelimit";
    public static final String SERVED_PLAIN_KEY = "ratelimit:served:plain";
    public static final String SERVED_BUCKET_KEY = "ratelimit:served:bucket";
    public static final long TICK_MS = 250;
    public static final int INITIAL_ZERO_TICKS = 2; //Para que no empiece a saco.

    private RateLimitConstants() {
    }
}