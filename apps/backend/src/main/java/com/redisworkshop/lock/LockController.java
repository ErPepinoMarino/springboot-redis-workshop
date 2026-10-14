package com.redisworkshop.lock;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/lock")
public class LockController {

    private final LockService service;
    private final LockTtlStreamer streamer;

    public LockController(LockService service, LockTtlStreamer streamer) {
        this.service = service;
        this.streamer = streamer;
    }

    @PostMapping("/acquire")
    LockResponse acquire(@RequestParam String client) {
        return service.acquire(client);
    }

    @PostMapping("/release")
    LockResponse release(@RequestParam String client) {
        return service.release(client);
    }

    @PostMapping("/counter/increment")
    LockResponse increment(@RequestParam String client) {
        return service.increment(client);
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(0L);
        streamer.start(emitter);
        return emitter;
    }
}