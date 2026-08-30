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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CodeRunner codeRunner;

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
                    "DLR Java Lab 1\n", "", 120, Instant.now());
        });

        String attemptId = startAttempt();
        String submissionId = submit(attemptId);
        mockMvc.perform(post("/api/submissions/{id}/run", submissionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        answerChoice(attemptId, 1);
        answerFreeText(attemptId, "Le compilateur compile le code source en bytecode, ensuite la JVM l'exécute.");

        mockMvc.perform(put("/api/attempts/{id}/checklist", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completed\":[true,true,true]}"))
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
        answerFreeText(attemptId, "Le code source est transformé.");
        mockMvc.perform(put("/api/attempts/{id}/checklist", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completed\":[true,false,false]}"))
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
                        .content("{\"completed\":[true,false,true]}"))
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

    private void answerFreeText(String attemptId, String answer) throws Exception {
        JsonNode request = objectMapper.createObjectNode().put("answerText", answer);
        mockMvc.perform(put("/api/attempts/{id}/quiz/JAVA-01-Q2", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk());
    }
}
