package com.dlr.assessment.web;

import com.dlr.catalog.application.LabCatalog;
import com.dlr.learning.application.LabResetService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Quiz de départ : questions avant le cours, sans correction, comparées au quiz final une fois le labo terminé. */
@SpringBootTest
@AutoConfigureMockMvc
class PretestControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired LabResetService resetService;
    @Autowired LabCatalog catalog;

    @BeforeEach
    void reset() {
        resetService.reset("JAVA-01");
    }

    @Test
    void offersTheFirstThreeChoiceQuestionsWithoutRevealingAnyAnswer() throws Exception {
        mockMvc.perform(get("/api/labs/JAVA-01/pretest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.questionCodes.length()").value(3))
                .andExpect(jsonPath("$.questionCodes[0]").value("JAVA-01-Q1"))
                .andExpect(jsonPath("$.questionCodes[2]").value("JAVA-01-Q3"))
                .andExpect(jsonPath("$.gain").doesNotExist())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("correct"))));
    }

    @Test
    void storesTheAnswersOnceAndThenStopsOfferingThePretest() throws Exception {
        mockMvc.perform(post("/api/labs/JAVA-01/pretest").contentType(MediaType.APPLICATION_JSON).content(answers(1, null, 0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ANSWERED"));

        Integer stored = jdbcTemplate.queryForObject("select count(*) from lab_pretest_answer where lab_code = 'JAVA-01'", Integer.class);
        org.junit.jupiter.api.Assertions.assertEquals(3, stored);
        mockMvc.perform(post("/api/labs/JAVA-01/pretest").contentType(MediaType.APPLICATION_JSON).content(answers(1, 1, 1)))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/labs/JAVA-01/pretest/skip")).andExpect(status().isConflict());
        mockMvc.perform(get("/api/labs/JAVA-01/pretest")).andExpect(jsonPath("$.status").value("ANSWERED"));
    }

    @Test
    void letsTheLearnerSkipThePretest() throws Exception {
        mockMvc.perform(post("/api/labs/JAVA-01/pretest/skip"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SKIPPED"));
        mockMvc.perform(get("/api/labs/JAVA-01/pretest")).andExpect(jsonPath("$.status").value("SKIPPED"));
    }

    @Test
    void rejectsIncompleteOrInvalidAnswers() throws Exception {
        mockMvc.perform(post("/api/labs/JAVA-01/pretest").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\":[{\"questionId\":\"JAVA-01-Q1\",\"selectedChoice\":1}]}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/labs/JAVA-01/pretest").contentType(MediaType.APPLICATION_JSON).content(answers(1, 9, 0)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/labs/JAVA-01/pretest").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\":[{\"questionId\":\"JAVA-01-Q1\",\"selectedChoice\":1},"
                                + "{\"questionId\":\"JAVA-01-Q1\",\"selectedChoice\":1},{\"questionId\":\"JAVA-01-Q3\",\"selectedChoice\":0}]}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/labs/JAVA-01/pretest")).andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void isNoLongerOfferedOnceTheQuizHasBeenStarted() throws Exception {
        UUID attempt = attempt("IN_PROGRESS");
        quizAnswer(attempt, "JAVA-01-Q1", 100);

        mockMvc.perform(get("/api/labs/JAVA-01/pretest")).andExpect(jsonPath("$.status").value("NOT_APPLICABLE"));
        mockMvc.perform(post("/api/labs/JAVA-01/pretest/skip")).andExpect(status().isConflict());
    }

    @Test
    void comparesTheStartResultWithTheFirstFinalAnswerOnceTheLabIsCompleted() throws Exception {
        var quiz = catalog.findByCode("JAVA-01").orElseThrow().quiz();
        int right = quiz.get(0).correctChoice();
        int wrong = (quiz.get(2).correctChoice() + 1) % 4;
        mockMvc.perform(post("/api/labs/JAVA-01/pretest").contentType(MediaType.APPLICATION_JSON).content(answers(right, null, wrong)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gain").doesNotExist());

        UUID attempt = attempt("COMPLETED");
        // Q3 a été corrigée après coup : seule la première réponse (fausse) compte pour la comparaison.
        quizAnswer(attempt, "JAVA-01-Q1", 100);
        quizAnswer(attempt, "JAVA-01-Q2", 100);
        quizAnswer(attempt, "JAVA-01-Q3", 100);
        firstAnswer(attempt, "JAVA-01-Q1", 100);
        firstAnswer(attempt, "JAVA-01-Q2", 100);
        firstAnswer(attempt, "JAVA-01-Q3", 0);

        mockMvc.perform(get("/api/labs/JAVA-01/pretest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gain.total").value(3))
                .andExpect(jsonPath("$.gain.correctAtStart").value(1))
                .andExpect(jsonPath("$.gain.correctAtEnd").value(2));
    }

    @Test
    void isOfferedAgainAfterTheLabIsReset() throws Exception {
        mockMvc.perform(post("/api/labs/JAVA-01/pretest/skip")).andExpect(status().isOk());

        resetService.reset("JAVA-01");

        mockMvc.perform(get("/api/labs/JAVA-01/pretest")).andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void answersNotFoundForAnUnknownLab() throws Exception {
        mockMvc.perform(get("/api/labs/NOPE-99/pretest")).andExpect(status().isNotFound());
    }

    private String answers(Integer q1, Integer q2, Integer q3) {
        return "{\"answers\":[{\"questionId\":\"JAVA-01-Q1\",\"selectedChoice\":" + q1 + "},"
                + "{\"questionId\":\"JAVA-01-Q2\",\"selectedChoice\":" + q2 + "},"
                + "{\"questionId\":\"JAVA-01-Q3\",\"selectedChoice\":" + q3 + "}]}";
    }

    private UUID attempt(String status) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("insert into attempt (id, lab_id, started_at, status) values (?, 'JAVA-01', ?, ?)",
                id, Timestamp.from(Instant.now()), status);
        return id;
    }

    private void quizAnswer(UUID attempt, String question, int score) {
        jdbcTemplate.update(
                "insert into quiz_answer (id, attempt_id, question_id, selected_choice, score, feedback, updated_at) values (?, ?, ?, 0, ?, 'Test', ?)",
                UUID.randomUUID(), attempt, question, score, Timestamp.from(Instant.now()));
    }

    private void firstAnswer(UUID attempt, String question, int score) {
        jdbcTemplate.update(
                "insert into quiz_answer_log (id, attempt_id, question_id, selected_choice, score, answered_at) values (?, ?, ?, 0, ?, ?)",
                UUID.randomUUID(), attempt, question, score, Timestamp.from(Instant.parse("2026-01-01T10:00:00Z")));
    }
}
