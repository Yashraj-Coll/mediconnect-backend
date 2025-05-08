package com.mediconnect.dto.request;

import lombok.Data;

/**
 * DTO for medical requests such as consultations, prescriptions, etc.
 */
@Data
public class MedicalRequest {
    
    private String patientId;
    private String text; // For additional details or notes
    private String symptoms;
    private String diagnosis;
    private String treatment;
    
    // Constructors
    public MedicalRequest() {
    }
    
    public MedicalRequest(String patientId, String text, String symptoms, String diagnosis, String treatment) {
        this.patientId = patientId;
        this.text = text;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
    }
    
    // Getters and Setters
    public String getPatientId() {
        return patientId;
    }
    
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getSymptoms() {
        return symptoms;
    }
    
    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }
    
    public String getDiagnosis() {
        return diagnosis;
    }
    
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
    
    public String getTreatment() {
        return treatment;
    }
    
    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }
    
    /**
     * Get detailed symptoms information including additional text if available
     * @return Combined symptoms and additional text
     */
    public String getDetailedSymptoms() {
        if (text != null && !text.isEmpty()) {
            return symptoms + " - Additional details: " + text;
        }
        return symptoms;
    }
    
    @Override
    public String toString() {
        return "MedicalRequest [patientId=" + patientId + ", symptoms=" + symptoms + 
               ", diagnosis=" + diagnosis + ", treatment=" + treatment + "]";
    }
}