package com.dlr.adaptation.web;

import com.dlr.catalog.application.LabCatalog;
import com.dlr.learning.application.LabResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Gain du quiz de départ agrégé : réponses d'avant le cours contre première réponse du quiz final. */
@SpringBootTest
@AutoConfigureMockMvc
class PretestGainControllerTest {

    private static final List<String> LABS = List.of("JAVA-01", "JAVA-02", "JAVA-03", "JAVA-04");

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired LabResetService resetService;
    @Autowired LabCatalog catalog;

    @BeforeEach
    void reset() {
        LABS.forEach(resetService::reset);
    }

    @Test
    void asksForMoreLabsBeforeJudgingAndCountsSkippedAndUnfinishedPretests() throws Exception {
        seed("JAVA-01", new Boolean[]{false, false, false}, new boolean[]{true, true, true});
        // Quiz de départ répondu mais laboratoire pas terminé : pas encore comparable.
        answerPretest("JAVA-02", new Boolean[]{true, true, true});
        attempt("JAVA-02", "IN_PROGRESS");
        jdbcTemplate.update("insert into lab_pretest (lab_code, status, completed_at) values ('JAVA-03', 'SKIPPED', ?)",
                Timestamp.from(Instant.now()));

        mockMvc.perform(get("/api/adaptation/pretest-gain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verdict").value("INSUFFICIENT_DATA"))
                .andExpect(jsonPath("$.pretestsAnswered").value(2))
                .andExpect(jsonPath("$.pretestsSkipped").value(1))
                .andExpect(jsonPath("$.labsCompared").value(1))
                .andExpect(jsonPath("$.questions").value(3));
    }

    @Test
    void measuresWhatTheCourseTaughtAcrossLabs() throws Exception {
        seed("JAVA-01", new Boolean[]{false, false, false}, new boolean[]{true, true, true});
        seed("JAVA-02", new Boolean[]{true, false, null}, new boolean[]{true, true, true});
        seed("JAVA-03", new Boolean[]{false, false, false}, new boolean[]{true, true, false});

        mockMvc.perform(get("/api/adaptation/pretest-gain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verdict").value("STRONG_GAIN"))
                .andExpect(jsonPath("$.labsCompared").value(3))
                .andExpect(jsonPath("$.questions").value(9))
                .andExpect(jsonPath("$.startCorrectPercent").value(11))
                .andExpect(jsonPath("$.endCorrectPercent").value(89))
                .andExpect(jsonPath("$.gainPoints").value(78))
                .andExpect(jsonPath("$.learned").value(7))
                .andExpect(jsonPath("$.lost").value(0))
                .andExpect(jsonPath("$.unknownAtStartPercent").value(11))
                .andExpect(jsonPath("$.labs.length()").value(3))
                .andExpect(jsonPath("$.labs[0].labCode").value("JAVA-01"))
                .andExpect(jsonPath("$.labs[0].gainPoints").value(100))
                .andExpect(jsonPath("$.labs[0].title").isNotEmpty())
                .andExpect(jsonPath("$.labs[1].labCode").value("JAVA-02"))
                .andExpect(jsonPath("$.labs[2].labCode").value("JAVA-03"));
    }

    @Test
    void recognizesNotionsAlreadyKnownBeforeTheCourse() throws Exception {
        for (String lab : List.of("JAVA-01", "JAVA-02", "JAVA-03")) {
            seed(lab, new Boolean[]{true, true, true}, new boolean[]{true, true, true});
        }

        mockMvc.perform(get("/api/adaptation/pretest-gain"))
                .andExpect(jsonPath("$.verdict").value("ALREADY_KNOWN"))
                .andExpect(jsonPath("$.startCorrectPercent").value(100))
                .andExpect(jsonPath("$.gainPoints").value(0));
    }

    @Test
    void flagsALowGainAndCountsWhatWasKnownThenLost() throws Exception {
        for (String lab : List.of("JAVA-01", "JAVA-02", "JAVA-03")) {
            seed(lab, new Boolean[]{true, true, false}, new boolean[]{true, false, false});
        }

        mockMvc.perform(get("/api/adaptation/pretest-gain"))
                .andExpect(jsonPath("$.verdict").value("LOW_GAIN"))
                .andExpect(jsonPath("$.startCorrectPercent").value(67))
                .andExpect(jsonPath("$.endCorrectPercent").value(33))
                .andExpect(jsonPath("$.gainPoints").value(-34))
                .andExpect(jsonPath("$.lost").value(3))
                .andExpect(jsonPath("$.learned").value(0));
    }

    /** Quiz de départ répondu puis laboratoire terminé ; `start` : juste, faux ou null (« Je ne sais pas »). */
    private void seed(String labCode, Boolean[] start, boolean[] end) {
        answerPretest(labCode, start);
        UUID attempt = attempt(labCode, "COMPLETED");
        var questions = choiceQuestions(labCode);
        for (int index = 0; index < questions.size(); index++) {
            jdbcTemplate.update(
                    "insert into quiz_answer_log (id, attempt_id, question_id, selected_choice, score, answered_at) values (?, ?, ?, 0, ?, ?)",
                    UUID.randomUUID(), attempt, questions.get(index).code(), end[index] ? 100 : 0,
                    Timestamp.from(Instant.parse("2026-01-01T10:00:00Z").plusSeconds(index)));
        }
    }

    private void answerPretest(String labCode, Boolean[] start) {
        var questions = choiceQuestions(labCode);
        jdbcTemplate.update("insert into lab_pretest (lab_code, status, completed_at) values (?, 'ANSWERED', ?)",
                labCode, Timestamp.from(Instant.now()));
        for (int index = 0; index < questions.size(); index++) {
            var question = questions.get(index);
            Integer choice = start[index] == null ? null
                    : start[index] ? question.correctChoice() : (question.correctChoice() + 1) % question.choices().size();
            jdbcTemplate.update(
                    "insert into lab_pretest_answer (id, lab_code, question_id, selected_choice, answered_at) values (?, ?, ?, ?, ?)",
                    UUID.randomUUID(), labCode, question.code(), choice, Timestamp.from(Instant.now()));
        }
    }

    private List<com.dlr.catalog.domain.LabContent.QuizQuestion> choiceQuestions(String labCode) {
        return catalog.findByCode(labCode).orElseThrow().quiz().stream()
                .filter(question -> "SINGLE_CHOICE".equals(question.type())).limit(3).toList();
    }

    private UUID attempt(String labCode, String status) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("insert into attempt (id, lab_id, started_at, status) values (?, ?, ?, ?)",
                id, labCode, Timestamp.from(Instant.now()), status);
        return id;
    }
}
