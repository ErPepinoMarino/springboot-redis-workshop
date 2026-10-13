package com.redisworkshop.pubsub;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ClockPublisher {

    private final StringRedisTemplate redis;

    public ClockPublisher(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Scheduled(fixedRate = PubSubConstants.CLOCK_INTERVAL_MS)
    public void tick() {
        redis.convertAndSend(PubSubConstants.CLOCK_CHANNEL, Instant.now().toString());
    }
}