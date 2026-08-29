package com.dlr.tutor.application;

public class TutorUnavailableException extends RuntimeException {
    public TutorUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
