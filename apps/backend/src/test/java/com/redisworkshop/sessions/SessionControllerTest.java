package com.redisworkshop.sessions;

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
import org.springframework.test.web.servlet.MvcResult;

import com.redisworkshop.TestcontainersConfiguration;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SessionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void loginValidoDevuelveSesionYCookie() throws Exception {
        MvcResult result = mockMvc.perform(post("/sessions/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"user\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"))
                .andReturn();

        assertThat(result.getResponse().getCookie("session")).isNotNull();
    }

    @Test
    void loginInvalidoDevuelve401() throws Exception {
        mockMvc.perform(post("/sessions/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"malo\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meSinCookieDevuelve401() throws Exception {
        mockMvc.perform(get("/sessions/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meConCookieDevuelveElUsuario() throws Exception {
        Cookie cookie = login("user", "user");

        mockMvc.perform(get("/sessions/me").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"));
    }

    @Test
    void logoutInvalidaLaSesion() throws Exception {
        Cookie cookie = login("user", "user");

        mockMvc.perform(post("/sessions/logout").cookie(cookie))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/sessions/me").cookie(cookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void contadoresSegunRol() throws Exception {
        mockMvc.perform(post("/sessions/counters/public/increment"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/sessions/counters/authenticated/increment"))
                .andExpect(status().isUnauthorized());

        Cookie user = login("user", "user");
        mockMvc.perform(post("/sessions/counters/authenticated/increment").cookie(user))
                .andExpect(status().isOk());
        mockMvc.perform(post("/sessions/counters/admin/increment").cookie(user))
                .andExpect(status().isForbidden());

        Cookie admin = login("admin", "admin");
        mockMvc.perform(post("/sessions/counters/admin/increment").cookie(admin))
                .andExpect(status().isOk());
    }

    private Cookie login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/sessions/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie("session");
    }
}