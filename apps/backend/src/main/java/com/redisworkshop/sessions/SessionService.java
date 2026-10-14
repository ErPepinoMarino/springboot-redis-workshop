package com.redisworkshop.sessions;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private static final String PREFIX = "session:";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate redis;
    private final HashOperations<String, String, String> hash;
    private final Duration ttl;

    public SessionService(StringRedisTemplate redis, @Value("${sessions.ttl:30s}") Duration ttl) {
        this.redis = redis;
        this.hash = redis.opsForHash();
        this.ttl = ttl;
    }

    public String create(String username, String role) {
        String token = newToken();
        String key = PREFIX + token;
        hash.putAll(key, Map.of("username", username, "role", role));
        redis.expire(key, ttl);
        return token;
    }

    public Optional<SessionUser> find(String token) {
        Map<String, String> fields = hash.entries(PREFIX + token);
        if (fields.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new SessionUser(fields.get("username"), fields.get("role")));
    }

    public void delete(String token) {
        redis.delete(PREFIX + token);
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    public long ttlMillis(String token) {
        Long millis = redis.getExpire(PREFIX + token, TimeUnit.MILLISECONDS);
        //-2 significa clave no existente
        return millis == null ? -2 : millis;
    }
}