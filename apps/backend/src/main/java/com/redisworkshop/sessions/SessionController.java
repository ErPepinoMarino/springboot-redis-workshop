package com.redisworkshop.sessions;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private static final String COOKIE = "session";
    private final SessionTtlStreamer streamer;

    private final UserRepository users;
    private final SessionService sessions;
    private final PasswordEncoder encoder;
    private final Duration ttl;

    public SessionController(UserRepository users, SessionService sessions, PasswordEncoder encoder,
            SessionTtlStreamer streamer, @Value("${sessions.ttl:30s}") Duration ttl) {
        this.users = users;
        this.sessions = sessions;
        this.encoder = encoder;
        this.streamer = streamer;
        this.ttl = ttl;
    }

    @PostMapping("/login")
    ResponseEntity<SessionUser> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        User user = users.findByUsername(request.username()).orElse(null);
        if (user == null || !encoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = sessions.create(user.getUsername(), user.getRole());
        response.addCookie(cookie(token, (int) ttl.toSeconds()));
        return ResponseEntity.ok(new SessionUser(user.getUsername(), user.getRole()));
    }

    @GetMapping("/me")
    ResponseEntity<SessionUser> me(@CookieValue(name = COOKIE, required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return sessions.find(token)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    @GetMapping(path = "/ttl/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    ResponseEntity<SseEmitter> ttlStream(@CookieValue(name = COOKIE, required = false) String token) {
        if (token == null || sessions.find(token).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        SseEmitter emitter = new SseEmitter(0L);
        streamer.start(token, emitter);
        return ResponseEntity.ok(emitter);
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(@CookieValue(name = COOKIE, required = false) String token,
            HttpServletResponse response) {
        if (token != null) {
            sessions.delete(token);
        }
        response.addCookie(cookie("", 0));
        return ResponseEntity.noContent().build();
    }

    private Cookie cookie(String value, int maxAge) {
        Cookie cookie = new Cookie(COOKIE, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}