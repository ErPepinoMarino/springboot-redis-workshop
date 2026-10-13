package com.redisworkshop.pubsub;

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
class PubSubIntegrationTest {

    @Autowired
    StringRedisTemplate redis;

    @Autowired
    ClockPublisher publisher;

    @Test
    void elPublisherLlegaAlSuscriptor() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> received = new AtomicReference<>();
        MessageListener listener = (message, pattern) -> {
            received.set(new String(message.getBody(), StandardCharsets.UTF_8));
            latch.countDown();
        };

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redis.getConnectionFactory());
        container.addMessageListener(listener, new ChannelTopic(PubSubConstants.CLOCK_CHANNEL));
        container.afterPropertiesSet();
        container.start();
        try {
            publisher.tick();

            assertThat(latch.await(3, TimeUnit.SECONDS)).isTrue();
            assertThat(received.get()).isNotBlank();
        } finally {
            container.stop();
        }
    }
}