package com.dlr.learning.domain;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.UUID;

public record Attempt(
        UUID id,
        String labCode,
        Instant startedAt,
        Instant completedAt,
        AttemptStatus status,
        BigDecimal score,
        boolean continuedBelowThreshold
) {
}
