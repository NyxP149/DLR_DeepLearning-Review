package com.dlr.catalog.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PathProgressControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void unlocksTheNextPythonLabAfterAValidatedPrerequisite() throws Exception {
        Instant now = Instant.now();
        jdbcTemplate.update(
                "insert into attempt (id, lab_id, started_at, completed_at, score, status) values (?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), "PYTHON-01", Timestamp.from(now), Timestamp.from(now), 100, "COMPLETED");

        mockMvc.perform(get("/api/paths/PYTHON/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedLabs").value(1))
                .andExpect(jsonPath("$.progressPercent").value(16))
                .andExpect(jsonPath("$.nextLabCode").value("PYTHON-02"))
                .andExpect(jsonPath("$.labs[1].state").value("AVAILABLE"));

        mockMvc.perform(post("/api/labs/PYTHON-02/attempts"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.labCode").value("PYTHON-02"));
    }
}
