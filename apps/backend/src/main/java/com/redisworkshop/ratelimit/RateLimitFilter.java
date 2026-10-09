package com.redisworkshop.ratelimit;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final TokenBucketLimiter limiter;
    private final int capacity;
    private final double rate;
    private final int ttlSeconds;

    public RateLimitFilter(TokenBucketLimiter limiter,
                           @Value("${app.ratelimit.capacity:20}") int capacity,
                           @Value("${app.ratelimit.rate:10}") double rate,
                           @Value("${app.ratelimit.ttl-seconds:30}") int ttlSeconds) {
        this.limiter = limiter;
        this.capacity = capacity;
        this.rate = rate;
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/ratelimit/") || path.startsWith("/ratelimit/simulate");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientId = request.getRemoteAddr();
        RateLimitDecision decision = limiter.tryAcquire(clientId, capacity, rate, ttlSeconds);

        response.setHeader("X-RateLimit-Limit", String.valueOf(capacity));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.remaining()));

        if (!decision.allowed()) {
            long retryAfterSeconds = Math.max(1, (long) Math.ceil(decision.retryAfterMs() / 1000.0));
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("text/plain");
            response.getWriter().write("Too Many Requests");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

