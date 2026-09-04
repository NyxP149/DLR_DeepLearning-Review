package com.dlr.execution.infrastructure;

import com.dlr.execution.application.RunnerAvailability;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "dlr.execution.enabled", havingValue = "false")
public class DisabledRunnerAvailability implements RunnerAvailability {

    @Override
    public Status status() {
        return new Status(false, "DISABLED", "L'exécution est désactivée sur cette API.");
    }
}
