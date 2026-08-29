package com.dlr.execution.domain;

import java.time.Instant;
import java.util.UUID;

public record ExecutionResult(
        UUID id,
        UUID submissionId,
        ExecutionStatus status,
        Integer exitCode,
        String standardOutput,
        String errorOutput,
        long durationMs,
        Instant createdAt
) {
}

