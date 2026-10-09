package com.redisworkshop.ratelimit;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ratelimit")
public class RateLimitController {

    private final RateLimitSimulator simulator;

    public RateLimitController(RateLimitSimulator simulator) {
        this.simulator = simulator;
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
}