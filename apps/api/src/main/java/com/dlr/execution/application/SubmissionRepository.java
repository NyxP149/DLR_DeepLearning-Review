package com.dlr.execution.application;

import com.dlr.execution.domain.Submission;

import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository {

    Submission save(Submission submission);

    Optional<Submission> findById(UUID id);

    /** Dernière soumission de l'exercice guidé (celle qui sert au score), jamais celle d'un exercice de transfert. */
    Optional<Submission> findLatestByAttemptId(UUID attemptId);

    Optional<Submission> findLatestByAttemptIdAndExercise(UUID attemptId, String exerciseCode);
}
