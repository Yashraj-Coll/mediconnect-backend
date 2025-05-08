package com.mediconnect.dto;

/**
 * DTO for transferring Doctor Language data between layers
 */
public class DoctorLanguageDTO {
    
    private Long id;
    private Long doctorId;
    private String language;
    
    // Constructors
    public DoctorLanguageDTO() {
    }
    
    public DoctorLanguageDTO(Long id, Long doctorId, String language) {
        this.id = id;
        this.doctorId = doctorId;
        this.language = language;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    @Override
    public String toString() {
        return "DoctorLanguageDTO [id=" + id + ", doctorId=" + doctorId + ", language=" + language + "]";
    }
}