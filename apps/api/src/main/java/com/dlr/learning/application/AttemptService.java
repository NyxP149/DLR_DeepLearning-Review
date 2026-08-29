package com.dlr.learning.application;

import com.dlr.catalog.application.LabCatalog;
import com.dlr.learning.domain.Attempt;
import com.dlr.learning.domain.AttemptStatus;
import com.dlr.shared.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AttemptService {

    private final LabCatalog labCatalog;
    private final AttemptRepository attemptRepository;
    private final Clock clock;

    @Autowired
    public AttemptService(LabCatalog labCatalog, AttemptRepository attemptRepository) {
        this(labCatalog, attemptRepository, Clock.systemUTC());
    }

    AttemptService(LabCatalog labCatalog, AttemptRepository attemptRepository, Clock clock) {
        this.labCatalog = labCatalog;
        this.attemptRepository = attemptRepository;
        this.clock = clock;
    }

    @Transactional
    public Attempt start(String labCode) {
        labCatalog.findByCode(labCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "LAB_NOT_FOUND", "Laboratoire introuvable : " + labCode));

        Attempt attempt = new Attempt(
                UUID.randomUUID(),
                labCode.toUpperCase(),
                Instant.now(clock),
                null,
                AttemptStatus.IN_PROGRESS,
                null,
                false);
        return attemptRepository.save(attempt);
    }

    public Attempt get(UUID id) {
        return attemptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ATTEMPT_NOT_FOUND", "Tentative introuvable : " + id));
    }

    @Transactional
    public Attempt complete(UUID id, BigDecimal score, AttemptRepository.ScoreBreakdown breakdown, int threshold) {
        Attempt attempt = get(id);
        if (attempt.status() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cette tentative est déjà terminée.");
        }
        AttemptStatus status = score.compareTo(BigDecimal.valueOf(threshold)) >= 0
                ? AttemptStatus.COMPLETED
                : AttemptStatus.COMPLETED_BELOW_THRESHOLD;
        return attemptRepository.complete(id, status, score, breakdown, Instant.now(clock));
    }

    @Transactional
    public Attempt continueBelowThreshold(UUID id) {
        Attempt attempt = get(id);
        if (attempt.status() != AttemptStatus.COMPLETED_BELOW_THRESHOLD) {
            throw new IllegalStateException("La poursuite explicite concerne uniquement une tentative sous le seuil.");
        }
        return attemptRepository.allowContinuation(id);
    }
}
