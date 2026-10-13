package com.redisworkshop.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import com.redisworkshop.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class RateLimitStreamTest {

    @Autowired
    StringRedisTemplate redis;

    @Autowired
    RateLimitSimulationService service;

    @Test
    void laSimulacionPublicaTicksEnElCanal() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> firstTick = new AtomicReference<>();
        MessageListener listener = (message, pattern) -> {
            firstTick.compareAndSet(null, new String(message.getBody(), StandardCharsets.UTF_8));
            latch.countDown();
        };

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redis.getConnectionFactory());
        container.addMessageListener(listener, new ChannelTopic(RateLimitConstants.CHANNEL));
        container.afterPropertiesSet();
        container.start();
        try {
            service.start(1000, 100, 20, 180);

            assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
            assertThat(firstTick.get()).contains("plain");
        } finally {
            container.stop();
        }
    }
}