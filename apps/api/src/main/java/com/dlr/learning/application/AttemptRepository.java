package com.dlr.learning.application;

import com.dlr.learning.domain.Attempt;
import com.dlr.learning.domain.AttemptStatus;

import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;

public interface AttemptRepository {

    Attempt save(Attempt attempt);

    Optional<Attempt> findById(UUID id);

    Optional<Attempt> findLatestInProgress(String labCode);

    Attempt complete(UUID id, AttemptStatus status, BigDecimal score, ScoreBreakdown breakdown, java.time.Instant completedAt);

    Attempt allowContinuation(UUID id);

    record ScoreBreakdown(
            BigDecimal tests,
            BigDecimal quiz,
            BigDecimal practice,
            BigDecimal connections,
            BigDecimal selfAssessment,
            String version
    ) {
    }
}
