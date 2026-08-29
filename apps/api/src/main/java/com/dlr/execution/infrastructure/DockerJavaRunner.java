package com.dlr.execution.infrastructure;

import com.dlr.execution.application.CodeRunner;
import com.dlr.execution.domain.ExecutionResult;
import com.dlr.execution.domain.ExecutionStatus;
import com.dlr.execution.domain.Submission;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class DockerJavaRunner implements CodeRunner {

    private static final int MAX_OUTPUT_BYTES = 64 * 1024;
    private static final int COMPILATION_ERROR_EXIT_CODE = 20;

    private final String dockerCli;
    private final String image;
    private final Duration timeout;
    private final Clock clock;

    @Autowired
    public DockerJavaRunner(
            @Value("${dlr.execution.docker-cli:docker}") String dockerCli,
            @Value("${dlr.execution.java-image:dlr/java-runner:21}") String image,
            @Value("${dlr.execution.timeout-seconds:10}") long timeoutSeconds
    ) {
        this(resolveDockerCli(dockerCli), image, Duration.ofSeconds(timeoutSeconds), Clock.systemUTC());
    }

    DockerJavaRunner(String dockerCli, String image, Duration timeout, Clock clock) {
        this.dockerCli = dockerCli;
        this.image = image;
        this.timeout = timeout;
        this.clock = clock;
    }

    @Override
    public ExecutionResult run(Submission submission) {
        Instant startedAt = Instant.now(clock);
        Path workspace = null;
        String containerName = "dlr-java-" + UUID.randomUUID();

        try {
            workspace = Files.createTempDirectory("dlr-java-");
            Files.writeString(
                    workspace.resolve("Main.java"),
                    submission.sourceCode(),
                    StandardCharsets.UTF_8);

            Process process = new ProcessBuilder(buildCommand(workspace, containerName))
                    .redirectInput(ProcessBuilder.Redirect.PIPE)
                    .start();
            process.getOutputStream().close();

            CompletableFuture<String> stdout = readAsync(process.getInputStream());
            CompletableFuture<String> stderr = readAsync(process.getErrorStream());

            if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                removeContainer(containerName);
                process.destroyForcibly();
                return result(
                        submission.id(),
                        ExecutionStatus.TIMEOUT,
                        null,
                        completed(stdout),
                        append(completed(stderr), "Temps maximal d'exécution dépassé."),
                        startedAt);
            }

            int exitCode = process.exitValue();
            ExecutionStatus status = switch (exitCode) {
                case 0 -> ExecutionStatus.SUCCESS;
                case COMPILATION_ERROR_EXIT_CODE -> ExecutionStatus.COMPILATION_ERROR;
                default -> ExecutionStatus.RUNTIME_ERROR;
            };
            return result(
                    submission.id(),
                    status,
                    exitCode,
                    completed(stdout),
                    completed(stderr),
                    startedAt);
        } catch (IOException exception) {
            return result(
                    submission.id(),
                    ExecutionStatus.RUNNER_ERROR,
                    null,
                    "",
                    "Impossible de démarrer le runner Docker : " + exception.getMessage(),
                    startedAt);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            removeContainer(containerName);
            return result(
                    submission.id(),
                    ExecutionStatus.RUNNER_ERROR,
                    null,
                    "",
                    "Exécution interrompue.",
                    startedAt);
        } finally {
            deleteWorkspace(workspace);
        }
    }

    List<String> buildCommand(Path workspace, String containerName) {
        List<String> command = new ArrayList<>();
        command.add(dockerCli);
        command.addAll(List.of(
                "run",
                "--rm",
                "--pull", "never",
                "--name", containerName,
                "--network", "none",
                "--memory", "128m",
                "--memory-swap", "128m",
                "--cpus", "0.50",
                "--pids-limit", "64",
                "--read-only",
                "--tmpfs", "/work:rw,noexec,nosuid,size=32m",
                "--cap-drop", "ALL",
                "--security-opt", "no-new-privileges",
                "--user", "10001:10001",
                "--mount", "type=bind,source=" + workspace.toAbsolutePath() + ",target=/workspace,readonly",
                image,
                "sh",
                "-c",
                "javac -encoding UTF-8 -d /work /workspace/Main.java || exit 20; java -cp /work Main"
        ));
        return List.copyOf(command);
    }

    private CompletableFuture<String> readAsync(InputStream input) {
        return CompletableFuture.supplyAsync(() -> {
            try (input) {
                ByteArrayOutputStream captured = new ByteArrayOutputStream(MAX_OUTPUT_BYTES);
                byte[] buffer = new byte[4096];
                boolean truncated = false;
                int read;
                while ((read = input.read(buffer)) != -1) {
                    int remaining = MAX_OUTPUT_BYTES - captured.size();
                    if (remaining > 0) {
                        captured.write(buffer, 0, Math.min(read, remaining));
                    }
                    if (read > remaining) {
                        truncated = true;
                    }
                }
                String value = captured.toString(StandardCharsets.UTF_8);
                return truncated ? value + System.lineSeparator() + "[sortie tronquée]" : value;
            } catch (IOException exception) {
                return "[sortie indisponible : " + exception.getMessage() + "]";
            }
        });
    }

    private String completed(CompletableFuture<String> output) {
        try {
            return output.get(2, TimeUnit.SECONDS);
        } catch (Exception exception) {
            return "[sortie indisponible]";
        }
    }

    private ExecutionResult result(
            UUID submissionId,
            ExecutionStatus status,
            Integer exitCode,
            String stdout,
            String stderr,
            Instant startedAt
    ) {
        Instant completedAt = Instant.now(clock);
        return new ExecutionResult(
                UUID.randomUUID(),
                submissionId,
                status,
                exitCode,
                stdout,
                stderr,
                Math.max(0, Duration.between(startedAt, completedAt).toMillis()),
                completedAt);
    }

    private void removeContainer(String containerName) {
        try {
            Process cleanup = new ProcessBuilder(dockerCli, "rm", "-f", containerName).start();
            cleanup.waitFor(5, TimeUnit.SECONDS);
        } catch (IOException exception) {
            // Le conteneur possède --rm et sera supprimé par Docker à la fin du processus.
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private void deleteWorkspace(Path workspace) {
        if (workspace == null) {
            return;
        }
        try {
            Files.deleteIfExists(workspace.resolve("Main.java"));
            Files.deleteIfExists(workspace);
        } catch (IOException exception) {
            workspace.toFile().deleteOnExit();
        }
    }

    private String append(String current, String extra) {
        if (current == null || current.isBlank()) {
            return extra;
        }
        return current + System.lineSeparator() + extra;
    }

    private static String resolveDockerCli(String configuredCli) {
        if (!"docker".equalsIgnoreCase(configuredCli)) {
            return configuredCli;
        }

        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            Path userInstall = Path.of(
                    localAppData,
                    "Programs",
                    "DockerDesktop",
                    "resources",
                    "bin",
                    "docker.exe");
            if (Files.isRegularFile(userInstall)) {
                return userInstall.toString();
            }
        }
        return configuredCli;
    }
}
