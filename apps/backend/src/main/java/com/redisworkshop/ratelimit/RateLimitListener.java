package com.redisworkshop.ratelimit;

import java.nio.charset.StandardCharsets;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.redisworkshop.sse.SseRegistry;

@Component
public class RateLimitListener implements MessageListener {

    private final SseRegistry registry;

    public RateLimitListener(SseRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        registry.fanOut(RateLimitConstants.CHANNEL, new String(message.getBody(), StandardCharsets.UTF_8));
    }
}