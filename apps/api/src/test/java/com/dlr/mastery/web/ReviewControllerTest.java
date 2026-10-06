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

    @Test
    void successIsRefusedWhileTheLabScoreIsBelowItsThreshold() throws Exception {
        UUID attemptId = insertScoredAttempt("JAVA-02", "COMPLETED_BELOW_THRESHOLD", "35.00");
        UUID reviewId = insertReview(attemptId, "JAVA-02", 0);

        mockMvc.perform(get("/api/reviews"))
                .andExpect(jsonPath("$[?(@.id == '" + reviewId + "')].belowThreshold").value(true));

        mockMvc.perform(post("/api/reviews/{id}/complete", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"successful\":true}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("35.00")));

        mockMvc.perform(get("/api/reviews"))
                .andExpect(jsonPath("$[?(@.id == '" + reviewId + "')]").exists());

        mockMvc.perform(post("/api/reviews/{id}/complete", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"successful\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextReview.stage").value(0));
    }

    @Test
    void successIsAcceptedOnceTheScoreReachesTheThreshold() throws Exception {
        UUID attemptId = insertScoredAttempt("JAVA-03", "COMPLETED", "85.00");
        UUID reviewId = insertReview(attemptId, "JAVA-03", 0);

        mockMvc.perform(get("/api/reviews"))
                .andExpect(jsonPath("$[?(@.id == '" + reviewId + "')].belowThreshold").value(false));

        mockMvc.perform(post("/api/reviews/{id}/complete", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"successful\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextReview.stage").value(1));
    }

    @Test
    void showsTheLiveScoreInsteadOfTheTextStoredAtFirstCompletion() throws Exception {
        UUID attemptId = insertScoredAttempt("JAVA-04", "COMPLETED", "95.00");
        UUID reviewId = insertReviewWithReason(attemptId, "JAVA-04", 0, "Score 35.00 % sous le seuil recommandé de 70 %.");

        mockMvc.perform(get("/api/reviews"))
                .andExpect(jsonPath("$[?(@.id == '" + reviewId + "')].reason")
                        .value(org.hamcrest.Matchers.contains(
                                "Consolider les concepts du laboratoire avec la répétition espacée.")));

        UUID lowAttempt = insertScoredAttempt("JAVA-05", "COMPLETED_BELOW_THRESHOLD", "42.50");
        UUID lowReview = insertReviewWithReason(lowAttempt, "JAVA-05", 0, "Consolider les concepts du laboratoire avec la répétition espacée.");

        mockMvc.perform(get("/api/reviews"))
                .andExpect(jsonPath("$[?(@.id == '" + lowReview + "')].reason")
                        .value(org.hamcrest.Matchers.contains("Score 42.50 % sous le seuil recommandé de 70 %.")));
    }

    @Test
    void keepsTheDifficultyReasonUntouched() throws Exception {
        UUID attemptId = insertScoredAttempt("JAVA-06", "COMPLETED", "90.00");
        UUID reviewId = insertReviewWithReason(attemptId, "JAVA-06", 0, "Revoir rapidement après une difficulté.");

        mockMvc.perform(get("/api/reviews"))
                .andExpect(jsonPath("$[?(@.id == '" + reviewId + "')].reason")
                        .value(org.hamcrest.Matchers.contains("Revoir rapidement après une difficulté.")));
    }

    @Test
    void cleanupMigrationKeepsOnlyTheMostAdvancedPendingReviewPerLab() throws Exception {
        jdbcTemplate.update("delete from review_item where lab_id = 'JAVA-07'");
        UUID attemptId = insertScoredAttempt("JAVA-07", "COMPLETED", "90.00");
        UUID early = insertReview(attemptId, "JAVA-07", 0);
        UUID advanced = insertReview(attemptId, "JAVA-07", 2);
        UUID done = insertReview(attemptId, "JAVA-07", 1);
        jdbcTemplate.update("update review_item set status = 'COMPLETED' where id = ?", done);

        String sql = new String(new org.springframework.core.io.ClassPathResource(
                "db/migration/V19__dedupe_pending_reviews.sql").getInputStream().readAllBytes(),
                java.nio.charset.StandardCharsets.UTF_8);
        jdbcTemplate.execute(sql);

        mockMvc.perform(get("/api/reviews"))
                .andExpect(jsonPath("$[?(@.id == '" + advanced + "')]").exists())
                .andExpect(jsonPath("$[?(@.id == '" + early + "')]").doesNotExist());
        org.assertj.core.api.Assertions.assertThat(jdbcTemplate.queryForObject(
                "select count(*) from review_item where id = ? and status = 'COMPLETED'", Integer.class, done)).isEqualTo(1);
    }

    private UUID insertReviewWithReason(UUID attemptId, String labCode, int stage, String reason) {
        UUID reviewId = insertReview(attemptId, labCode, stage);
        jdbcTemplate.update("update review_item set reason = ? where id = ?", reason, reviewId);
        return reviewId;
    }

    private UUID insertScoredAttempt(String labCode, String status, String score) {
        UUID attemptId = UUID.randomUUID();
        Instant now = Instant.now();
        jdbcTemplate.update(
                "insert into attempt (id, lab_id, started_at, completed_at, status, score) values (?, ?, ?, ?, ?, ?)",
                attemptId, labCode, Timestamp.from(now.minusSeconds(3600)), Timestamp.from(now.minusSeconds(60)),
                status, new java.math.BigDecimal(score));
        return attemptId;
    }

    private UUID insertReview(UUID attemptId, String labCode, int stage) {
        UUID reviewId = UUID.randomUUID();
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                insert into review_item (id, attempt_id, lab_id, due_at, reason, status, repetition_stage, created_at)
                values (?, ?, ?, ?, 'Test', 'PENDING', ?, ?)
                """,
                reviewId, attemptId, labCode, Timestamp.from(now.minusSeconds(60)), stage, Timestamp.from(now));
        return reviewId;
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
