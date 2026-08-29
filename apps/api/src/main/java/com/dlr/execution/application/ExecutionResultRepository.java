package com.dlr.execution.application;

import com.dlr.execution.domain.ExecutionResult;

import java.util.Optional;
import java.util.UUID;

public interface ExecutionResultRepository {

    ExecutionResult save(ExecutionResult result);

    Optional<ExecutionResult> findLatestByAttemptId(UUID attemptId);
}
