package com.dlr.adaptation.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Calibration : la confiance déclarée avant la correction est comparée au résultat de la première réponse. */
@SpringBootTest
@AutoConfigureMockMvc
class CalibrationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;

    private int tick;

    @BeforeEach
    void reset() {
        jdbcTemplate.update("delete from review_answer");
        jdbcTemplate.update("delete from quiz_answer_log");
        tick = 0;
    }

    @Test
    void asksForMoreAnswersBeforeJudgingTheCalibration() throws Exception {
        UUID attempt = attempt();
        log(attempt, "JAVA-01-Q1", 100, 3);

        mockMvc.perform(get("/api/adaptation/calibration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAnswers").value(1))
                .andExpect(jsonPath("$.verdict").value("INSUFFICIENT_DATA"))
                .andExpect(jsonPath("$.levels.length()").value(3))
                .andExpect(jsonPath("$.blindSpots.length()").value(0));
    }

    @Test
    void flagsOverconfidenceAndListsTheBlindSpotsUsingOnlyTheFirstAnswer() throws Exception {
        UUID sure = attempt();
        log(sure, "JAVA-01-Q1", 100, 3);
        log(sure, "JAVA-01-Q2", 0, 3);
        log(sure, "JAVA-01-Q3", 0, 3);
        log(sure, "JAVA-01-Q4", 0, 3);
        log(sure, "JAVA-01-Q5", 50, 3);
        // Corrigée ensuite : seule la première réponse compte pour la calibration.
        log(sure, "JAVA-01-Q2", 100, 3);
        UUID unsure = attempt();
        for (int question = 1; question <= 5; question++) {
            log(unsure, "JAVA-01-Q" + question, 0, 1);
        }

        mockMvc.perform(get("/api/adaptation/calibration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAnswers").value(10))
                .andExpect(jsonPath("$.verdict").value("OVERCONFIDENT"))
                .andExpect(jsonPath("$.levels[2].total").value(5))
                .andExpect(jsonPath("$.levels[2].correct").value(1))
                .andExpect(jsonPath("$.levels[2].partial").value(1))
                .andExpect(jsonPath("$.levels[2].wrong").value(3))
                .andExpect(jsonPath("$.levels[2].correctPercent").value(20))
                .andExpect(jsonPath("$.levels[0].total").value(5))
                .andExpect(jsonPath("$.overconfidencePercent").value(80))
                .andExpect(jsonPath("$.sources[0].code").value("QUIZ"))
                .andExpect(jsonPath("$.blindSpots.length()").value(4))
                .andExpect(jsonPath("$.blindSpots[0].labCode").value("JAVA-01"))
                .andExpect(jsonPath("$.blindSpots[0].prompt").isNotEmpty());
    }

    @Test
    void recognizesAnUnderconfidentLearnerWhoIsRightWhileDoubting() throws Exception {
        UUID attempt = attempt();
        for (int question = 1; question <= 5; question++) {
            log(attempt, "JAVA-01-Q" + question, 100, 1);
        }
        UUID other = attempt();
        for (int question = 1; question <= 5; question++) {
            log(other, "JAVA-01-Q" + question, 100, 2);
        }

        mockMvc.perform(get("/api/adaptation/calibration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verdict").value("UNDERCONFIDENT"))
                .andExpect(jsonPath("$.underconfidencePercent").value(100))
                .andExpect(jsonPath("$.blindSpots.length()").value(0));
    }

    @Test
    void countsReviewAnswersAndIgnoresAnswersWithoutConfidence() throws Exception {
        UUID attempt = attempt();
        for (int question = 1; question <= 5; question++) {
            log(attempt, "JAVA-01-Q" + question, 100, 3);
        }
        UUID unsure = attempt();
        for (int question = 1; question <= 4; question++) {
            log(unsure, "JAVA-01-Q" + question, 0, 1);
        }
        jdbcTemplate.update(
                "insert into quiz_answer_log (id, attempt_id, question_id, score, confidence, answered_at) values (?, ?, ?, 0, null, ?)",
                UUID.randomUUID(), unsure, "JAVA-01-Q5", at());
        review(attempt, "JAVA-01-Q1", 0, 1);

        mockMvc.perform(get("/api/adaptation/calibration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAnswers").value(10))
                .andExpect(jsonPath("$.verdict").value("CALIBRATED"))
                .andExpect(jsonPath("$.sources[1].code").value("REVIEW"))
                .andExpect(jsonPath("$.sources[1].total").value(1))
                .andExpect(jsonPath("$.levels[2].correctPercent").value(100));
    }

    private UUID attempt() {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("insert into attempt (id, lab_id, started_at, status) values (?, 'JAVA-01', ?, 'IN_PROGRESS')",
                id, at());
        return id;
    }

    private void log(UUID attempt, String question, int score, int confidence) {
        jdbcTemplate.update(
                "insert into quiz_answer_log (id, attempt_id, question_id, score, confidence, answered_at) values (?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), attempt, question, score, confidence, at());
    }

    private void review(UUID attempt, String question, int score, int confidence) {
        UUID reviewId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                insert into review_item (id, attempt_id, lab_id, due_at, reason, status, repetition_stage, created_at)
                values (?, ?, 'JAVA-01', ?, 'Test', 'COMPLETED', 0, ?)
                """, reviewId, attempt, at(), at());
        jdbcTemplate.update(
                """
                insert into review_answer (id, review_id, question_id, score, outcome, confidence, answered_at)
                values (?, ?, ?, ?, 'WRONG', ?, ?)
                """, UUID.randomUUID(), reviewId, question, score, confidence, at());
    }

    private Timestamp at() {
        return Timestamp.from(Instant.parse("2026-01-01T10:00:00Z").plusSeconds(tick++));
    }
}
