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

    @Test
    void routesPythonAndTypescriptToTheirDedicatedImages() {
        DockerJavaRunner runner = new DockerJavaRunner("docker", "java-image", "python-image", "typescript-image", Duration.ofSeconds(10), Clock.systemUTC());
        var python = runner.buildCommand(Path.of("C:/tmp/dlr-python"), "dlr-python", runner.spec("PYTHON"));
        var typescript = runner.buildCommand(Path.of("C:/tmp/dlr-ts"), "dlr-ts", runner.spec("TYPESCRIPT"));
        assertThat(python).contains("python-image");
        assertThat(python.getLast()).contains("py_compile").contains("main.py");
        assertThat(typescript).contains("typescript-image");
        assertThat(typescript.getLast()).contains("tsc --strict").contains("node /work/main.js");
    }
}
