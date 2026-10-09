package com.redisworkshop.ratelimit;

import java.util.List;

public record RateLimitSimulation(
        int requests,
        int capacity,
        double rate,
        int queue,
        int passed,
        int queued,
        int dropped,
        double drainSeconds,
        //La lista a modo de grafica x/y segundos, peticiones que quedan
        List<Point> timeline) {

    public record Point(double seconds, int remaining) {
    }
}