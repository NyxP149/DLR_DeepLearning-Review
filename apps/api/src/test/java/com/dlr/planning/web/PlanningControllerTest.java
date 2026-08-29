package com.dlr.planning.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlanningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void updatesTheLocalProfileAndRecordsActualStudyTime() throws Exception {
        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Nyx\",\"targetMonths\":4,\"weekdayMinutes\":90,\"weekendMinutes\":60}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Nyx"));

        String today = LocalDate.now().toString();
        mockMvc.perform(post("/api/calendar/{date}", today)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actualMinutes\":75,\"reward\":\"Une promenade\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actualMinutes").value(75))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(get("/api/calendar").param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessions.length()").value(7))
                .andExpect(jsonPath("$.sessions[0].actualMinutes").value(75));
    }

    @Test
    void rejectsAnUnsustainableProfile() throws Exception {
        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Nyx\",\"targetMonths\":2,\"weekdayMinutes\":600,\"weekendMinutes\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("VALIDATION_ERROR"));
    }
}
