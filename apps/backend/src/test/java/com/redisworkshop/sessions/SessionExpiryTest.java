package com.redisworkshop.sessions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.redisworkshop.TestcontainersConfiguration;

import jakarta.servlet.http.Cookie;

@SpringBootTest(properties = "sessions.ttl=1s")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SessionExpiryTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void laSesionCaducaTrasElTtl() throws Exception {
        MvcResult login = mockMvc.perform(post("/sessions/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"user\"}"))
                .andExpect(status().isOk())
                .andReturn();
        Cookie cookie = login.getResponse().getCookie("session");

        mockMvc.perform(get("/sessions/me").cookie(cookie))
                .andExpect(status().isOk());

        Thread.sleep(1500);

        mockMvc.perform(get("/sessions/me").cookie(cookie))
                .andExpect(status().isUnauthorized());
    }
}