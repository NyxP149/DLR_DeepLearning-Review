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

/**
 * Exécute les 24 exercices du parcours Terminal (bash et PowerShell) dans l'image dlr/shell-runner:1 :
 * chaque squelette doit s'exécuter sans donner la sortie attendue, chaque solution de référence doit la produire.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DLR_RUN_DOCKER_TESTS", matches = "true")
class TerminalContentRunnerIntegrationTest {

    @Autowired CodeRunner runner;
    @Autowired LabCatalog catalog;

    @Test
    void executesEveryTerminalExerciseInDocker() {
        var labs = catalog.findAll().stream().filter(lab -> lab.code().startsWith("TERMINAL-")).toList();
        assertThat(labs).hasSize(24);
        assertThat(labs.stream().map(lab -> lab.language()).distinct()).containsExactlyInAnyOrder("BASH", "POWERSHELL");

        for (var lab : labs) {
            var exercise = lab.exercises().getFirst();
            var starter = runner.run(submission(lab.language(), exercise.starterCode()));
            assertThat(starter.status()).as(lab.code() + ": " + starter.errorOutput()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(starter.standardOutput().strip()).as(lab.code() + " ne doit pas donner la solution")
                    .isNotEqualTo(exercise.expectedOutput().strip());

            var solved = runner.run(submission(lab.language(), referenceSolution(exercise.code(), lab.language())));
            assertThat(solved.status()).as(lab.code() + ": " + solved.errorOutput()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(solved.standardOutput().strip()).as(lab.code() + " solution de référence")
                    .isEqualTo(exercise.expectedOutput().strip());
        }
    }

    private static Submission submission(String language, String source) {
        return new Submission(UUID.randomUUID(), UUID.randomUUID(), language, source, SubmissionOrigin.EDITOR, Instant.now());
    }

    private static String referenceSolution(String exerciseCode, String language) {
        String extension = "POWERSHELL".equals(language) ? "ps1" : "sh";
        try (var stream = TerminalContentRunnerIntegrationTest.class
                .getResourceAsStream("/terminal-solutions/" + exerciseCode + "." + extension)) {
            assertThat(stream).as("solution de référence " + exerciseCode).isNotNull();
            return new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
