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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DLR_RUN_DOCKER_TESTS", matches = "true")
class ExpandedContentRunnerIntegrationTest {

    private static final Map<String, Integer> EXPECTED = Map.of(
            "TYPESCRIPT-", 24,
            "SPRING_BOOT-", 12,
            "ANGULAR-", 10,
            "SQL-", 10,
            "DEVOPS-", 8,
            "LLM-", 12);

    @Autowired private CodeRunner runner;
    @Autowired private LabCatalog catalog;

    @Test
    void executesEveryExpandedProfessionalStarterInDocker() {
        EXPECTED.forEach((prefix, count) -> {
            var labs = catalog.findAll().stream().filter(lab -> lab.code().startsWith(prefix)).toList();
            assertThat(labs).as(prefix).hasSize(count);
            for (var lab : labs) {
                var exercise = lab.exercises().getFirst();
                var submission = new Submission(
                        UUID.randomUUID(), UUID.randomUUID(), lab.language(), exercise.starterCode(),
                        SubmissionOrigin.EDITOR, Instant.now());
                var result = runner.run(submission);
                assertThat(result.status()).as(lab.code() + ": " + result.errorOutput()).isEqualTo(ExecutionStatus.SUCCESS);
                assertThat(result.standardOutput().strip()).as(lab.code()).isEqualTo(exercise.expectedOutput());
            }
        });
    }
}
