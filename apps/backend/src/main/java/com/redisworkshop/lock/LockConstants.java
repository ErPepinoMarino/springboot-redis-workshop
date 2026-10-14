package com.redisworkshop.lock;

public final class LockConstants {

    public static final String LOCK_KEY = "lock:counter";
    public static final String COUNTER_KEY = "lock:counter:value";
    public static final long LOCK_TTL_MS = 10_000;
    public static final long TICK_MS = 250;

    private LockConstants() {
    }
}