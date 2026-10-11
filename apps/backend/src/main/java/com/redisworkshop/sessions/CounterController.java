package com.redisworkshop.sessions;

import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sessions/counters")
public class CounterController {

    private static final long MAX = 100;

    private final StringRedisTemplate redis;

    public CounterController(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @GetMapping
    Map<String, Long> all() {
        return Map.of(
                "public", read("counter:public"),
                "authenticated", read("counter:authenticated"),
                "admin", read("counter:admin"));
    }

    @PostMapping("/public/increment")
    Map<String, Long> publicIncrement() {
        return Map.of("value", increment("counter:public"));
    }

    @PostMapping("/authenticated/increment")
    ResponseEntity<Map<String, Long>> authenticatedIncrement(
            @RequestAttribute(name = SessionAuthFilter.ATTRIBUTE, required = false) SessionUser user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(Map.of("value", increment("counter:authenticated")));
    }

    @PostMapping("/admin/increment")
    ResponseEntity<Map<String, Long>> adminIncrement(
            @RequestAttribute(name = SessionAuthFilter.ATTRIBUTE, required = false) SessionUser user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!"ADMIN".equals(user.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(Map.of("value", increment("counter:admin")));
    }

    private long increment(String key) {
        Long value = redis.opsForValue().increment(key);
        long current = value == null ? 0 : value;
        if (current > MAX) {
            redis.opsForValue().set(key, "0");
            current = 0;
        }
        return current;
    }

    private long read(String key) {
        String value = redis.opsForValue().get(key);
        return value == null ? 0 : Long.parseLong(value);
    }
}