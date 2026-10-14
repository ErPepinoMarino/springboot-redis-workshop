package com.redisworkshop.sessions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SessionTtlStreamer {

    private static final long TICK_MS = 250;

    private final SessionService sessions;
    private final ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "session-ttl");
        thread.setDaemon(true);
        return thread;
    });

    public SessionTtlStreamer(SessionService sessions) {
        this.sessions = sessions;
    }

    public void start(String token, SseEmitter emitter) {
        executor.submit(() -> run(token, emitter));
    }

    private void run(String token, SseEmitter emitter) {
        try {
            while (true) {
                long millis = sessions.ttlMillis(token);
                if (millis == -2) {
                    emitter.send(SseEmitter.event().data("{\"expired\":true}"));
                    emitter.complete();
                    return;
                }
                emitter.send(SseEmitter.event().data("{\"ttl\":" + Math.max(millis, 0) + "}"));
                sleep(TICK_MS);
            }
        } catch (Exception e) {
            emitter.complete(); // cliente desconectado → cerramos limpio
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