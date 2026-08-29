package com.dlr.execution.infrastructure;

import com.dlr.execution.domain.ExecutionStatus;
import com.dlr.execution.domain.Submission;
import com.dlr.execution.domain.SubmissionOrigin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "DLR_RUN_DOCKER_TESTS", matches = "true")
class DockerJavaRunnerIntegrationTest {

    private static final String IMAGE = "dlr/java-runner:21";

    @Test
    void compilesAndRunsJavaCodeWithoutNetworkOrHostWriteAccess() {
        DockerJavaRunner runner = new DockerJavaRunner(
                dockerCli(), IMAGE, Duration.ofSeconds(10), Clock.systemUTC());

        var result = runner.run(submission(
                """
                public class Main {
                    public static void main(String[] args) {
                        System.out.println("DLR Java Lab 1");
                    }
                }
                """));

        assertThat(result.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(result.standardOutput()).contains("DLR Java Lab 1");
        assertThat(result.errorOutput()).isBlank();
    }

    @Test
    void reportsACompilationError() {
        DockerJavaRunner runner = new DockerJavaRunner(
                dockerCli(), IMAGE, Duration.ofSeconds(10), Clock.systemUTC());

        var result = runner.run(submission(
                """
                public class Main {
                    public static void main(String[] args) {
                        System.out.println("erreur")
                    }
                }
                """));

        assertThat(result.status()).isEqualTo(ExecutionStatus.COMPILATION_ERROR);
        assertThat(result.exitCode()).isEqualTo(20);
        assertThat(result.errorOutput()).contains("error:");
    }

    @Test
    void forciblyStopsAnInfiniteLoop() {
        DockerJavaRunner runner = new DockerJavaRunner(
                dockerCli(), IMAGE, Duration.ofSeconds(1), Clock.systemUTC());

        var result = runner.run(submission(
                """
                public class Main {
                    public static void main(String[] args) {
                        while (true) {
                        }
                    }
                }
                """));

        assertThat(result.status()).isEqualTo(ExecutionStatus.TIMEOUT);
        assertThat(result.errorOutput()).contains("Temps maximal");
    }

    private Submission submission(String sourceCode) {
        return new Submission(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "JAVA",
                sourceCode,
                SubmissionOrigin.EDITOR,
                Instant.now());
    }

    private String dockerCli() {
        String localAppData = System.getenv("LOCALAPPDATA");
        return localAppData
                + "/Programs/DockerDesktop/resources/bin/docker.exe";
    }
}

