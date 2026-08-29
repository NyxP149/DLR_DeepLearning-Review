package com.dlr.execution.domain;

public enum ExecutionStatus {
    SUCCESS,
    TESTS_FAILED,
    COMPILATION_ERROR,
    RUNTIME_ERROR,
    TIMEOUT,
    RUNNER_ERROR
}
