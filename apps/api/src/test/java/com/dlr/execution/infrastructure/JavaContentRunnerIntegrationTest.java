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
class JavaContentRunnerIntegrationTest {

    @Autowired private CodeRunner runner;
    @Autowired private LabCatalog catalog;

    @Test
    void compilesEveryStarterAndExecutesTheProfessionalSequenceInTheIsolatedRunner() {
        var javaLabs = catalog.findAll().stream()
                .filter(lab -> lab.code().startsWith("JAVA-"))
                .toList();

        assertThat(javaLabs).hasSize(24);
        for (var lab : javaLabs) {
            var exercise = lab.exercises().getFirst();
            var submission = new Submission(
                    UUID.randomUUID(), UUID.randomUUID(), "JAVA", exercise.starterCode(),
                    SubmissionOrigin.EDITOR, Instant.now());
            var result = runner.run(submission);
            assertThat(result.status()).as(lab.code() + ": " + result.errorOutput()).isEqualTo(ExecutionStatus.SUCCESS);
            // Tous les starters sont des squelettes à compléter : ils compilent
            // mais ne produisent pas encore la sortie attendue.
            assertThat(result.standardOutput().strip()).as(lab.code() + " ne doit pas donner la solution")
                    .isNotEqualTo(exercise.expectedOutput().strip());

            if (lab.number() >= 7) {
                var solution = new Submission(
                        UUID.randomUUID(), UUID.randomUUID(), "JAVA", referenceSolution(exercise.code()),
                        SubmissionOrigin.EDITOR, Instant.now());
                var solved = runner.run(solution);
                assertThat(solved.status()).as(lab.code() + ": " + solved.errorOutput()).isEqualTo(ExecutionStatus.SUCCESS);
                assertThat(solved.standardOutput().strip()).as(lab.code() + " solution de référence")
                        .isEqualTo(exercise.expectedOutput().strip());
            }
        }
    }

    private static String referenceSolution(String exerciseCode) {
        try (var stream = JavaContentRunnerIntegrationTest.class.getResourceAsStream("/java-solutions/" + exerciseCode + ".java")) {
            assertThat(stream).as("solution de référence " + exerciseCode).isNotNull();
            return new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
