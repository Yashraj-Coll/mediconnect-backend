package com.mediconnect.exception;

public class TreatmentRecommendationException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public TreatmentRecommendationException(String message) {
        super(message);
    }
    
    public TreatmentRecommendationException(String message, Throwable cause) {
        super(message, cause);
    }
}