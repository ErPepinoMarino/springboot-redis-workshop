package com.redisworkshop.ratelimit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RateLimitSimulationService {

    private final StringRedisTemplate redis;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "ratelimit-sim");
        thread.setDaemon(true);
        return thread;
    });

    public RateLimitSimulationService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void start(int total, int capacity, double rate, int queue) {
        executor.submit(() -> run(total, capacity, rate, queue));
    }

    private void run(int total, int capacity, double rate, int queue) {
        int passed = Math.min(total, capacity);          // admite ya (bucket lleno)
        int queued = Math.min(total - passed, queue);    // a la cola
        double perTick = rate * RateLimitConstants.TICK_MS / 1000.0;
        int bucketTicks = perTick > 0 ? (int) Math.ceil(queued / perTick) : 0;

        // 1. Ticks iniciales a CERO (se ve la base antes del pico)
        for (int i = 0; i < RateLimitConstants.INITIAL_ZERO_TICKS; i++) {
            tick(0, 0);
        }

        // 2. Modelo: tick 0 = ráfaga; luego el bucket suelta rate por tick
        for (int i = 0; i <= bucketTicks; i++) {
            int plain = i == 0 ? passed + queued : 0;
            int bucket = i == 0
                    ? passed
                    : (int) Math.round(Math.min(queued - (i - 1) * perTick, perTick));
            tick(plain, bucket);
        }
        //Fin de la simulacion:
        redis.convertAndSend(RateLimitConstants.CHANNEL, "{\"done\":true}");
    }

    private void tick(int plain, int bucket) {
        // Admite → INCRBY en Redis
        if (plain > 0) {
            redis.opsForValue().increment(RateLimitConstants.SERVED_PLAIN_KEY, plain);
        }
        if (bucket > 0) {
            redis.opsForValue().increment(RateLimitConstants.SERVED_BUCKET_KEY, bucket);
        }

        // Lee + resetea (GETSET) y publica
        long servedPlain = readAndReset(RateLimitConstants.SERVED_PLAIN_KEY);
        long servedBucket = readAndReset(RateLimitConstants.SERVED_BUCKET_KEY);
        redis.convertAndSend(RateLimitConstants.CHANNEL,
                "{\"plain\":" + servedPlain + ",\"bucket\":" + servedBucket + "}");

        sleep(RateLimitConstants.TICK_MS);
    }

    private long readAndReset(String key) {
        String value = redis.opsForValue().getAndSet(key, "0");
        return value == null ? 0 : Long.parseLong(value);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}