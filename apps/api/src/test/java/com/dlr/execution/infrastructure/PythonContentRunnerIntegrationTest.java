package com.dlr.execution.infrastructure;

import com.dlr.catalog.application.LabCatalog;
import com.dlr.execution.application.CodeRunner;
import com.dlr.execution.domain.ExecutionStatus;
import com.dlr.execution.domain.Submission;
import com.dlr.execution.domain.SubmissionOrigin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DLR_RUN_DOCKER_TESTS", matches = "true")
class PythonContentRunnerIntegrationTest {

    @Autowired private CodeRunner runner;
    @Autowired private LabCatalog catalog;

    @Test
    void executesEveryPythonProfessionalStarterInTheIsolatedRunner() {
        var pythonLabs = catalog.findAll().stream()
                .filter(lab -> lab.code().startsWith("PYTHON-"))
                .toList();

        assertThat(pythonLabs).hasSize(24);
        for (var lab : pythonLabs) {
            var exercise = lab.exercises().getFirst();
            var submission = new Submission(
                    UUID.randomUUID(), UUID.randomUUID(), "PYTHON", exercise.starterCode(),
                    SubmissionOrigin.EDITOR, Instant.now());
            var result = runner.run(submission);
            assertThat(result.status()).as(lab.code() + ": " + result.errorOutput()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(result.standardOutput().strip()).as(lab.code() + " ne doit pas donner la solution")
                    .isNotEqualTo(exercise.expectedOutput().strip());

            var solved = runner.run(new Submission(
                    UUID.randomUUID(), UUID.randomUUID(), "PYTHON", referenceSolution("python", exercise.code(), "py"),
                    SubmissionOrigin.EDITOR, Instant.now()));
            assertThat(solved.status()).as(lab.code() + ": " + solved.errorOutput()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(solved.standardOutput().strip()).as(lab.code() + " solution de référence")
                    .isEqualTo(exercise.expectedOutput().strip());
        }
    }

    private static String referenceSolution(String folder, String exerciseCode, String extension) {
        try (var stream = PythonContentRunnerIntegrationTest.class.getResourceAsStream("/" + folder + "-solutions/" + exerciseCode + "." + extension)) {
            assertThat(stream).as("solution de référence " + exerciseCode).isNotNull();
            return new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}

