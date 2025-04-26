package com.mediconnect.exception;

public class ChatbotException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public ChatbotException(String message) {
        super(message);
    }
    
    public ChatbotException(String message, Throwable cause) {
        super(message, cause);
    }
}