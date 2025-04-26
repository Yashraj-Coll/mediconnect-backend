package com.mediconnect.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class VoiceTranscriptionDTO {
    
    private String transcribedText;
    
    private List<Map<String, Object>> icdCodes;
    
    private List<Map<String, Object>> cptCodes;
    
    private Double confidenceScore;
    
    private Double completenessScore;
    
    private boolean hasPatientInfo;
    
    private boolean hasDiagnosis;
    
    private boolean hasTreatment;
    
    private List<String> suggestions;
    
    // Getters and setters
    public String getTranscribedText() {
        return transcribedText;
    }

    public void setTranscribedText(String transcribedText) {
        this.transcribedText = transcribedText;
    }

    public List<Map<String, Object>> getIcdCodes() {
        return icdCodes;
    }

    public void setIcdCodes(List<Map<String, Object>> icdCodes) {
        this.icdCodes = icdCodes;
    }

    public List<Map<String, Object>> getCptCodes() {
        return cptCodes;
    }

    public void setCptCodes(List<Map<String, Object>> cptCodes) {
        this.cptCodes = cptCodes;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public Double getCompletenessScore() {
        return completenessScore;
    }

    public void setCompletenessScore(Double completenessScore) {
        this.completenessScore = completenessScore;
    }

    public boolean isHasPatientInfo() {
        return hasPatientInfo;
    }

    public void setHasPatientInfo(boolean hasPatientInfo) {
        this.hasPatientInfo = hasPatientInfo;
    }

    public boolean isHasDiagnosis() {
        return hasDiagnosis;
    }

    public void setHasDiagnosis(boolean hasDiagnosis) {
        this.hasDiagnosis = hasDiagnosis;
    }

    public boolean isHasTreatment() {
        return hasTreatment;
    }

    public void setHasTreatment(boolean hasTreatment) {
        this.hasTreatment = hasTreatment;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
    
    // Added boolean getters for boolean properties (to fix the method not found errors)
    public boolean getHasDiagnosis() {
        return hasDiagnosis;
    }
    
    public boolean getHasTreatment() {
        return hasTreatment;
    }
    
    public boolean getHasPatientInfo() {
        return hasPatientInfo;
    }
}