package com.dlr.learning.web;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LabResetControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void removesAllLearningDataForOnlyTheSelectedLab() throws Exception {
        Instant now = Instant.now();
        UUID attemptId = UUID.randomUUID();
        UUID submissionId = UUID.randomUUID();

        jdbcTemplate.update(
                "insert into attempt (id, lab_id, started_at, status) values (?, ?, ?, ?)",
                attemptId, "JAVA-06", Timestamp.from(now), "IN_PROGRESS");
        jdbcTemplate.update(
                "insert into submission (id, attempt_id, language, source_code, origin, created_at) values (?, ?, ?, ?, ?, ?)",
                submissionId, attemptId, "JAVA", "class Main {}", "EDITOR", Timestamp.from(now));
        jdbcTemplate.update(
                "insert into execution_result (id, submission_id, status, exit_code, standard_output, error_output, duration_ms, created_at) values (?, ?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), submissionId, "SUCCESS", 0, "ok", "", 10, Timestamp.from(now));
        jdbcTemplate.update(
                "insert into quiz_answer (id, attempt_id, question_id, selected_choice, score, feedback, updated_at) values (?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), attemptId, "JAVA-06-Q1", 1, 100, "ok", Timestamp.from(now));
        jdbcTemplate.update(
                "insert into attempt_checklist (attempt_id, completed_items, total_items, updated_at, completed_state) values (?, ?, ?, ?, ?)",
                attemptId, 1, 3, Timestamp.from(now), "true,false,false");
        jdbcTemplate.update(
                "insert into review_item (id, attempt_id, lab_id, due_at, reason, status, created_at) values (?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), attemptId, "JAVA-06", Timestamp.from(now), "test", "PENDING", Timestamp.from(now));

        mockMvc.perform(delete("/api/labs/JAVA-06/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labCode").value("JAVA-06"))
                .andExpect(jsonPath("$.attemptsDeleted").value(1))
                .andExpect(jsonPath("$.submissionsDeleted").value(1))
                .andExpect(jsonPath("$.executionsDeleted").value(1))
                .andExpect(jsonPath("$.reviewsDeleted").value(1));

        assertThat(count("attempt", "id", attemptId)).isZero();
        assertThat(count("submission", "attempt_id", attemptId)).isZero();
        assertThat(count("quiz_answer", "attempt_id", attemptId)).isZero();
        assertThat(count("attempt_checklist", "attempt_id", attemptId)).isZero();
        assertThat(count("review_item", "attempt_id", attemptId)).isZero();
    }

    private int count(String table, String column, UUID id) {
        return jdbcTemplate.queryForObject(
                "select count(*) from " + table + " where " + column + " = ?", Integer.class, id);
    }
}
