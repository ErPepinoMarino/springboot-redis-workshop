package com.redisworkshop.products;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.redisworkshop.TestcontainersConfiguration;

@SpringBootTest(properties = "spring.cache.redis.time-to-live=1s")
@Import(TestcontainersConfiguration.class)
class ProductCacheTtlTest {

    @Autowired
    ProductService service;

    @Autowired
    StringRedisTemplate redis;

    @Test
    void laEntradaCaducaTrasElTtl() throws InterruptedException {
        redis.delete("products::1");

        service.findById(1L);
        assertThat(redis.hasKey("products::1")).isTrue();

        Thread.sleep(1500);

        assertThat(redis.hasKey("products::1")).isFalse();
    }
     @Test
    void elModoSinCacheNoGuardaNada() {
        redis.delete("products::3");

        Product product = service.findByIdNoCache(3L);

        assertThat(product).isNotNull();
        assertThat(redis.hasKey("products::3")).isFalse();
    }
}