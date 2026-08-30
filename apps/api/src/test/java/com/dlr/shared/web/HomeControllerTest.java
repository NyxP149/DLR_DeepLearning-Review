package com.dlr.shared.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void exposesAUsefulApiLandingPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("DLR — Deep Learning & Review"))
                .andExpect(jsonPath("$.status").value("API disponible"))
                .andExpect(jsonPath("$.frontend").value("http://localhost:4200"))
                .andExpect(jsonPath("$.swagger").value("http://localhost:8081/swagger-ui.html"));
    }
}
