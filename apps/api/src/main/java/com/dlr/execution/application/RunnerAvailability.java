package com.dlr.execution.application;

public interface RunnerAvailability {

    Status status();

    record Status(boolean available, String mode, String message) {
    }
}
