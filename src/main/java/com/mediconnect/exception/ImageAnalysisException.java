package com.mediconnect.exception;

public class ImageAnalysisException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public ImageAnalysisException(String message) {
        super(message);
    }
    
    public ImageAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}