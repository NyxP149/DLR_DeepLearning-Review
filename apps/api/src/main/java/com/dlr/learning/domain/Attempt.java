package com.dlr.learning.domain;

import java.time.Instant;
import java.util.UUID;

public record Attempt(
        UUID id,
        String labCode,
        Instant startedAt,
        Instant completedAt,
        AttemptStatus status
) {
}

