package com.redisworkshop.products;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.redisworkshop.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ProductRepositoryTest {

    @Autowired
    ProductRepository repository;

    @Test
    void guardaYLeeUnProducto() {
        Product saved = repository.save(new Product("Webcam", "1080p", new BigDecimal("59.90")));

        Product found = repository.findById(saved.getId()).orElseThrow();

        assertThat(found.getName()).isEqualTo("Webcam");
        assertThat(found.getPrice()).isEqualByComparingTo("59.90");
    }

    @Test
    void encuentraLosProductosDeEjemplo() {
        assertThat(repository.findById(1L)).isPresent();
    }
}
