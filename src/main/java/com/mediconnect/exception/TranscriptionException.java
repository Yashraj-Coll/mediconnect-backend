package com.mediconnect.exception;

public class TranscriptionException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public TranscriptionException(String message) {
        super(message);
    }
    
    public TranscriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}