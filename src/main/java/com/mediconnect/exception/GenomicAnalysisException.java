package com.mediconnect.exception;

public class GenomicAnalysisException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public GenomicAnalysisException(String message) {
        super(message);
    }
    
    public GenomicAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}