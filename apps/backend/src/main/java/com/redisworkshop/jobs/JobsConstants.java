package com.redisworkshop.jobs;

import java.time.Duration;

public final class JobsConstants {

    private JobsConstants() {}

    public static final String STREAM = "jobs";
    public static final String GROUP = "workers";
    public static final String JOB_KEY_PREFIX = "job:";
    public static final Duration JOB_TTL = Duration.ofSeconds(30);

    public static final long QUICK_MS = 2_000;
    public static final long SLOW_MS = 5_000;
    public static final long FAILING_MS = 3_000;
    public static final long CRASH_MS = 3_000;

    public static final long BLOCK_MS = 2_000;
    public static final long RECLAIM_IDLE_MS = 8_000;
    public static final int CONSUMERS = 3;
}