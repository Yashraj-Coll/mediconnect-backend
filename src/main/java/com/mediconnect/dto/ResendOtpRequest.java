package com.mediconnect.dto;

import jakarta.validation.constraints.NotBlank;

public class ResendOtpRequest {
    
    @NotBlank(message = "Email or phone number is required")
    private String identifier;
    
    public ResendOtpRequest() {}
    
    public ResendOtpRequest(String identifier) {
        this.identifier = identifier;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}