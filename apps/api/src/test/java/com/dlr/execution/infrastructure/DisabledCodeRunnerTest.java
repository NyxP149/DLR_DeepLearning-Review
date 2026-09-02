package com.dlr.execution.infrastructure;

import com.dlr.execution.application.ExecutionUnavailableException;
import com.dlr.execution.domain.Submission;
import com.dlr.execution.domain.SubmissionOrigin;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisabledCodeRunnerTest {

    @Test
    void refusesExecutionWithoutPretendingThatTheSubmittedCodeFailed() {
        var submission = new Submission(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "JAVA",
                "class Main {}",
                SubmissionOrigin.EDITOR,
                Instant.now());

        assertThatThrownBy(() -> new DisabledCodeRunner().run(submission))
                .isInstanceOf(ExecutionUnavailableException.class)
                .hasMessageContaining("service Runner isolé");
    }
}

