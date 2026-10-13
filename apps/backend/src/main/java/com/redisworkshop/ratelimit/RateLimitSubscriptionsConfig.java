package com.redisworkshop.ratelimit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RateLimitSubscriptionsConfig {

    @Bean
    RedisMessageListenerContainer rateLimitListenerContainer(
            RedisConnectionFactory connectionFactory, RateLimitListener rateLimitListener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(rateLimitListener, new ChannelTopic(RateLimitConstants.CHANNEL));
        return container;
    }
}