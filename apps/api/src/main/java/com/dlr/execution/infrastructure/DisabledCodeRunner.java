package com.dlr.execution.infrastructure;

import com.dlr.execution.application.CodeRunner;
import com.dlr.execution.application.ExecutionUnavailableException;
import com.dlr.execution.domain.ExecutionResult;
import com.dlr.execution.domain.Submission;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "dlr.execution.enabled", havingValue = "false")
public class DisabledCodeRunner implements CodeRunner {

    @Override
    public ExecutionResult run(Submission submission) {
        throw new ExecutionUnavailableException(
                "L'exécution distante est temporairement désactivée. Le laboratoire et le brouillon restent accessibles pendant le déploiement du service Runner isolé.");
    }
}

