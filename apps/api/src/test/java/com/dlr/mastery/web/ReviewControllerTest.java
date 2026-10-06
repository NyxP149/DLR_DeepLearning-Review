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

    @Test
    void successSpacesTheNextReviewByThreeSevenFourteenThenThirtyDays() throws Exception {
        int[] expectedDays = {3, 7, 14, 30};
        UUID attemptId = insertAttempt();
        UUID reviewId = insertReview(attemptId, 0);
        for (int stage = 0; stage < expectedDays.length; stage++) {
            String body = mockMvc.perform(post("/api/reviews/{id}/complete", reviewId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"successful\":true}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nextReview.stage").value(stage + 1))
                    .andReturn().getResponse().getContentAsString();
            var next = objectMapper.readTree(body).get("nextReview");
            long days = java.time.Duration.between(Instant.now(), Instant.parse(next.get("dueAt").asText())).toHours() / 24 + 1;
            org.assertj.core.api.Assertions.assertThat(days).isBetween((long) expectedDays[stage] - 1, (long) expectedDays[stage]);
            reviewId = UUID.fromString(next.get("id").asText());
        }
    }

    @Test
    void finishingTheLastStageSchedulesNothingMore() throws Exception {
        UUID reviewId = insertReview(insertAttempt(), 4);

        mockMvc.perform(post("/api/reviews/{id}/complete", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"successful\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value(true))
                .andExpect(jsonPath("$.nextReview").doesNotExist());
    }

    @Test
    void aDifficultReviewStaysOnTheSameStageAndComesBackTomorrow() throws Exception {
        UUID reviewId = insertReview(insertAttempt(), 2);

        mockMvc.perform(post("/api/reviews/{id}/complete", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"successful\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value(false))
                .andExpect(jsonPath("$.nextReview.stage").value(2))
                .andExpect(jsonPath("$.nextReview.reason").value("Revoir rapidement après une difficulté."));

        mockMvc.perform(get("/api/reviews"))
                .andExpect(jsonPath("$[?(@.id == '" + reviewId + "')]").doesNotExist());
    }

    private UUID insertAttempt() {
        UUID attemptId = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into attempt (id, lab_id, started_at, status) values (?, 'JAVA-01', ?, 'IN_PROGRESS')",
                attemptId, Timestamp.from(Instant.now().minusSeconds(3600)));
        return attemptId;
    }

    private UUID insertReview(UUID attemptId, int stage) {
        UUID reviewId = UUID.randomUUID();
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                insert into review_item (id, attempt_id, lab_id, due_at, reason, status, repetition_stage, created_at)
                values (?, ?, 'JAVA-01', ?, 'Test', 'PENDING', ?, ?)
                """,
                reviewId, attemptId, Timestamp.from(now.minusSeconds(60)), stage, Timestamp.from(now));
        return reviewId;
    }
}
