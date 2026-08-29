package com.dlr.execution.application;

import com.dlr.execution.domain.ExecutionResult;

public interface ExecutionResultRepository {

    ExecutionResult save(ExecutionResult result);
}

