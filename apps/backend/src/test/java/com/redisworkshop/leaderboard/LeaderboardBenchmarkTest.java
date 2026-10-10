package com.redisworkshop.leaderboard;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class LeaderboardBenchmarkTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void ejecutaElBenchmarkDelLeaderboard() throws Exception {
        mockMvc.perform(post("/leaderboard/benchmark")
                        .param("players", "100")
                        .param("concurrency", "4")
                        .param("duration", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players").value(100))
                .andExpect(jsonPath("$.concurrency").value(4))
                .andExpect(jsonPath("$.durationSeconds").value(1))
                .andExpect(jsonPath("$.redisSuccesses").isNumber())
                .andExpect(jsonPath("$.postgresSuccesses").isNumber())
                .andExpect(jsonPath("$.redisTop.length()").value(50))
                .andExpect(jsonPath("$.postgresTop.length()").value(50));
    }
}