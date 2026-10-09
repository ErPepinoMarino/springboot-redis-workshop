package com.redisworkshop.ratelimit;

import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

@Component
public class TokenBucketLimiter {

    private static final String KEY_PREFIX = "ipCliente:";

    private final StringRedisTemplate redis;
    private final DefaultRedisScript<List> script;

    public TokenBucketLimiter(StringRedisTemplate redis) {
        this.redis = redis;
        this.script = new DefaultRedisScript<>();
        this.script.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("scripts/token_bucket.lua")));
        this.script.setResultType(List.class);
    }

    public RateLimitDecision tryAcquire(String clientId, int capacity, double rate, int ttlSeconds) {
        List<?> result = redis.execute(script, List.of(KEY_PREFIX + clientId),
                String.valueOf(capacity), String.valueOf(rate), String.valueOf(ttlSeconds));

        long allowed = ((Number) result.get(0)).longValue();
        long remaining = ((Number) result.get(1)).longValue();
        long retryAfterMs = ((Number) result.get(2)).longValue();

        return new RateLimitDecision(allowed == 1, remaining, retryAfterMs);
    }
}