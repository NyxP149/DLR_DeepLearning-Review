package com.dlr.execution.infrastructure;

import com.dlr.execution.application.RunnerAvailability;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "dlr.execution.enabled", havingValue = "true", matchIfMissing = true)
public class DockerRunnerAvailability implements RunnerAvailability {

    private static final Duration CACHE_DURATION = Duration.ofSeconds(15);
    private static final long PROBE_TIMEOUT_SECONDS = 5;

    private final String dockerCli;
    private final List<String> images;
    private volatile CachedStatus cached;

    public DockerRunnerAvailability(
            @Value("${dlr.execution.docker-cli:docker}") String dockerCli,
            @Value("${dlr.execution.java-image:dlr/java-runner:21}") String javaImage,
            @Value("${dlr.execution.python-image:dlr/python-runner:3.13}") String pythonImage,
            @Value("${dlr.execution.typescript-image:dlr/typescript-runner:22}") String typescriptImage
    ) {
        this.dockerCli = DockerJavaRunner.resolveDockerCli(dockerCli);
        this.images = List.of(javaImage, pythonImage, typescriptImage);
    }

    @Override
    public Status status() {
        CachedStatus current = cached;
        Instant now = Instant.now();
        if (current != null && current.checkedAt().plus(CACHE_DURATION).isAfter(now)) {
            return current.status();
        }

        Status checked = probe();
        cached = new CachedStatus(now, checked);
        return checked;
    }

    private Status probe() {
        Process process = null;
        try {
            var command = new java.util.ArrayList<String>();
            command.add(dockerCli);
            command.add("image");
            command.add("inspect");
            command.addAll(images);
            process = new ProcessBuilder(command)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            if (!process.waitFor(PROBE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return unavailable("Docker ne répond pas. Vérifie que Docker Desktop est démarré.");
            }
            if (process.exitValue() != 0) {
                return unavailable("Une ou plusieurs images Runner sont absentes. Relance le script hybride.");
            }
            return new Status(true, "LOCAL_DOCKER", "Runner Docker local connecté et prêt.");
        } catch (IOException exception) {
            return unavailable("Docker est introuvable ou inaccessible sur cette machine.");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return unavailable("La vérification du Runner a été interrompue.");
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private Status unavailable(String message) {
        return new Status(false, "UNAVAILABLE", message);
    }

    private record CachedStatus(Instant checkedAt, Status status) {
    }
}
