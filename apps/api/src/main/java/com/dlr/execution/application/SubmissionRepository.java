package com.dlr.execution.application;

import com.dlr.execution.domain.Submission;

import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository {

    Submission save(Submission submission);

    Optional<Submission> findById(UUID id);

    Optional<Submission> findLatestByAttemptId(UUID attemptId);
}
