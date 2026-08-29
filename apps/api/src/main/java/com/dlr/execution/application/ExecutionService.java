package com.dlr.execution.application;

import com.dlr.execution.domain.ExecutionResult;
import com.dlr.execution.domain.Submission;
import com.dlr.execution.domain.SubmissionOrigin;
import com.dlr.learning.application.AttemptService;
import com.dlr.catalog.application.LabCatalog;
import com.dlr.shared.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class ExecutionService {

    static final int MAX_SOURCE_BYTES = 64 * 1024;

    private final AttemptService attemptService;
    private final SubmissionRepository submissionRepository;
    private final ExecutionResultRepository resultRepository;
    private final CodeRunner codeRunner;
    private final LabCatalog labCatalog;
    private final Clock clock;

    @Autowired
    public ExecutionService(
            AttemptService attemptService,
            SubmissionRepository submissionRepository,
            ExecutionResultRepository resultRepository,
            CodeRunner codeRunner,
            LabCatalog labCatalog
    ) {
        this(attemptService, submissionRepository, resultRepository, codeRunner, labCatalog, Clock.systemUTC());
    }

    ExecutionService(
            AttemptService attemptService,
            SubmissionRepository submissionRepository,
            ExecutionResultRepository resultRepository,
            CodeRunner codeRunner,
            LabCatalog labCatalog,
            Clock clock
    ) {
        this.attemptService = attemptService;
        this.submissionRepository = submissionRepository;
        this.resultRepository = resultRepository;
        this.codeRunner = codeRunner;
        this.labCatalog = labCatalog;
        this.clock = clock;
    }

    @Transactional
    public Submission createSubmission(
            UUID attemptId,
            String language,
            String sourceCode,
            SubmissionOrigin origin
    ) {
        attemptService.get(attemptId);
        if (!"JAVA".equalsIgnoreCase(language)) {
            throw new IllegalArgumentException("Seul le langage JAVA est disponible dans cette tranche.");
        }
        if (sourceCode == null || sourceCode.isBlank()) {
            throw new IllegalArgumentException("Le code source est obligatoire.");
        }
        if (sourceCode.getBytes(StandardCharsets.UTF_8).length > MAX_SOURCE_BYTES) {
            throw new IllegalArgumentException("Le code source dépasse la limite de 64 Kio.");
        }

        return submissionRepository.save(new Submission(
                UUID.randomUUID(),
                attemptId,
                "JAVA",
                sourceCode,
                origin,
                Instant.now(clock)));
    }

    public ExecutionResult run(UUID submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SUBMISSION_NOT_FOUND", "Soumission introuvable : " + submissionId));
        ExecutionResult result = codeRunner.run(submission);
        if (result.status() == com.dlr.execution.domain.ExecutionStatus.SUCCESS) {
            var attempt = attemptService.get(submission.attemptId());
            var lab = labCatalog.findByCode(attempt.labCode())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "LAB_NOT_FOUND", "Laboratoire introuvable : " + attempt.labCode()));
            String expected = lab.exercises().getFirst().expectedOutput().strip();
            if (!result.standardOutput().strip().equals(expected)) {
                result = new ExecutionResult(
                        result.id(), result.submissionId(),
                        com.dlr.execution.domain.ExecutionStatus.TESTS_FAILED,
                        result.exitCode(), result.standardOutput(),
                        "Test visible échoué : la sortie du programme ne correspond pas exactement à la consigne.",
                        result.durationMs(), result.createdAt());
            }
        }
        return resultRepository.save(result);
    }
}
