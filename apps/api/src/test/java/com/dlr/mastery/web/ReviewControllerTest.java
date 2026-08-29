package com.dlr.mastery.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired ObjectMapper objectMapper;

    @Test
    void completesAReviewAndSchedulesTheNextInterval() throws Exception {
        UUID attemptId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        Instant now = Instant.now();
        jdbcTemplate.update(
                "insert into attempt (id, lab_id, started_at, status) values (?, 'JAVA-01', ?, 'IN_PROGRESS')",
                attemptId, Timestamp.from(now.minusSeconds(3600)));
        jdbcTemplate.update(
                """
                insert into review_item (id, attempt_id, lab_id, due_at, reason, status, repetition_stage, created_at)
                values (?, ?, 'JAVA-01', ?, 'Test', 'PENDING', 0, ?)
                """,
                reviewId, attemptId, Timestamp.from(now.minusSeconds(60)), Timestamp.from(now));

        mockMvc.perform(get("/api/reviews/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + reviewId + "')]").exists());

        mockMvc.perform(post("/api/reviews/{id}/complete", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"successful\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value(true))
                .andExpect(jsonPath("$.nextReview.stage").value(1));
    }
}
