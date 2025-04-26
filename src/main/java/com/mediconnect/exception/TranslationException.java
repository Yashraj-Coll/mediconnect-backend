package com.mediconnect.exception;

public class TranslationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public TranslationException(String message) {
        super(message);
    }
    
    public TranslationException(String message, Throwable cause) {
        super(message, cause);
    }
}