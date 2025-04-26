package com.mediconnect.exception;

public class HealthPredictionException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public HealthPredictionException(String message) {
        super(message);
    }
    
    public HealthPredictionException(String message, Throwable cause) {
        super(message, cause);
    }
}