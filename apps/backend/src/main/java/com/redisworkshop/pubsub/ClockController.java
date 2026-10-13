package com.redisworkshop.pubsub;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.redisworkshop.sse.SseRegistry;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/clock")
public class ClockController {

    private final SseRegistry registry;

    public ClockController(SseRegistry registry) {
        this.registry = registry;
    }

    @GetMapping
    public Map<String, String> now() {
        return Map.of("time", Instant.now().toString());
    }

     @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter stream() {
        return registry.register(PubSubConstants.CLOCK_CHANNEL);
    }
}