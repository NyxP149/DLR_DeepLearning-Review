package com.dlr.execution.domain;

import java.time.Instant;
import java.util.UUID;

public record Submission(
        UUID id,
        UUID attemptId,
        String language,
        String sourceCode,
        SubmissionOrigin origin,
        Instant createdAt
) {
}

