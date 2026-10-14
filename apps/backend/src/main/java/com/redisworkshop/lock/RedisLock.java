package com.redisworkshop.lock;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class RedisLock {
    //Este mini script en LUA hace que la liberacion del lock sea atómica.
    //Pa que? Para evitar condiciones de carrera.
    //Como? Primero comprueba que el lock es tuyo y solo en ese caso puedes borrar la llave.
    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] "
                    + "then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate redis;

    public RedisLock(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean tryAcquire(String owner) {
        Boolean acquired = redis.opsForValue().setIfAbsent(
                LockConstants.LOCK_KEY, owner, Duration.ofMillis(LockConstants.LOCK_TTL_MS));
        return Boolean.TRUE.equals(acquired);
    }

    public boolean release(String owner) {
        Long released = redis.execute(RELEASE_SCRIPT, List.of(LockConstants.LOCK_KEY), owner);
        return released != null && released > 0;
    }

    public Optional<String> holder() {
        return Optional.ofNullable(redis.opsForValue().get(LockConstants.LOCK_KEY));
    }

    public long ttlMillis() {
        Long millis = redis.getExpire(LockConstants.LOCK_KEY, TimeUnit.MILLISECONDS);
        return millis == null ? -2 : millis;
    }
}