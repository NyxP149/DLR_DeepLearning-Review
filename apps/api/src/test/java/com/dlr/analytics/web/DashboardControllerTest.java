package com.dlr.analytics.web;

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
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesTheLocalProfileAndJavaMvpProgress() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.displayName").value("Apprenant DLR"))
                .andExpect(jsonPath("$.totalLabs").value(6))
                .andExpect(jsonPath("$.completedLabs").isNumber())
                .andExpect(jsonPath("$.averageScore").isNumber())
                .andExpect(jsonPath("$.level").isNumber())
                .andExpect(jsonPath("$.currentStreak").isNumber())
                .andExpect(jsonPath("$.studyMinutes").isNumber())
                .andExpect(jsonPath("$.badges.length()").value(5));
    }
}
