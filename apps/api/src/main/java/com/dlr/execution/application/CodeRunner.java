package com.dlr.execution.application;

import com.dlr.execution.domain.ExecutionResult;
import com.dlr.execution.domain.Submission;

public interface CodeRunner {

    ExecutionResult run(Submission submission);
}

