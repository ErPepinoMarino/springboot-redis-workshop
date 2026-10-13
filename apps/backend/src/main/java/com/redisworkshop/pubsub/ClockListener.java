package com.redisworkshop.pubsub;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.redisworkshop.sse.SseRegistry;

import java.nio.charset.StandardCharsets;

@Component
public class ClockListener implements MessageListener {

    private final SseRegistry registry;

    public ClockListener(SseRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String time = new String(message.getBody(), StandardCharsets.UTF_8);
        registry.fanOut(PubSubConstants.CLOCK_CHANNEL, time);
    }
}