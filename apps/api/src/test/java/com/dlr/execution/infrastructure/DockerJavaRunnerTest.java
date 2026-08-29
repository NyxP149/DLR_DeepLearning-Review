package com.dlr.execution.infrastructure;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class DockerJavaRunnerTest {

    @Test
    void buildsAHardenedOfflineDockerCommand() {
        DockerJavaRunner runner = new DockerJavaRunner(
                "docker",
                "dlr/java-runner:21",
                Duration.ofSeconds(10),
                Clock.systemUTC());

        var command = runner.buildCommand(Path.of("C:/tmp/dlr-run"), "dlr-test");

        assertThat(command)
                .containsSubsequence("--pull", "never")
                .containsSubsequence("--network", "none")
                .contains("--read-only")
                .containsSubsequence("--cap-drop", "ALL")
                .containsSubsequence("--security-opt", "no-new-privileges")
                .containsSubsequence("--user", "10001:10001")
                .contains("dlr/java-runner:21");
        assertThat(command.getLast()).contains("javac").contains("java -cp");
    }
}

