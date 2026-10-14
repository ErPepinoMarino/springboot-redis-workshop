package com.redisworkshop.lock;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.redisworkshop.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class RedisLockTest {

    @Autowired
    RedisLock lock;

    @Autowired
    LockService service;

    @AfterEach
    void limpiar() {
        lock.release("pepe");
        lock.release("juan");
    }

    @Test
    void soloUnoAdquiereLaLlave() {
        assertThat(lock.tryAcquire("pepe")).isTrue();
        assertThat(lock.tryAcquire("juan")).isFalse();   // ocupada
        assertThat(lock.holder()).contains("pepe");
    }

    @Test
    void soloElDuenoLibera() {
        lock.tryAcquire("pepe");

        assertThat(lock.release("juan")).isFalse();      // juan NO es el dueño
        assertThat(lock.holder()).contains("pepe");

        assertThat(lock.release("pepe")).isTrue();       // pepe sí
        assertThat(lock.holder()).isEmpty();
    }

    @Test
    void accionarSoloConLaLlave() {
        assertThat(service.increment("juan").ok()).isFalse();   // sin llave → falla

        assertThat(service.acquire("pepe").ok()).isTrue();
        assertThat(service.increment("juan").ok()).isFalse();   // juan no la tiene
        assertThat(service.increment("pepe").ok()).isTrue();    // pepe sí
    }
}