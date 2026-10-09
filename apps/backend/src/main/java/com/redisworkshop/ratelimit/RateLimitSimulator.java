package com.redisworkshop.ratelimit;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class RateLimitSimulator {

    private static final int TIMELINE_POINTS = 50;

    public RateLimitSimulation simulate(int requests, int capacity, double rate, int queue) {
        int passed = Math.min(requests, Math.max(0, capacity));
        int overflow = requests - passed;
        int queued = Math.min(overflow, Math.max(0, queue));
        int dropped = overflow - queued;

        double drainSeconds = rate > 0 ? queued / rate : 0;

        return new RateLimitSimulation(requests, capacity, rate, queue, passed, queued, dropped,
                drainSeconds, buildTimeline(queued, rate, drainSeconds));
    }

    private List<RateLimitSimulation.Point> buildTimeline(int queued, double rate, double drainSeconds) {
        List<RateLimitSimulation.Point> points = new ArrayList<>();

        if (queued == 0 || rate <= 0) {
            points.add(new RateLimitSimulation.Point(0, queued));
            return points;
        }

        for (int i = 0; i <= TIMELINE_POINTS; i++) {
            double seconds = drainSeconds * i / TIMELINE_POINTS;
            int remaining = (int) Math.max(0, Math.round(queued - rate * seconds));
            points.add(new RateLimitSimulation.Point(seconds, remaining));
        }
        return points;
    }
}