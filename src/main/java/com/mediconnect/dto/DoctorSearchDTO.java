package com.mediconnect.dto;

public class DoctorSearchDTO {
    
    private String name;
    private String specialization;
    private String hospitalAffiliation;
    private Integer minYearsExperience;
    private Double maxFee;
    private Integer minRating;
    private Boolean isEmergencyAvailable;
    private String language;
    private String insuranceProvider;
    private String dayOfWeek;
    private String time;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "id";
    private String sortDirection = "ASC";
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSpecialization() {
        return specialization;
    }
    
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
    
    public String getHospitalAffiliation() {
        return hospitalAffiliation;
    }
    
    public void setHospitalAffiliation(String hospitalAffiliation) {
        this.hospitalAffiliation = hospitalAffiliation;
    }
    
    public Integer getMinYearsExperience() {
        return minYearsExperience;
    }
    
    public void setMinYearsExperience(Integer minYearsExperience) {
        this.minYearsExperience = minYearsExperience;
    }
    
    public Double getMaxFee() {
        return maxFee;
    }
    
    public void setMaxFee(Double maxFee) {
        this.maxFee = maxFee;
    }
    
    public Integer getMinRating() {
        return minRating;
    }
    
    public void setMinRating(Integer minRating) {
        this.minRating = minRating;
    }
    
    public Boolean getIsEmergencyAvailable() {
        return isEmergencyAvailable;
    }
    
    public void setIsEmergencyAvailable(Boolean isEmergencyAvailable) {
        this.isEmergencyAvailable = isEmergencyAvailable;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getInsuranceProvider() {
        return insuranceProvider;
    }
    
    public void setInsuranceProvider(String insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
    }
    
    public String getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortDirection() {
        return sortDirection;
    }
    
    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}