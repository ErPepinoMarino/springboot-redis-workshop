package com.redisworkshop.ratelimit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.redisworkshop.TestcontainersConfiguration;

@SpringBootTest(properties = {
        "app.ratelimit.capacity=2",
        "app.ratelimit.rate=0",
        "app.ratelimit.ttl-seconds=30"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class RateLimitFilterTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    StringRedisTemplate redis;

     @Test
    void permiteHastaElLimiteYLuegoDevuelve429() throws Exception {
        redis.delete("ipCliente:127.0.0.1");

        mockMvc.perform(get("/ratelimit/ping"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-RateLimit-Limit", "2"))
                .andExpect(header().string("X-RateLimit-Remaining", "1"));

        mockMvc.perform(get("/ratelimit/ping"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-RateLimit-Remaining", "0"));

        mockMvc.perform(get("/ratelimit/ping"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"));
    }

    @Test
    void noLimitaOtrasRutas() throws Exception {
        mockMvc.perform(get("/healthz"))
                .andExpect(status().isOk());
    }
}