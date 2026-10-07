package com.redisworkshop.products;

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
class BenchmarkTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void ejecutaElBenchmarkConCache() throws Exception {
        mockMvc.perform(get("/cache/products/bench")
                        .param("iterations", "20")
                        .param("cached", "true")
                        .param("concurrency", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("cache"))
                .andExpect(jsonPath("$.count").value(20))
                .andExpect(jsonPath("$.throughput").isNumber());
    }

    @Test
    void ejecutaElBenchmarkSinCache() throws Exception {
        mockMvc.perform(get("/cache/products/bench")
                        .param("iterations", "20")
                        .param("cached", "false")
                        .param("concurrency", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("nocache"))
                .andExpect(jsonPath("$.count").value(20));
    }
}