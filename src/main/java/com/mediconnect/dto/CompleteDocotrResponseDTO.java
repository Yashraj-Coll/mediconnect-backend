package com.mediconnect.dto;

import java.util.List;

/**
 * DTO for returning complete doctor information including
 * - Basic information
 * - Languages
 * - Appointment statistics
 * - Rating statistics
 * - Available slots
 */
public class CompleteDocotrResponseDTO {
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String specialization;
    private String licenseNumber;
    private String education;
    private String experience;
    private String hospitalAffiliation;
    private Integer yearsOfExperience;
    private Double consultationFee;
    private String biography;
    private Integer averageRating;
    private Boolean isAvailableForEmergency;
    private List<String> languages;
    private String profileImage;
    
    // Appointment statistics
    private Integer totalAppointments;
    private Integer completedAppointments;
    private Integer cancelledAppointments;
    
    // Rating statistics
    private Integer ratingCount;
    private Integer fiveStarRatings;
    private Integer fourStarRatings;
    private Integer threeStarRatings;
    private Integer twoStarRatings;
    private Integer oneStarRatings;
    
    // Available slots
    private List<SlotDTO> availableSlots;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
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
    public Boolean getIsAvailableForEmergency() {
        return isAvailableForEmergency;
    }
    public void setIsAvailableForEmergency(Boolean isAvailableForEmergency) {
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
    public Integer getTotalAppointments() {
        return totalAppointments;
    }
    public void setTotalAppointments(Integer totalAppointments) {
        this.totalAppointments = totalAppointments;
    }
    public Integer getCompletedAppointments() {
        return completedAppointments;
    }
    public void setCompletedAppointments(Integer completedAppointments) {
        this.completedAppointments = completedAppointments;
    }
    public Integer getCancelledAppointments() {
        return cancelledAppointments;
    }
    public void setCancelledAppointments(Integer cancelledAppointments) {
        this.cancelledAppointments = cancelledAppointments;
    }
    public Integer getRatingCount() {
        return ratingCount;
    }
    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }
    public Integer getFiveStarRatings() {
        return fiveStarRatings;
    }
    public void setFiveStarRatings(Integer fiveStarRatings) {
        this.fiveStarRatings = fiveStarRatings;
    }
    public Integer getFourStarRatings() {
        return fourStarRatings;
    }
    public void setFourStarRatings(Integer fourStarRatings) {
        this.fourStarRatings = fourStarRatings;
    }
    public Integer getThreeStarRatings() {
        return threeStarRatings;
    }
    public void setThreeStarRatings(Integer threeStarRatings) {
        this.threeStarRatings = threeStarRatings;
    }
    public Integer getTwoStarRatings() {
        return twoStarRatings;
    }
    public void setTwoStarRatings(Integer twoStarRatings) {
        this.twoStarRatings = twoStarRatings;
    }
    public Integer getOneStarRatings() {
        return oneStarRatings;
    }
    public void setOneStarRatings(Integer oneStarRatings) {
        this.oneStarRatings = oneStarRatings;
    }
    public List<SlotDTO> getAvailableSlots() {
        return availableSlots;
    }
    public void setAvailableSlots(List<SlotDTO> availableSlots) {
        this.availableSlots = availableSlots;
    }
    
    /**
     * Inner class for representing available time slots
     */
    public static class SlotDTO {
        private String date;
        private String startTime;
        private String endTime;
        private Boolean isBooked;
        
        public String getDate() {
            return date;
        }
        public void setDate(String date) {
            this.date = date;
        }
        public String getStartTime() {
            return startTime;
        }
        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }
        public String getEndTime() {
            return endTime;
        }
        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
        public Boolean getIsBooked() {
            return isBooked;
        }
        public void setIsBooked(Boolean isBooked) {
            this.isBooked = isBooked;
        }
    }
}