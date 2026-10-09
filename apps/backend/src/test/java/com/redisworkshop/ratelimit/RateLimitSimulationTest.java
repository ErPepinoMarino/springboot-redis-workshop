package com.redisworkshop.ratelimit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.redisworkshop.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class RateLimitSimulationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void simulaElAluvion() throws Exception {
        mockMvc.perform(get("/ratelimit/simulate")
                        .param("requests", "1000")
                        .param("capacity", "20")
                        .param("rate", "20")
                        .param("queue", "180"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passed").value(20))
                .andExpect(jsonPath("$.queued").value(180))
                .andExpect(jsonPath("$.dropped").value(800))
                .andExpect(jsonPath("$.drainSeconds").value(9.0))
                .andExpect(jsonPath("$.timeline.length()").value(51))
                .andExpect(jsonPath("$.timeline[0].remaining").value(180))
                .andExpect(jsonPath("$.timeline[50].remaining").value(0));
    }

     @Test
    void aplicaLosLimitesDelBackend() throws Exception {
        mockMvc.perform(get("/ratelimit/simulate")
                        .param("requests", String.valueOf(RateLimitConstants.MAX_REQUESTS + 10))
                        .param("capacity", String.valueOf(RateLimitConstants.MAX_CAPACITY + 10))
                        .param("rate", "1")
                        .param("queue", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacity").value(RateLimitConstants.MAX_CAPACITY))
                .andExpect(jsonPath("$.passed").value(RateLimitConstants.MAX_CAPACITY));
    }
}