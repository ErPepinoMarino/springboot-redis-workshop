package com.redisworkshop.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class LockTtlStreamer {

    private final RedisLock lock;
    private final ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "lock-ttl");
        thread.setDaemon(true);
        return thread;
    });

    public LockTtlStreamer(RedisLock lock) {
        this.lock = lock;
    }

    public void start(SseEmitter emitter) {
        executor.submit(() -> run(emitter));
    }

    private void run(SseEmitter emitter) {
        try {
            while (true) {
                String holder = lock.holder().orElse(null);
                long ttl = holder == null ? 0 : Math.max(lock.ttlMillis(), 0);
                String json = "{\"holder\":" + (holder == null ? "null" : "\"" + holder + "\"")
                        + ",\"ttlMs\":" + ttl + "}";
                emitter.send(SseEmitter.event().data(json));
                sleep(LockConstants.TICK_MS);
            }
        } catch (Exception e) {
            emitter.complete(); // cliente desconectado
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}