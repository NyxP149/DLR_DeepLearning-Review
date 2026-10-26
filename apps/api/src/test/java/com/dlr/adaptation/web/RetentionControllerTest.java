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
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Rétention différée : les rappels des révisions rangés selon le délai écoulé depuis la fin du laboratoire. */
@SpringBootTest
@AutoConfigureMockMvc
class RetentionControllerTest {

    private static final Instant COMPLETED_AT = Instant.parse("2026-01-01T10:00:00Z");

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;

    private UUID attempt;

    @BeforeEach
    void reset() {
        jdbcTemplate.update("delete from review_answer");
        jdbcTemplate.update("delete from quiz_answer_log");
        jdbcTemplate.update("delete from quiz_answer");
        attempt = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into attempt (id, lab_id, started_at, completed_at, status) values (?, 'JAVA-01', ?, ?, 'COMPLETED')",
                attempt, Timestamp.from(COMPLETED_AT.minus(1, ChronoUnit.HOURS)), Timestamp.from(COMPLETED_AT));
    }

    @Test
    void asksForMoreRecallsBeforeJudgingRetention() throws Exception {
        initial("JAVA-01-Q1", 100);
        recall("JAVA-01-Q1", 100, 1);

        mockMvc.perform(get("/api/adaptation/retention"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecalls").value(1))
                .andExpect(jsonPath("$.verdict").value("INSUFFICIENT_DATA"))
                .andExpect(jsonPath("$.buckets.length()").value(4))
                .andExpect(jsonPath("$.retainedLabs.length()").value(0))
                .andExpect(jsonPath("$.forgotten.length()").value(0));
    }

    @Test
    void waitsForRecallsMadeAtLeastThreeDaysAfterTheLab() throws Exception {
        for (int question = 1; question <= 5; question++) {
            initial("JAVA-01-Q" + question, 100);
            recall("JAVA-01-Q" + question, 100, question % 3);
        }

        mockMvc.perform(get("/api/adaptation/retention"))
                .andExpect(jsonPath("$.totalRecalls").value(5))
                .andExpect(jsonPath("$.verdict").value("TOO_EARLY"))
                .andExpect(jsonPath("$.buckets[0].total").value(5));
    }

    @Test
    void recognizesDurableRetentionAndListsTheRetainedLab() throws Exception {
        int[] days = {1, 4, 5, 9, 16, 20};
        for (int index = 0; index < days.length; index++) {
            String question = "JAVA-01-Q" + (index % 5 + 1);
            initial(question, 100);
            recall(question, 100, days[index]);
        }

        mockMvc.perform(get("/api/adaptation/retention"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecalls").value(6))
                .andExpect(jsonPath("$.verdict").value("DURABLE"))
                .andExpect(jsonPath("$.buckets[0].total").value(1))
                .andExpect(jsonPath("$.buckets[1].total").value(2))
                .andExpect(jsonPath("$.buckets[2].total").value(1))
                .andExpect(jsonPath("$.buckets[3].total").value(2))
                .andExpect(jsonPath("$.buckets[3].toDays").doesNotExist())
                .andExpect(jsonPath("$.buckets[1].correctPercent").value(100))
                .andExpect(jsonPath("$.buckets[1].initialPercent").value(100))
                .andExpect(jsonPath("$.retainedLabs.length()").value(1))
                .andExpect(jsonPath("$.retainedLabs[0].labCode").value("JAVA-01"))
                .andExpect(jsonPath("$.retainedLabs[0].delayDays").value(20))
                .andExpect(jsonPath("$.forgotten.length()").value(0));
    }

    @Test
    void flagsFragileRetentionAndListsWhatWasForgotten() throws Exception {
        initial("JAVA-01-Q1", 100);
        initial("JAVA-01-Q2", 100);
        initial("JAVA-01-Q3", 100);
        initial("JAVA-01-Q4", 100);
        initial("JAVA-01-Q5", 0);
        // Une première réponse fausse puis corrigée : seule la première compte pour la comparaison.
        initial("JAVA-01-Q5", 100);
        recall("JAVA-01-Q5", 100, 2);
        recall("JAVA-01-Q1", 0, 4);
        recall("JAVA-01-Q2", 0, 5);
        recall("JAVA-01-Q3", 50, 6);
        recall("JAVA-01-Q4", 100, 8);

        mockMvc.perform(get("/api/adaptation/retention"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecalls").value(5))
                .andExpect(jsonPath("$.verdict").value("FRAGILE"))
                .andExpect(jsonPath("$.buckets[0].initialPercent").value(0))
                .andExpect(jsonPath("$.buckets[1].total").value(3))
                .andExpect(jsonPath("$.buckets[1].correctPercent").value(0))
                .andExpect(jsonPath("$.buckets[1].partial").value(1))
                .andExpect(jsonPath("$.buckets[1].initialPercent").value(100))
                .andExpect(jsonPath("$.forgotten.length()").value(3))
                .andExpect(jsonPath("$.forgotten[0].questionId").value("JAVA-01-Q3"))
                .andExpect(jsonPath("$.forgotten[0].outcome").value("PARTIAL"))
                .andExpect(jsonPath("$.forgotten[0].delayDays").value(6))
                .andExpect(jsonPath("$.forgotten[0].prompt").isNotEmpty())
                .andExpect(jsonPath("$.forgotten[2].questionId").value("JAVA-01-Q1"))
                .andExpect(jsonPath("$.retainedLabs[0].delayDays").value(8));
    }

    private void initial(String question, int score) {
        jdbcTemplate.update(
                "insert into quiz_answer_log (id, attempt_id, question_id, selected_choice, score, answered_at) values (?, ?, ?, 0, ?, ?)",
                UUID.randomUUID(), attempt, question, score, Timestamp.from(COMPLETED_AT.minus(30, ChronoUnit.MINUTES)
                        .plusSeconds(jdbcTemplate.queryForObject("select count(*) from quiz_answer_log", Integer.class))));
    }

    private void recall(String question, int score, int daysAfter) {
        Instant answeredAt = COMPLETED_AT.plus(daysAfter, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS);
        UUID reviewId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                insert into review_item (id, attempt_id, lab_id, due_at, reason, status, repetition_stage, created_at)
                values (?, ?, 'JAVA-01', ?, 'Test', 'COMPLETED', 0, ?)
                """, reviewId, attempt, Timestamp.from(answeredAt), Timestamp.from(answeredAt));
        jdbcTemplate.update(
                """
                insert into review_answer (id, review_id, question_id, score, outcome, confidence, answered_at)
                values (?, ?, ?, ?, 'WRONG', 2, ?)
                """, UUID.randomUUID(), reviewId, question, score, Timestamp.from(answeredAt));
    }
}
