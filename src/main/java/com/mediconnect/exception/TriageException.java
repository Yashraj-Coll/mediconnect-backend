package com.mediconnect.exception;

public class TriageException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public TriageException(String message) {
        super(message);
    }
    
    public TriageException(String message, Throwable cause) {
        super(message, cause);
    }
}