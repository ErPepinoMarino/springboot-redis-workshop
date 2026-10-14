package com.redisworkshop.lock;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LockService {

    private final RedisLock lock;
    private final StringRedisTemplate redis;

    public LockService(RedisLock lock, StringRedisTemplate redis) {
        this.lock = lock;
        this.redis = redis;
    }

    public LockResponse acquire(String client) {
        boolean ok = lock.tryAcquire(client);
        return new LockResponse(ok, lock.holder().orElse(null), counter());
    }

    public LockResponse release(String client) {
        boolean ok = lock.release(client);
        return new LockResponse(ok, lock.holder().orElse(null), counter());
    }

    public LockResponse increment(String client) {
        if (lock.holder().filter(client::equals).isEmpty()) {
            return new LockResponse(false, lock.holder().orElse(null), counter());
        }
        long value = redis.opsForValue().increment(LockConstants.COUNTER_KEY);
        return new LockResponse(true, client, value);
    }

    private long counter() {
        String value = redis.opsForValue().get(LockConstants.COUNTER_KEY);
        return value == null ? 0 : Long.parseLong(value);
    }
}