package com.redisworkshop.ratelimit;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.redisworkshop.sse.SseRegistry;

@RestController
@RequestMapping("/ratelimit")
public class RateLimitController {

    private final RateLimitSimulator simulator;
    private final RateLimitSimulationService simulationService;
    private final SseRegistry registry;

    public RateLimitController(RateLimitSimulator simulator,
                               RateLimitSimulationService simulationService,
                               SseRegistry registry) {
        this.simulator = simulator;
        this.simulationService = simulationService;
        this.registry = registry;
    }

    @GetMapping("/ping")
    Map<String, String> ping() {
        return Map.of("message", "pong");
    }

    @GetMapping("/simulate")
    RateLimitSimulation simulate(
            @RequestParam(defaultValue = RateLimitConstants.DEFAULT_REQUESTS) int requests,
            @RequestParam(defaultValue = RateLimitConstants.DEFAULT_CAPACITY) int capacity,
            @RequestParam(defaultValue = RateLimitConstants.DEFAULT_RATE) double rate,
            @RequestParam(defaultValue = RateLimitConstants.DEFAULT_QUEUE) int queue) {
        return simulator.simulate(
                Math.max(0, Math.min(requests, RateLimitConstants.MAX_REQUESTS)),
                Math.max(0, Math.min(capacity, RateLimitConstants.MAX_CAPACITY)),
                Math.max(0, Math.min(rate, RateLimitConstants.MAX_RATE)),
                Math.max(0, Math.min(queue, RateLimitConstants.MAX_QUEUE)));
    }

    @PostMapping("/simulate/stream")
    void startStream(
            @RequestParam(defaultValue = RateLimitConstants.DEFAULT_REQUESTS) int requests,
            @RequestParam(defaultValue = RateLimitConstants.DEFAULT_CAPACITY) int capacity,
            @RequestParam(defaultValue = RateLimitConstants.DEFAULT_RATE) double rate,
            @RequestParam(defaultValue = RateLimitConstants.DEFAULT_QUEUE) int queue) {
        simulationService.start(
                Math.max(0, Math.min(requests, RateLimitConstants.MAX_REQUESTS)),
                Math.max(0, Math.min(capacity, RateLimitConstants.MAX_CAPACITY)),
                Math.max(0, Math.min(rate, RateLimitConstants.MAX_RATE)),
                Math.max(0, Math.min(queue, RateLimitConstants.MAX_QUEUE)));
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter stream() {
        return registry.register(RateLimitConstants.CHANNEL);
    }
}