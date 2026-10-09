package com.redisworkshop.ratelimit;

public record RateLimitDecision(boolean allowed, long remaining, long retryAfterMs) {
}
