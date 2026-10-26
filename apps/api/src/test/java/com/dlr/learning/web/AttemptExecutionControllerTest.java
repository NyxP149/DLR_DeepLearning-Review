package com.dlr.learning.web;

import com.dlr.execution.application.CodeRunner;
import com.dlr.execution.domain.ExecutionResult;
import com.dlr.execution.domain.ExecutionStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AttemptExecutionControllerTest {
    private static final String JAVA_01_OUTPUT =
            "DLR Java Lab 1\nSource: Main.java\nBytecode: Main.class\nExécution: JVM\n";


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @MockitoBean
    private CodeRunner codeRunner;

    @org.junit.jupiter.api.BeforeEach
    void clearReviews() {
        jdbcTemplate.update("delete from review_item");
    }

    @Test
    void startsAndReloadsAnAttempt() throws Exception {
        String body = mockMvc.perform(post("/api/labs/JAVA-01/attempts"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.labCode").value("JAVA-01"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID attemptId = UUID.fromString(objectMapper.readTree(body).get("id").asText());

        mockMvc.perform(get("/api/attempts/{id}", attemptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(attemptId.toString()));
    }

    @Test
    void savesASubmissionWithoutReturningItsSourceCode() throws Exception {
        String attemptBody = mockMvc.perform(post("/api/labs/JAVA-01/attempts"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String attemptId = objectMapper.readTree(attemptBody).get("id").asText();

        JsonNode request = objectMapper.createObjectNode()
                .put("language", "JAVA")
                .put("sourceCode", "public class Main { public static void main(String[] args) {} }")
                .put("origin", "EDITOR");

        mockMvc.perform(post("/api/attempts/{id}/submissions", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attemptId").value(attemptId))
                .andExpect(jsonPath("$.language").value("JAVA"))
                .andExpect(jsonPath("$.sourceCode").doesNotExist());
    }

    @Test
    void acceptsTheLabLanguageAndRejectsAMismatchedSubmission() throws Exception {
        String attemptBody = mockMvc.perform(post("/api/labs/PYTHON-01/attempts"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String attemptId = objectMapper.readTree(attemptBody).get("id").asText();

        mockMvc.perform(post("/api/attempts/{id}/submissions", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"language\":\"PYTHON\",\"sourceCode\":\"print('ok')\",\"origin\":\"EDITOR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.language").value("PYTHON"));

        mockMvc.perform(post("/api/attempts/{id}/submissions", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"language\":\"JAVA\",\"sourceCode\":\"class Main {}\",\"origin\":\"EDITOR\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("PYTHON")));
    }

    @Test
    void blocksASequencedPythonLabUntilItsPrerequisiteIsValidated() throws Exception {
        mockMvc.perform(post("/api/labs/PYTHON-06/attempts"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("LAB_LOCKED"))
                .andExpect(jsonPath("$.missingPrerequisites[0]").value("PYTHON-05"));
    }

    @Test
    void refusesASubmissionForAnUnknownAttempt() throws Exception {
        JsonNode request = objectMapper.createObjectNode()
                .put("language", "JAVA")
                .put("sourceCode", "public class Main {}")
                .put("origin", "EDITOR");

        mockMvc.perform(post("/api/attempts/{id}/submissions", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("ATTEMPT_NOT_FOUND"));
    }

    @Test
    void completesAFullAttemptWithADeterministicScore() throws Exception {
        when(codeRunner.run(any())).thenAnswer(invocation -> {
            var submission = invocation.getArgument(0, com.dlr.execution.domain.Submission.class);
            return new ExecutionResult(
                    UUID.randomUUID(), submission.id(), ExecutionStatus.SUCCESS, 0,
                    JAVA_01_OUTPUT, "", 120, Instant.now());
        });

        String attemptId = startAttempt();
        String submissionId = submit(attemptId);
        mockMvc.perform(post("/api/submissions/{id}/run", submissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        answerAllCorrect(attemptId);

        mockMvc.perform(put("/api/attempts/{id}/checklist", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completed\":[true,true,true,true]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(100));

        mockMvc.perform(post("/api/attempts/{id}/complete", attemptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attempt.status").value("COMPLETED"))
                .andExpect(jsonPath("$.attempt.score").value(100))
                .andExpect(jsonPath("$.reviewScheduled").value(true));
    }

    @Test
    void schedulesAReviewAndAllowsExplicitContinuationBelowThreshold() throws Exception {
        when(codeRunner.run(any())).thenAnswer(invocation -> {
            var submission = invocation.getArgument(0, com.dlr.execution.domain.Submission.class);
            return new ExecutionResult(
                    UUID.randomUUID(), submission.id(), ExecutionStatus.SUCCESS, 0, "ok", "", 80, Instant.now());
        });

        String attemptId = startAttempt();
        String submissionId = submit(attemptId);
        mockMvc.perform(post("/api/submissions/{id}/run", submissionId)).andExpect(status().isOk());
        answerChoice(attemptId, 0);
        for (var question : new String[] {"JAVA-01-Q2", "JAVA-01-Q3", "JAVA-01-Q4"}) {
            mockMvc.perform(put("/api/attempts/{id}/quiz/{question}", attemptId, question)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"selectedChoice\":0}"))
                    .andExpect(status().isOk());
        }
        answerFreeText(attemptId, "Le code source est transformé.");
        mockMvc.perform(put("/api/attempts/{id}/checklist", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completed\":[true,false,false,false]}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/attempts/{id}/complete", attemptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attempt.status").value("COMPLETED_BELOW_THRESHOLD"))
                .andExpect(jsonPath("$.reviewScheduled").value(true));

        mockMvc.perform(post("/api/attempts/{id}/continue", attemptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.continuedBelowThreshold").value(true));
    }

    @Test
    void recalculatesTheScoreAfterFixingAFailedRunWithoutResettingTheLab() throws Exception {
        when(codeRunner.run(any()))
                .thenAnswer(invocation -> {
                    var submission = invocation.getArgument(0, com.dlr.execution.domain.Submission.class);
                    return new ExecutionResult(
                            UUID.randomUUID(), submission.id(), ExecutionStatus.TESTS_FAILED, 1,
                            "Mauvaise sortie", "Sortie inattendue", 50, Instant.now());
                })
                .thenAnswer(invocation -> {
                    var submission = invocation.getArgument(0, com.dlr.execution.domain.Submission.class);
                    return new ExecutionResult(
                            UUID.randomUUID(), submission.id(), ExecutionStatus.SUCCESS, 0,
                            JAVA_01_OUTPUT, "", 120, Instant.now());
                });

        String attemptId = startAttempt();
        String firstSubmissionId = submit(attemptId);
        mockMvc.perform(post("/api/submissions/{id}/run", firstSubmissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TESTS_FAILED"));

        answerAllCorrect(attemptId);
        mockMvc.perform(put("/api/attempts/{id}/checklist", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completed\":[true,true,true,true]}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/attempts/{id}/complete", attemptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attempt.status").value("COMPLETED_BELOW_THRESHOLD"))
                .andExpect(jsonPath("$.breakdown.tests").value(0))
                .andExpect(jsonPath("$.reviewScheduled").value(true));

        String secondSubmissionId = submit(attemptId);
        mockMvc.perform(post("/api/submissions/{id}/run", secondSubmissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        mockMvc.perform(post("/api/attempts/{id}/complete", attemptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attempt.status").value("COMPLETED"))
                .andExpect(jsonPath("$.attempt.score").value(100))
                .andExpect(jsonPath("$.breakdown.tests").value(100))
                .andExpect(jsonPath("$.reviewScheduled").value(false));

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.attemptId == '" + attemptId + "')]", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[?(@.attemptId == '" + attemptId + "')].reason")
                        .value(org.hamcrest.Matchers.contains(
                                "Consolider les concepts du laboratoire avec la répétition espacée.")));
    }

    @Test
    void doesNotCreateASecondReviewChainWhenTheLabAlreadyHasAPendingReview() throws Exception {
        when(codeRunner.run(any())).thenAnswer(invocation -> {
            var submission = invocation.getArgument(0, com.dlr.execution.domain.Submission.class);
            return new ExecutionResult(
                    UUID.randomUUID(), submission.id(), ExecutionStatus.SUCCESS, 0,
                    JAVA_01_OUTPUT, "", 120, Instant.now());
        });

        String firstAttempt = completeLabOnce();
        String secondAttempt = completeLabOnce();

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.attemptId == '" + firstAttempt + "')]", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[?(@.attemptId == '" + secondAttempt + "')]", org.hamcrest.Matchers.hasSize(0)));
    }

    private String completeLabOnce() throws Exception {
        String attemptId = startAttempt();
        mockMvc.perform(post("/api/submissions/{id}/run", submit(attemptId))).andExpect(status().isOk());
        answerAllCorrect(attemptId);
        mockMvc.perform(put("/api/attempts/{id}/checklist", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completed\":[true,true,true,true]}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/attempts/{id}/complete", attemptId)).andExpect(status().isOk());
        return attemptId;
    }

    @Test
    void reportsAFailedTestWhenTheProgramOutputIsWrong() throws Exception {
        when(codeRunner.run(any())).thenAnswer(invocation -> {
            var submission = invocation.getArgument(0, com.dlr.execution.domain.Submission.class);
            return new ExecutionResult(
                    UUID.randomUUID(), submission.id(), ExecutionStatus.SUCCESS, 0,
                    "Mauvaise sortie", "", 50, Instant.now());
        });

        String attemptId = startAttempt();
        String submissionId = submit(attemptId);
        mockMvc.perform(post("/api/submissions/{id}/run", submissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TESTS_FAILED"))
                .andExpect(jsonPath("$.errorOutput").value(org.hamcrest.Matchers.containsString("Test visible échoué")));
    }

    @Test
    void resumesTheLatestDraftAnswersAndChecklist() throws Exception {
        String attemptId = startAttempt();
        submit(attemptId);
        answerChoice(attemptId, 1);
        mockMvc.perform(put("/api/attempts/{id}/checklist", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completed\":[true,false,true,false]}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/labs/JAVA-01/attempts/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attempt.id").value(attemptId))
                .andExpect(jsonPath("$.sourceCode").value(org.hamcrest.Matchers.containsString("public class Main")))
                .andExpect(jsonPath("$.quizAnswers[0].selectedChoice").value(1))
                .andExpect(jsonPath("$.checklist[0]").value(true))
                .andExpect(jsonPath("$.checklist[1]").value(false))
                .andExpect(jsonPath("$.checklist[2]").value(true));
    }

    private String startAttempt() throws Exception {
        String body = mockMvc.perform(post("/api/labs/JAVA-01/attempts"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asText();
    }

    private String submit(String attemptId) throws Exception {
        String request = """
                {"language":"JAVA","sourceCode":"public class Main { public static void main(String[] args) {} }","origin":"EDITOR"}
                """;
        String body = mockMvc.perform(post("/api/attempts/{id}/submissions", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asText();
    }

    private void answerChoice(String attemptId, int selectedChoice) throws Exception {
        mockMvc.perform(put("/api/attempts/{id}/quiz/JAVA-01-Q1", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selectedChoice\":" + selectedChoice + "}"))
                .andExpect(status().isOk());
    }

    private void answerAllCorrect(String attemptId) throws Exception {
        answerChoice(attemptId, 1);
        for (var question : new Object[][] {{"JAVA-01-Q2", 1}, {"JAVA-01-Q3", 2}, {"JAVA-01-Q4", 1}}) {
            mockMvc.perform(put("/api/attempts/{id}/quiz/{question}", attemptId, question[0])
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"selectedChoice\":" + question[1] + "}"))
                    .andExpect(status().isOk());
        }
        answerFreeText(attemptId, "Le compilateur javac compile le code source en bytecode, ensuite la JVM l'exécute ; une erreur de syntaxe est signalée à la compilation.");
    }

    private void answerFreeText(String attemptId, String answer) throws Exception {
        JsonNode request = objectMapper.createObjectNode().put("answerText", answer);
        mockMvc.perform(put("/api/attempts/{id}/quiz/JAVA-01-Q5", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk());
    }
}
