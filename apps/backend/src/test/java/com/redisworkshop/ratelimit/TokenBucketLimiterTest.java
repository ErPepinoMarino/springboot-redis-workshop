package com.redisworkshop.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.redisworkshop.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TokenBucketLimiterTest {

    @Autowired
    TokenBucketLimiter limiter;

    @Autowired
    StringRedisTemplate redis;

    @Test
    void permiteHastaLaCapacidadYLuegoRechaza() {
        redis.delete("ipCliente:exactitud");

        assertThat(limiter.tryAcquire("exactitud", 3, 0, 30).allowed()).isTrue();
        assertThat(limiter.tryAcquire("exactitud", 3, 0, 30).allowed()).isTrue();
        assertThat(limiter.tryAcquire("exactitud", 3, 0, 30).allowed()).isTrue();

        RateLimitDecision denied = limiter.tryAcquire("exactitud", 3, 0, 30);
        assertThat(denied.allowed()).isFalse();
        assertThat(denied.remaining()).isZero();
    }

    @Test
    void cubosIndependientesPorCliente() {
        redis.delete("ipCliente:A");
        redis.delete("ipCliente:B");

        limiter.tryAcquire("A", 1, 0, 30);

        assertThat(limiter.tryAcquire("A", 1, 0, 30).allowed()).isFalse();
        assertThat(limiter.tryAcquire("B", 1, 0, 30).allowed()).isTrue();
    }

    @Test
    void regeneraFichasConElTiempo() throws InterruptedException {
        redis.delete("ipCliente:refill");

        assertThat(limiter.tryAcquire("refill", 1, 100, 30).allowed()).isTrue();
        assertThat(limiter.tryAcquire("refill", 1, 100, 30).allowed()).isFalse();

        Thread.sleep(50);

        assertThat(limiter.tryAcquire("refill", 1, 100, 30).allowed()).isTrue();
    }

    @Test
    void exactamenteDiezDeCienEnConcurrencia() throws Exception {
        redis.delete("ipCliente:concurrencia");

        int threads = 100;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger allowed = new AtomicInteger();

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            tasks.add(() -> {
                start.await();
                if (limiter.tryAcquire("concurrencia", 10, 0, 30).allowed()) {
                    allowed.incrementAndGet();
                }
                return null;
            });
        }

        List<Future<Void>> futures = new ArrayList<>();
        for (Callable<Void> task : tasks) {
            futures.add(pool.submit(task));
        }
        start.countDown();
        for (Future<Void> future : futures) {
            future.get();
        }
        pool.shutdownNow();

        assertThat(allowed.get()).isEqualTo(10);
    }
}
