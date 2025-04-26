package com.mediconnect.dto;

import java.util.Map;

public class AiDiagnosisRequestDTO {
    
    private String inputParameters;
    private Map<String, Object> additionalInfo;
    
    // Getters and Setters
    public String getInputParameters() {
        return inputParameters;
    }
    
    public void setInputParameters(String inputParameters) {
        this.inputParameters = inputParameters;
    }
    
    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }
    
    public void setAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}