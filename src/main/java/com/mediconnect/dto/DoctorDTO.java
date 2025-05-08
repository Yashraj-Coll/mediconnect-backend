package com.mediconnect.dto;

import java.util.List;

public class DoctorDTO {
    
    private Long id;
    private Long userId;
    private String specialization;
    private String licenseNumber;
    private String education;
    private String experience;
    private String hospitalAffiliation;
    private Integer yearsOfExperience;
    private Double consultationFee;
    private String biography;
    private Integer averageRating;
    private boolean isAvailableForEmergency;
    private List<String> languages; // Added field for languages
    private String profileImage; // Added field for profile image URL
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getSpecialization() {
        return specialization;
    }
    
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
    
    public String getLicenseNumber() {
        return licenseNumber;
    }
    
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
    
    public String getEducation() {
        return education;
    }
    
    public void setEducation(String education) {
        this.education = education;
    }
    
    public String getExperience() {
        return experience;
    }
    
    public void setExperience(String experience) {
        this.experience = experience;
    }
    
    public String getHospitalAffiliation() {
        return hospitalAffiliation;
    }
    
    public void setHospitalAffiliation(String hospitalAffiliation) {
        this.hospitalAffiliation = hospitalAffiliation;
    }
    
    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }
    
    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }
    
    public Double getConsultationFee() {
        return consultationFee;
    }
    
    public void setConsultationFee(Double consultationFee) {
        this.consultationFee = consultationFee;
    }
    
    public String getBiography() {
        return biography;
    }
    
    public void setBiography(String biography) {
        this.biography = biography;
    }
    
    public Integer getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(Integer averageRating) {
        this.averageRating = averageRating;
    }
    
    public boolean isAvailableForEmergency() {
        return isAvailableForEmergency;
    }
    
    public void setAvailableForEmergency(boolean isAvailableForEmergency) {
        this.isAvailableForEmergency = isAvailableForEmergency;
    }
    
    public List<String> getLanguages() {
        return languages;
    }
    
    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }
    
    public String getProfileImage() {
        return profileImage;
    }
    
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}