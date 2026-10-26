package com.dlr.execution.web;

import com.dlr.catalog.application.LabCatalog;
import com.dlr.execution.application.CodeRunner;
import com.dlr.execution.application.ExecutionResultRepository;
import com.dlr.execution.domain.ExecutionResult;
import com.dlr.execution.domain.ExecutionStatus;
import com.dlr.learning.application.LabResetService;
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
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Exercices de transfert : soumissions par exercice, vérification propre à chacun, hors score. */
@SpringBootTest
@AutoConfigureMockMvc
class TransferExerciseControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired LabResetService resetService;
    @Autowired LabCatalog catalog;
    @Autowired ExecutionResultRepository resultRepository;
    @MockitoBean CodeRunner codeRunner;

    private final AtomicReference<String> output = new AtomicReference<>("");
    private UUID attempt;

    @BeforeEach
    void setUp() {
        resetService.reset("JAVA-10");
        attempt = attempt("IN_PROGRESS");
        when(codeRunner.run(any())).thenAnswer(invocation -> {
            var submission = invocation.getArgument(0, com.dlr.execution.domain.Submission.class);
            return new ExecutionResult(UUID.randomUUID(), submission.id(), ExecutionStatus.SUCCESS, 0, output.get(), "", 10, Instant.now());
        });
    }

    @Test
    void checksATransferSubmissionAgainstItsOwnExpectedOutput() throws Exception {
        var lab = catalog.findByCode("JAVA-10").orElseThrow();
        output.set(lab.exercise("JAVA-10-T1").orElseThrow().expectedOutput());
        assertThat(submitAndRun(attempt, "JAVA-10-T1", "code t1")).isEqualTo("SUCCESS");

        // La sortie de l'autre transfert ne valide pas celui-ci.
        output.set(lab.exercise("JAVA-10-T2").orElseThrow().expectedOutput());
        assertThat(submitAndRun(attempt, "JAVA-10-T1", "code t1 bis")).isEqualTo("TESTS_FAILED");
        assertThat(submitAndRun(attempt, "JAVA-10-T2", "code t2")).isEqualTo("SUCCESS");
    }

    @Test
    void aTransferRunNeverChangesTheGuidedExerciseResult() throws Exception {
        var lab = catalog.findByCode("JAVA-10").orElseThrow();
        output.set(lab.guidedExercise().expectedOutput());
        assertThat(submitAndRun(attempt, null, "code guide")).isEqualTo("SUCCESS");
        output.set("n'importe quoi");
        assertThat(submitAndRun(attempt, "JAVA-10-T1", "code transfert")).isEqualTo("TESTS_FAILED");

        assertThat(resultRepository.findLatestByAttemptId(attempt)).get()
                .extracting(ExecutionResult::status).isEqualTo(ExecutionStatus.SUCCESS);
        mockMvc.perform(get("/api/labs/JAVA-10/attempts/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceCode").value("code guide"));
    }

    @Test
    void exposesTheTransferStateInTheWorkspace() throws Exception {
        mockMvc.perform(get("/api/labs/JAVA-10/attempts/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transfers.length()").value(2))
                .andExpect(jsonPath("$.transfers[0].exerciseCode").value("JAVA-10-T1"))
                .andExpect(jsonPath("$.transfers[0].passed").value(false))
                .andExpect(jsonPath("$.transfers[0].sourceCode").doesNotExist());

        output.set(catalog.findByCode("JAVA-10").orElseThrow().exercise("JAVA-10-T1").orElseThrow().expectedOutput());
        submitAndRun(attempt, "JAVA-10-T1", "ma solution");

        mockMvc.perform(get("/api/labs/JAVA-10/attempts/current"))
                .andExpect(jsonPath("$.transfers[0].passed").value(true))
                .andExpect(jsonPath("$.transfers[0].sourceCode").value("ma solution"))
                .andExpect(jsonPath("$.transfers[1].passed").value(false));
    }

    @Test
    void rejectsAnUnknownExerciseAndAcceptsTheGuidedOne() throws Exception {
        mockMvc.perform(post("/api/attempts/" + attempt + "/submissions").contentType(MediaType.APPLICATION_JSON)
                        .content(submission("JAVA-10-X9", "code")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/attempts/" + attempt + "/submissions").contentType(MediaType.APPLICATION_JSON)
                        .content(submission("JAVA-10-E1", "code")))
                .andExpect(status().isCreated());
        Integer transfers = jdbcTemplate.queryForObject(
                "select count(*) from submission where attempt_id = ? and exercise_code is not null", Integer.class, attempt);
        assertThat(transfers).isZero();
    }

    @Test
    void feedsTheTransferIndicatorOfTheCoach() throws Exception {
        UUID completed = attempt("COMPLETED");
        output.set(catalog.findByCode("JAVA-10").orElseThrow().exercise("JAVA-10-T2").orElseThrow().expectedOutput());
        submitAndRun(completed, "JAVA-10-T2", "code");

        // Tous les labos ont désormais un transfert : le total dépend des labos terminés présents en base.
        int available = jdbcTemplate.queryForList(
                        "select distinct lab_id from attempt where status in ('COMPLETED', 'COMPLETED_BELOW_THRESHOLD')", String.class)
                .stream()
                .mapToInt(code -> catalog.findByCode(code).orElseThrow().transferExercises().size())
                .sum();
        mockMvc.perform(get("/api/adaptation/insights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transferScore").value(Math.round(100f / available)))
                .andExpect(jsonPath("$.factors[?(@ == 'transferts réussis : 1 sur " + available + "')]").exists());
    }

    private String submitAndRun(UUID attemptId, String exerciseCode, String source) throws Exception {
        String created = mockMvc.perform(post("/api/attempts/" + attemptId + "/submissions").contentType(MediaType.APPLICATION_JSON)
                        .content(submission(exerciseCode, source)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(created).get("id").asText();
        String result = mockMvc.perform(post("/api/submissions/" + id + "/run"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(result).get("status").asText();
    }

    private String submission(String exerciseCode, String source) throws Exception {
        var body = objectMapper.createObjectNode().put("language", "JAVA").put("sourceCode", source).put("origin", "EDITOR");
        if (exerciseCode != null) body.put("exerciseCode", exerciseCode);
        return objectMapper.writeValueAsString(body);
    }

    private UUID attempt(String status) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("insert into attempt (id, lab_id, started_at, status) values (?, 'JAVA-10', ?, ?)",
                id, Timestamp.from(Instant.now()), status);
        return id;
    }
}
