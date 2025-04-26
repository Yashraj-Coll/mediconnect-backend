package com.mediconnect.exception;

public class MedicationInteractionException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public MedicationInteractionException(String message) {
        super(message);
    }
    
    public MedicationInteractionException(String message, Throwable cause) {
        super(message, cause);
    }
}