package com.redisworkshop.products;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.redisworkshop.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ProductCacheTest {

    @Autowired
    ProductService service;

    @Autowired
    ProductRepository repository;

    @Autowired
    StringRedisTemplate redis;

    @Test
    void guardaElProductoEnCacheAlLeerlo() {
        redis.delete("products::1");

        service.findById(1L);

        assertThat(redis.hasKey("products::1")).isTrue();
    }

    @Test
    void sirveDesdeLaCacheSinTocarLaBaseDeDatos() {
        Product saved = repository.save(new Product("Producto cacheado", "para test", new BigDecimal("1.00")));
        Long id = saved.getId();

        Product first = service.findById(id);

        repository.deleteById(id);
        Product second = service.findById(id);

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(second.getName()).isEqualTo("Producto cacheado");
    }
}