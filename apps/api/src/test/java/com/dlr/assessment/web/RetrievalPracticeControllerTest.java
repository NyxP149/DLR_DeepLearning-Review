package com.dlr.assessment.web;

import com.dlr.catalog.application.LabCatalog;
import com.dlr.catalog.domain.LabContent;
import com.dlr.execution.application.CodeRunner;
import com.dlr.execution.domain.ExecutionResult;
import com.dlr.execution.domain.ExecutionStatus;
import com.dlr.execution.domain.Submission;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pratique de récupération : score V2, correction révélée après la réponse et révisions par question de rappel. */
@SpringBootTest
@AutoConfigureMockMvc
class RetrievalPracticeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired LabCatalog catalog;
    @MockitoBean CodeRunner codeRunner;

    private LabContent lab;
    private LabContent.QuizQuestion choiceQuestion;
    private LabContent.QuizQuestion textQuestion;

    @BeforeEach
    void prepare() {
        jdbcTemplate.update("delete from review_item");
        lab = catalog.findByCode("JAVA-01").orElseThrow();
        choiceQuestion = lab.quiz().stream().filter(q -> "SINGLE_CHOICE".equals(q.type())).findFirst().orElseThrow();
        textQuestion = lab.quiz().stream().filter(q -> "FREE_TEXT".equals(q.type())).findFirst().orElseThrow();
        when(codeRunner.run(any())).thenAnswer(invocation -> {
            Submission submission = invocation.getArgument(0, Submission.class);
            return new ExecutionResult(UUID.randomUUID(), submission.id(), ExecutionStatus.SUCCESS, 0,
                    "DLR Java Lab 1\nSource: Main.java\nBytecode: Main.class\nExécution: JVM\n", "", 50, Instant.now());
        });
    }

    @Test
    void scoresANewAttemptWithV2FromTheFirstAnswerAndTheHintsUsed() throws Exception {
        String attemptId = startAndRunAttempt();
        int wrong = (choiceQuestion.correctChoice() + 1) % choiceQuestion.choices().size();

        // Première réponse fausse, puis corrigée après avoir lu l'explication : seule la première compte.
        answer(attemptId, choiceQuestion.code(), "{\"selectedChoice\":" + wrong + ",\"confidence\":3}");
        answer(attemptId, choiceQuestion.code(), "{\"selectedChoice\":" + choiceQuestion.correctChoice() + ",\"confidence\":3}");
        answer(attemptId, textQuestion.code(), "{\"answerText\":\"" + String.join(" ", textQuestion.expectedKeywords()) + "\"}");
        // Les autres questions à choix reçoivent une première réponse fausse : le quiz reste à 0.
        long otherChoices = lab.quiz().stream()
                .filter(q -> "SINGLE_CHOICE".equals(q.type()) && !q.code().equals(choiceQuestion.code())).count();
        for (var question : lab.quiz()) {
            if ("SINGLE_CHOICE".equals(question.type()) && !question.code().equals(choiceQuestion.code())) {
                int other = (question.correctChoice() + 1) % question.choices().size();
                answer(attemptId, question.code(), "{\"selectedChoice\":" + other + "}");
            }
        }
        checklistAllDone(attemptId);
        jdbcTemplate.update("insert into attempt_hint (id, attempt_id, level, created_at) values (?, ?, 2, ?)",
                UUID.randomUUID(), UUID.fromString(attemptId), Timestamp.from(Instant.now()));

        // V2 : tests 30 + quiz 0 + pratique 65 % de 20 + explication 20 + checklist 5 = 68
        mockMvc.perform(post("/api/attempts/{id}/complete", attemptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.breakdown.version").value("V2"))
                .andExpect(jsonPath("$.breakdown.quiz").value(0))
                .andExpect(jsonPath("$.breakdown.practice").value(65))
                .andExpect(jsonPath("$.attempt.score").value(68.0));
        assertThat(jdbcTemplate.queryForObject("select count(*) from quiz_answer_log where attempt_id = ?", Integer.class,
                UUID.fromString(attemptId))).isEqualTo(3 + (int) otherChoices);
    }

    @Test
    void ignoresDraftsSavedWhileTypingWhenScoringTheFirstAnswer() throws Exception {
        String attemptId = startAndRunAttempt();
        for (var question : lab.quiz()) {
            if ("SINGLE_CHOICE".equals(question.type())) {
                answer(attemptId, question.code(), "{\"selectedChoice\":" + question.correctChoice() + "}");
            }
        }
        // La sauvegarde automatique enregistre des textes partiels avant l'envoi réel de la réponse complète.
        answer(attemptId, textQuestion.code(), "{\"answerText\":\"Le\",\"draft\":true}");
        answer(attemptId, textQuestion.code(), "{\"answerText\":\"Le contrat\",\"draft\":true}");
        answer(attemptId, textQuestion.code(),
                "{\"answerText\":\"" + String.join(" ", textQuestion.expectedKeywords()) + "\",\"confidence\":2}");
        checklistAllDone(attemptId);

        mockMvc.perform(post("/api/attempts/{id}/complete", attemptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.breakdown.connections").value(100));
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from quiz_answer_log where attempt_id = ? and question_id = ?", Integer.class,
                UUID.fromString(attemptId), textQuestion.code())).isEqualTo(1);
        // La réponse reste enregistrée pour être restaurée au rechargement.
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from quiz_answer where attempt_id = ? and question_id = ?", Integer.class,
                UUID.fromString(attemptId), textQuestion.code())).isEqualTo(1);
    }

    @Test
    void keepsTheV1RulesWhenAnAlreadyScoredAttemptIsRecalculated() throws Exception {
        String attemptId = startAndRunAttempt();
        for (var question : lab.quiz()) {
            if ("SINGLE_CHOICE".equals(question.type())) {
                answer(attemptId, question.code(), "{\"selectedChoice\":" + question.correctChoice() + "}");
            }
        }
        answer(attemptId, textQuestion.code(), "{\"answerText\":\"" + String.join(" ", textQuestion.expectedKeywords()) + "\"}");
        checklistAllDone(attemptId);
        jdbcTemplate.update("insert into attempt_hint (id, attempt_id, level, created_at) values (?, ?, 3, ?)",
                UUID.randomUUID(), UUID.fromString(attemptId), Timestamp.from(Instant.now()));
        mockMvc.perform(post("/api/attempts/{id}/complete", attemptId))
                .andExpect(jsonPath("$.breakdown.version").value("V2"))
                .andExpect(jsonPath("$.breakdown.practice").value(40));

        jdbcTemplate.update("update attempt set score_version = 'V1' where id = ?", UUID.fromString(attemptId));

        mockMvc.perform(post("/api/attempts/{id}/complete", attemptId))
                .andExpect(jsonPath("$.breakdown.version").value("V1"))
                .andExpect(jsonPath("$.breakdown.practice").value(100))
                .andExpect(jsonPath("$.attempt.score").value(100));
    }

    @Test
    void revealsTheCorrectionAfterTheAnswerAndRejectsAnInvalidConfidence() throws Exception {
        String attemptId = startAndRunAttempt();
        int wrong = (choiceQuestion.correctChoice() + 1) % choiceQuestion.choices().size();

        mockMvc.perform(put("/api/attempts/{id}/quiz/{q}", attemptId, choiceQuestion.code())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selectedChoice\":" + wrong + ",\"confidence\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false))
                .andExpect(jsonPath("$.correctChoice").value(choiceQuestion.correctChoice()));

        mockMvc.perform(put("/api/attempts/{id}/quiz/{q}", attemptId, textQuestion.code())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answerText\":\"je ne sais pas\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingIdeas.length()").value(textQuestion.expectedKeywords().size()));

        mockMvc.perform(put("/api/attempts/{id}/quiz/{q}", attemptId, choiceQuestion.code())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selectedChoice\":0,\"confidence\":4}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void asksARecallQuestionWithoutRevealingTheAnswer() throws Exception {
        UUID reviewId = insertReview(0);

        mockMvc.perform(get("/api/reviews/{id}/question", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value(choiceQuestion.code()))
                .andExpect(jsonPath("$.choices.length()").value(choiceQuestion.choices().size()))
                .andExpect(jsonPath("$.correctChoice").doesNotExist())
                .andExpect(jsonPath("$.explanation").doesNotExist());

        mockMvc.perform(post("/api/reviews/{id}/answer", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\":\"" + textQuestion.code() + "\",\"answerText\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void advancesToTheNextIntervalWhenTheAnswerIsCorrectAndConfident() throws Exception {
        UUID reviewId = insertReview(0);

        String body = answerReview(reviewId, choiceQuestion.code(), choiceQuestion.correctChoice(), 3)
                .andExpect(jsonPath("$.outcome").value("CORRECT"))
                .andExpect(jsonPath("$.nextReview.stage").value(1))
                .andReturn().getResponse().getContentAsString();

        assertThat(daysUntil(objectMapper.readTree(body).get("nextReview"))).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("select count(*) from review_answer where review_id = ?", Integer.class, reviewId))
                .isEqualTo(1);
    }

    @Test
    void repeatsTheSameIntervalWhenTheCorrectAnswerWasHesitant() throws Exception {
        UUID reviewId = insertReview(2);

        String body = answerReview(reviewId, choiceQuestion.code(), choiceQuestion.correctChoice(), 1)
                .andExpect(jsonPath("$.outcome").value("CORRECT"))
                .andExpect(jsonPath("$.nextReview.stage").value(2))
                .andReturn().getResponse().getContentAsString();

        assertThat(daysUntil(objectMapper.readTree(body).get("nextReview"))).isEqualTo(7);
    }

    @Test
    void reviewsTomorrowAndExplainsWhenAConfidentAnswerIsWrong() throws Exception {
        UUID reviewId = insertReview(2);
        int wrong = (choiceQuestion.correctChoice() + 1) % choiceQuestion.choices().size();

        String body = answerReview(reviewId, choiceQuestion.code(), wrong, 3)
                .andExpect(jsonPath("$.outcome").value("WRONG"))
                .andExpect(jsonPath("$.correctChoice").value(choiceQuestion.correctChoice()))
                .andExpect(jsonPath("$.calibration").value(org.hamcrest.Matchers.containsString("sûr")))
                .andExpect(jsonPath("$.nextReview.stage").value(2))
                .andReturn().getResponse().getContentAsString();

        assertThat(daysUntil(objectMapper.readTree(body).get("nextReview"))).isEqualTo(1);
    }

    @Test
    void reviewsSoonerWhenAFreeTextAnswerIsOnlyPartiallyRight() throws Exception {
        // Chaque révision terminée sur ce labo fait avancer d'une question : on en termine autant qu'il faut pour atteindre le texte libre.
        for (int i = 0; i < lab.quiz().indexOf(textQuestion); i++) {
            UUID done = insertReview(0);
            jdbcTemplate.update("update review_item set status = 'COMPLETED' where id = ?", done);
        }
        UUID reviewId = insertReview(2);
        List<String> keywords = textQuestion.expectedKeywords();
        String half = String.join(" ", keywords.subList(0, Math.max(1, keywords.size() / 2)));

        mockMvc.perform(get("/api/reviews/{id}/question", reviewId))
                .andExpect(jsonPath("$.questionId").value(textQuestion.code()));
        String body = mockMvc.perform(post("/api/reviews/{id}/answer", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\":\"" + textQuestion.code() + "\",\"answerText\":\"" + half + "\",\"confidence\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outcome").value("PARTIAL"))
                .andExpect(jsonPath("$.nextReview.stage").value(2))
                .andReturn().getResponse().getContentAsString();

        assertThat(daysUntil(objectMapper.readTree(body).get("nextReview"))).isEqualTo(3);
    }

    private org.springframework.test.web.servlet.ResultActions answerReview(
            UUID reviewId, String questionId, int choice, int confidence) throws Exception {
        return mockMvc.perform(post("/api/reviews/{id}/answer", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\":\"" + questionId + "\",\"selectedChoice\":" + choice
                                + ",\"confidence\":" + confidence + "}"))
                .andExpect(status().isOk());
    }

    private long daysUntil(JsonNode review) {
        return Math.round(Duration.between(Instant.now(), Instant.parse(review.get("dueAt").asText())).toMinutes() / 1440.0);
    }

    private UUID insertReview(int stage) {
        UUID attemptId = UUID.randomUUID();
        Instant now = Instant.now();
        jdbcTemplate.update("insert into attempt (id, lab_id, started_at, status) values (?, 'JAVA-01', ?, 'IN_PROGRESS')",
                attemptId, Timestamp.from(now.minusSeconds(3600)));
        UUID reviewId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                insert into review_item (id, attempt_id, lab_id, due_at, reason, status, repetition_stage, created_at)
                values (?, ?, 'JAVA-01', ?, 'Test', 'PENDING', ?, ?)
                """,
                reviewId, attemptId, Timestamp.from(now.minusSeconds(60)), stage, Timestamp.from(now));
        return reviewId;
    }

    private String startAndRunAttempt() throws Exception {
        String attempt = mockMvc.perform(post("/api/labs/JAVA-01/attempts")).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String attemptId = objectMapper.readTree(attempt).get("id").asText();
        String submission = mockMvc.perform(post("/api/attempts/{id}/submissions", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"language\":\"JAVA\",\"sourceCode\":\"public class Main {}\",\"origin\":\"EDITOR\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        mockMvc.perform(post("/api/submissions/{id}/run", objectMapper.readTree(submission).get("id").asText()))
                .andExpect(status().isOk());
        return attemptId;
    }

    private void answer(String attemptId, String questionId, String body) throws Exception {
        mockMvc.perform(put("/api/attempts/{id}/quiz/{q}", attemptId, questionId)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
    }

    private void checklistAllDone(String attemptId) throws Exception {
        String all = String.join(",", java.util.Collections.nCopies(lab.checklist().size(), "true"));
        mockMvc.perform(put("/api/attempts/{id}/checklist", attemptId)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"completed\":[" + all + "]}"))
                .andExpect(status().isOk());
    }
}
