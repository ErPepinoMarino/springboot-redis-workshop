package com.redisworkshop.jobs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.redisworkshop.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class JobsIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void enqueueDevuelveJobPendiente() throws Exception {
        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"QUICK\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.type").value("QUICK"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void quickTerminaEnDone() throws Exception {
        String id = enqueue("QUICK");

        assertThat(waitForTerminal(id, 10_000)).isEqualTo("DONE");
    }

    @Test
    void failingTerminaEnFailedConError() throws Exception {
        String id = enqueue("FAILING");

        assertThat(waitForTerminal(id, 10_000)).isEqualTo("FAILED");
        assertThat(JsonPath.<String>read(body(id), "$.error")).isNotNull();
    }

    @Test
    void crashSeReclamaYTermina() throws Exception {
        String id = enqueue("CRASH");

        assertThat(waitForTerminal(id, 25_000)).isEqualTo("DONE");
        assertThat(JsonPath.<Integer>read(body(id), "$.attempts")).isEqualTo(2);
    }

    @Test
    void jobInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/jobs/no-existe"))
                .andExpect(status().isNotFound());
    }

    private String enqueue(String type) throws Exception {
        String response = mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"" + type + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.<String>read(response, "$.id");
    }

    private String waitForTerminal(String id, long timeoutMs) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMs;
        String status = "PENDING";
        while (System.currentTimeMillis() < deadline) {
            status = JsonPath.<String>read(body(id), "$.status");
            if ("DONE".equals(status) || "FAILED".equals(status)) {
                return status;
            }
            Thread.sleep(200);
        }
        return status;
    }

    private String body(String id) throws Exception {
        return mockMvc.perform(get("/jobs/" + id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }
}