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
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void devuelveElProducto() throws Exception {
        mockMvc.perform(get("/cache/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Teclado mecánico"))
                .andExpect(jsonPath("$.price").value(49.90));
    }

    @Test
    void productoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/cache/products/9999"))
                .andExpect(status().isNotFound());
    }
     @Test
    void sinCacheDevuelveElProducto() throws Exception {
        mockMvc.perform(get("/cache/products/1").param("cached", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Teclado mecánico"));
    }
}