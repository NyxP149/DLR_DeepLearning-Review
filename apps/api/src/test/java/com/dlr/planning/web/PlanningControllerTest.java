package com.dlr.planning.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

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

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
                .andExpect(jsonPath("$.activities.length()").value(24))
                .andExpect(jsonPath("$.sessions[0].actualMinutes").value(75));
    }

    @Test
    @Transactional
    void schedulesOnlyTheNextLabAfterTheEffectiveCompletionDate() throws Exception {
        Instant completedAt = Instant.now();
        jdbcTemplate.update(
                "insert into attempt (id, lab_id, started_at, completed_at, status, score) values (?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), "SQL-01", Timestamp.from(completedAt.minusSeconds(3600)),
                Timestamp.from(completedAt), "COMPLETED", 100);

        mockMvc.perform(get("/api/calendar").param("days", "7").param("path", "SQL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pathCode").value("SQL"))
                .andExpect(jsonPath("$.activities.length()").value(10))
                .andExpect(jsonPath("$.activities[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.activities[0].effectiveDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.activities[1].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.activities[1].effectiveDate").value(LocalDate.now().plusDays(1).toString()))
                .andExpect(jsonPath("$.activities[2].status").value("WAITING_FOR_COMPLETION"))
                .andExpect(jsonPath("$.activities[2].effectiveDate").doesNotExist())
                .andExpect(jsonPath("$.activities[2].availableAfterLabCode").value("SQL-02"));
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
