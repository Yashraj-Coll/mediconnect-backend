package com.mediconnect.exception;

public class MonitoringException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public MonitoringException(String message) {
        super(message);
    }
    
    public MonitoringException(String message, Throwable cause) {
        super(message, cause);
    }
}