package com.redisworkshop;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class RedisConnectionTest {

    @Autowired
    StringRedisTemplate redis;

    @Test
    void guardaYLeeUnaClave() {
        redis.opsForValue().set("saludo", "hola");

        assertThat(redis.opsForValue().get("saludo")).isEqualTo("hola");
    }
}