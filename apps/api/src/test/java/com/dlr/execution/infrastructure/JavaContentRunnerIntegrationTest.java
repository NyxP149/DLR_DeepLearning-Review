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
            // JAVA-01 à JAVA-06 sont les exercices guidés historiques : leur starter
            // contient volontairement le travail à compléter. JAVA-07 à JAVA-24
            // fournissent un exemple exécutable dont la sortie sert de contrat.
            if (lab.number() >= 7) {
                assertThat(result.standardOutput().strip()).as(lab.code()).isEqualTo(exercise.expectedOutput());
            }
        }
    }
}
