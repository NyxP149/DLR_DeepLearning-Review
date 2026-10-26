package com.dlr.mastery.web;

import com.dlr.learning.application.LabResetService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Maîtrise des concepts : pilotée par le dernier rappel (rétention) plutôt que par la seule étape de révision. */
@SpringBootTest
@AutoConfigureMockMvc
class ConceptMasteryControllerTest {

    private static final Instant COMPLETED_AT = Instant.parse("2026-01-01T10:00:00Z");

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired LabResetService resetService;

    private UUID attempt;

    @BeforeEach
    void setUp() {
        clean();
        attempt = UUID.randomUUID();
    }

    @AfterEach
    void clean() {
        resetService.reset("JAVA-01");
        jdbcTemplate.update("delete from adaptation_recommendation");
    }

    @Test
    void exposesConceptsWithoutLeakingAssessmentSecrets() throws Exception {
        mockMvc.perform(get("/api/mastery/concepts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("JAVA-JVM-BYTECODE"))
                .andExpect(jsonPath("$[0].labCode").value("JAVA-01"))
                .andExpect(jsonPath("$[0].status").isNotEmpty())
                .andExpect(jsonPath("$[0].masteryQuestion").isNotEmpty())
                .andExpect(jsonPath("$[1].status").value("NOT_STARTED"))
                .andExpect(jsonPath("$[1].retention").value("NONE"))
                .andExpect(jsonPath("$[1].lastRecallDays").doesNotExist())
                .andExpect(jsonPath("$[1].score").doesNotExist());
    }

    @Test
    void keepsTheStageRuleWhenNoRecallHasBeenRecorded() throws Exception {
        completedAttempt(85);
        reviewItem(4);

        mockMvc.perform(get("/api/mastery/concepts"))
                .andExpect(jsonPath("$[0].status").value("MASTERED"))
                .andExpect(jsonPath("$[0].retention").value("NONE"));
    }

    @Test
    void masteryNeedsACorrectConfidentRecallAtTheFinalStage() throws Exception {
        completedAttempt(85);
        recall(4, 100, 3, 30);

        mockMvc.perform(get("/api/mastery/concepts"))
                .andExpect(jsonPath("$[0].status").value("MASTERED"))
                .andExpect(jsonPath("$[0].retention").value("RETAINED"))
                .andExpect(jsonPath("$[0].lastRecallDays").value(30));
    }

    @Test
    void aFailedFinalReviewIsNoLongerMasteryAndFlagsAForgottenConcept() throws Exception {
        completedAttempt(85);
        recall(4, 0, 2, 30);

        mockMvc.perform(get("/api/mastery/concepts"))
                .andExpect(jsonPath("$[0].status").value("TO_REVIEW"))
                .andExpect(jsonPath("$[0].retention").value("FORGOTTEN"))
                .andExpect(jsonPath("$[0].completedReviewStage").value(4));
    }

    @Test
    void aHesitantOrEarlyOrPartialRecallKeepsTheConceptConsolidating() throws Exception {
        completedAttempt(85);
        recall(4, 100, 1, 30);
        mockMvc.perform(get("/api/mastery/concepts"))
                .andExpect(jsonPath("$[0].status").value("CONSOLIDATING"))
                .andExpect(jsonPath("$[0].retention").value("RETAINED"));

        clean();
        attempt = UUID.randomUUID();
        completedAttempt(85);
        recall(2, 100, 3, 7);
        mockMvc.perform(get("/api/mastery/concepts"))
                .andExpect(jsonPath("$[0].status").value("CONSOLIDATING"));

        clean();
        attempt = UUID.randomUUID();
        completedAttempt(85);
        recall(4, 50, 3, 30);
        mockMvc.perform(get("/api/mastery/concepts"))
                .andExpect(jsonPath("$[0].status").value("CONSOLIDATING"))
                .andExpect(jsonPath("$[0].retention").value("SHAKY"));
    }

    @Test
    void onlyTheLatestRecallCounts() throws Exception {
        completedAttempt(85);
        recall(0, 0, 2, 1);
        recall(0, 100, 3, 3);
        mockMvc.perform(get("/api/mastery/concepts"))
                .andExpect(jsonPath("$[0].status").value("CONSOLIDATING"))
                .andExpect(jsonPath("$[0].retention").value("RETAINED"));

        recall(1, 0, 2, 8);
        mockMvc.perform(get("/api/mastery/concepts"))
                .andExpect(jsonPath("$[0].status").value("TO_REVIEW"))
                .andExpect(jsonPath("$[0].lastRecallDays").value(8));
    }

    @Test
    void aLabBelowTheThresholdStaysToReviewWhateverTheRecall() throws Exception {
        completedAttempt(40);
        recall(0, 100, 3, 3);

        mockMvc.perform(get("/api/mastery/concepts"))
                .andExpect(jsonPath("$[0].status").value("TO_REVIEW"))
                .andExpect(jsonPath("$[0].retention").value("RETAINED"));
    }

    @Test
    void recommendsAForgottenConceptFirstAndExplainsWhy() throws Exception {
        completedAttempt(85);
        recall(1, 0, 3, 3);

        mockMvc.perform(get("/api/adaptation/recommendation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labCode").value("JAVA-01"))
                .andExpect(jsonPath("$.difficulty").value("RENFORCEMENT"))
                .andExpect(jsonPath("$.reason").value(org.hamcrest.Matchers.containsString("s'efface")))
                .andExpect(jsonPath("$.factors[2]").value("dernier rappel raté, à J+3 après la fin du labo"));
    }

    private void completedAttempt(int score) {
        jdbcTemplate.update(
                "insert into attempt (id, lab_id, started_at, completed_at, status, score) values (?, 'JAVA-01', ?, ?, 'COMPLETED', ?)",
                attempt, Timestamp.from(COMPLETED_AT.minus(1, ChronoUnit.HOURS)), Timestamp.from(COMPLETED_AT), score);
    }

    private void reviewItem(int stage) {
        jdbcTemplate.update(
                """
                insert into review_item (id, attempt_id, lab_id, due_at, reason, status, repetition_stage, created_at, completed_at)
                values (?, ?, 'JAVA-01', ?, 'Test', 'COMPLETED', ?, ?, ?)
                """, UUID.randomUUID(), attempt, Timestamp.from(COMPLETED_AT), stage, Timestamp.from(COMPLETED_AT),
                Timestamp.from(COMPLETED_AT));
    }

    /** Révision de l'étape donnée, répondue `daysAfter` jours après la fin du labo avec ce score et cette confiance. */
    private void recall(int stage, int score, int confidence, int daysAfter) {
        Instant answeredAt = COMPLETED_AT.plus(daysAfter, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS);
        UUID reviewId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                insert into review_item (id, attempt_id, lab_id, due_at, reason, status, repetition_stage, created_at, completed_at)
                values (?, ?, 'JAVA-01', ?, 'Test', 'COMPLETED', ?, ?, ?)
                """, reviewId, attempt, Timestamp.from(answeredAt), stage, Timestamp.from(answeredAt), Timestamp.from(answeredAt));
        jdbcTemplate.update(
                """
                insert into review_answer (id, review_id, question_id, score, outcome, confidence, answered_at)
                values (?, ?, 'JAVA-01-Q1', ?, 'WRONG', ?, ?)
                """, UUID.randomUUID(), reviewId, score, confidence, Timestamp.from(answeredAt));
    }
}
