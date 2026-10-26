package com.dlr.execution.domain;

import java.time.Instant;
import java.util.UUID;

/** `exerciseCode` : exercice de transfert visé, vide pour l'exercice guidé du laboratoire. */
public record Submission(
        UUID id,
        UUID attemptId,
        String language,
        String sourceCode,
        SubmissionOrigin origin,
        Instant createdAt,
        String exerciseCode
) {
    public Submission(UUID id, UUID attemptId, String language, String sourceCode, SubmissionOrigin origin, Instant createdAt) {
        this(id, attemptId, language, sourceCode, origin, createdAt, null);
    }
}
