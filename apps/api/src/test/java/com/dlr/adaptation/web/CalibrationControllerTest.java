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
                .andExpect(jsonPath("$.blindSpots.length()").value(0))
                .andExpect(jsonPath("$.trend.verdict").value("INSUFFICIENT_DATA"));
    }

    @Test
    void showsAnImprovingCalibrationWhenTheGapBetweenConfidenceAndResultsShrinks() throws Exception {
        week("2026-01-05", 3, 0, 5);
        week("2026-01-12", 3, 4, 1);

        mockMvc.perform(get("/api/adaptation/calibration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trend.verdict").value("IMPROVING"))
                .andExpect(jsonPath("$.trend.periods.length()").value(2))
                .andExpect(jsonPath("$.trend.periods[0].weekStart").value("2026-01-05"))
                .andExpect(jsonPath("$.trend.periods[0].confidencePercent").value(100))
                .andExpect(jsonPath("$.trend.periods[0].accuracyPercent").value(0))
                .andExpect(jsonPath("$.trend.periods[0].gapPoints").value(100))
                .andExpect(jsonPath("$.trend.periods[0].overconfidencePercent").value(100))
                .andExpect(jsonPath("$.trend.periods[1].accuracyPercent").value(80))
                .andExpect(jsonPath("$.trend.periods[1].gapPoints").value(20))
                .andExpect(jsonPath("$.trend.message").value(org.hamcrest.Matchers.containsString("de 100 à 20 points")));
    }

    @Test
    void showsAWorseningCalibrationAndGroupsAnswersByWeek() throws Exception {
        week("2026-01-05", 3, 5, 0);
        // Un mardi : rattaché au lundi 12 janvier.
        week("2026-01-13", 3, 2, 3);

        mockMvc.perform(get("/api/adaptation/calibration"))
                .andExpect(jsonPath("$.trend.verdict").value("WORSENING"))
                .andExpect(jsonPath("$.trend.periods[1].weekStart").value("2026-01-12"))
                .andExpect(jsonPath("$.trend.periods[1].total").value(5))
                .andExpect(jsonPath("$.trend.periods[1].gapPoints").value(60));
    }

    @Test
    void reportsAStableGapAndIgnoresWeeksWithTooFewAnswers() throws Exception {
        week("2026-01-05", 3, 3, 2);
        week("2026-01-12", 3, 3, 2);
        week("2026-01-19", 3, 0, 2);

        mockMvc.perform(get("/api/adaptation/calibration"))
                .andExpect(jsonPath("$.trend.verdict").value("STABLE"))
                .andExpect(jsonPath("$.trend.periods.length()").value(3))
                .andExpect(jsonPath("$.trend.periods[2].reliable").value(false))
                .andExpect(jsonPath("$.trend.periods[0].reliable").value(true));
    }

    @Test
    void measuresUnderconfidenceAsANegativeGap() throws Exception {
        week("2026-01-05", 1, 5, 0);
        week("2026-01-12", 1, 5, 0);

        mockMvc.perform(get("/api/adaptation/calibration"))
                .andExpect(jsonPath("$.trend.periods[0].confidencePercent").value(33))
                .andExpect(jsonPath("$.trend.periods[0].gapPoints").value(-67))
                .andExpect(jsonPath("$.trend.periods[0].sureTotal").value(0))
                .andExpect(jsonPath("$.trend.verdict").value("STABLE"));
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

    /** Une semaine de réponses à une confiance donnée : `correct` justes puis `wrong` fausses, toutes datées de cette semaine. */
    private void week(String monday, int confidence, int correct, int wrong) {
        UUID attempt = UUID.randomUUID();
        Instant base = Instant.parse(monday + "T10:00:00Z");
        jdbcTemplate.update("insert into attempt (id, lab_id, started_at, status) values (?, 'JAVA-01', ?, 'IN_PROGRESS')",
                attempt, Timestamp.from(base));
        int question = 1;
        for (int index = 0; index < correct; index++) logAt(attempt, question++, 100, confidence, base.plusSeconds(question));
        for (int index = 0; index < wrong; index++) logAt(attempt, question++, 0, confidence, base.plusSeconds(question));
    }

    private void logAt(UUID attempt, int question, int score, int confidence, Instant at) {
        jdbcTemplate.update(
                "insert into quiz_answer_log (id, attempt_id, question_id, score, confidence, answered_at) values (?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), attempt, "JAVA-01-Q" + question, score, confidence, Timestamp.from(at));
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
