package com.redisworkshop.products;

import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@EnableCaching
public class CacheConfig {

    //Esto es para evitar condiciones de carrera en el testeo ( y posterior uso)
    //Ya que nuestro codigo tras lanzar una peticion a redis la "asumia hecha" y seguia ejecutando sin confirmar su ejecucion de facto.
      @Bean
    RedisCacheManagerBuilderCustomizer escriturasInmediatas(RedisConnectionFactory connectionFactory) {
        return builder -> builder.cacheWriter(
                RedisCacheWriter.create(connectionFactory, config -> config.immediateWrites(true)));
    }
}
