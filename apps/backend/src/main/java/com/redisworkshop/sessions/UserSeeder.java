package com.redisworkshop.sessions;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserSeeder implements CommandLineRunner {

    private final UserRepository repository;
    private final PasswordEncoder encoder;

    public UserSeeder(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) { //String... args es obligatorio pero no lo usamos pa na.
        seed("user", "user", "USER");
        seed("admin", "admin", "ADMIN");
    }

    private void seed(String username, String rawPassword, String role) {
        if (repository.findByUsername(username).isEmpty()) {
            repository.save(new User(username, encoder.encode(rawPassword), role));
        }
    }
}