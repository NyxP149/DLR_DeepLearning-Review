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
        assertThat(command.getLast())
                .contains("cp /workspace/Main.java /work/Main.java")
                .contains("cd /work")
                .contains("javac")
                .contains("java -cp");
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

    @Test
    void routesBashAndPowershellToTheShellImageWithAnExecutableWorkDirectory() {
        DockerJavaRunner runner = new DockerJavaRunner(
                "docker", "java-image", "python-image", "typescript-image", "shell-image", Duration.ofSeconds(10), Clock.systemUTC());
        var bash = runner.buildCommand(Path.of("C:/tmp/dlr-bash"), "dlr-bash", runner.spec("BASH"));
        var powershell = runner.buildCommand(Path.of("C:/tmp/dlr-ps"), "dlr-ps", runner.spec("POWERSHELL"));

        assertThat(bash).contains("shell-image").containsSubsequence("--network", "none").contains("--read-only");
        assertThat(bash.getLast()).contains("bash -n main.sh").contains("bash main.sh").contains("export HOME=/work");
        assertThat(powershell).contains("shell-image");
        assertThat(powershell.getLast()).contains("pwsh -NoProfile -NonInteractive -File main.ps1");
        // Un terminal doit pouvoir lancer ses propres scripts : /work y est exécutable (exec explicite, Docker monte noexec par défaut).
        assertThat(bash).contains("/work:rw,exec,nosuid,size=32m");
        assertThat(powershell).contains("/work:rw,exec,nosuid,size=32m");
    }

    @Test
    void keepsTheWorkDirectoryNonExecutableForCompiledLanguages() {
        DockerJavaRunner runner = new DockerJavaRunner("docker", "java-image", Duration.ofSeconds(10), Clock.systemUTC());
        assertThat(runner.buildCommand(Path.of("C:/tmp/dlr-java"), "dlr-java", runner.spec("JAVA")))
                .contains("/work:rw,noexec,nosuid,size=32m");
        assertThat(runner.buildCommand(Path.of("C:/tmp/dlr-python"), "dlr-python", runner.spec("PYTHON")))
                .contains("/work:rw,noexec,nosuid,size=32m");
    }
}
