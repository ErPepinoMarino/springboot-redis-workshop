package com.redisworkshop.sse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseRegistry {

    private final Map<String, List<SseEmitter>> byTopic = new ConcurrentHashMap<>();

    public SseEmitter register(String topic) {
        SseEmitter emitter = new SseEmitter(0L); // 0 = sin timeout (no caduca a los ~30s)
        byTopic.computeIfAbsent(topic, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(topic, emitter));
        emitter.onError(error -> remove(topic, emitter));
        emitter.onTimeout(() -> remove(topic, emitter));
        return emitter;
    }

    public void fanOut(String topic, String data) {
        for (SseEmitter emitter : byTopic.getOrDefault(topic, List.of())) {
            try {
                emitter.send(SseEmitter.event().data(data));
            } catch (Exception e) {
                remove(topic, emitter);
            }
        }
    }

    private void remove(String topic, SseEmitter emitter) {
        byTopic.getOrDefault(topic, List.of()).remove(emitter);
    }
}