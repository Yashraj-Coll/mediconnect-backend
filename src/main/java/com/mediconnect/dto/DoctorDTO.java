package com.mediconnect.dto;

import java.util.List;
import java.util.Map;

public class DoctorDTO {
    
    // Basic Information
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String gender;
    
    // Professional Information
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
    private List<String> languages;
    private String profileImage;
    
    // Extended Profile Information
    private String about;
    private String patientCount;
    private List<String> expertise;
    private List<String> services;
    private List<Map<String, Object>> experiences;
    private List<Map<String, Object>> educationDetails;
    private List<Map<String, Object>> awards;
    private List<Map<String, Object>> clinics;
    private List<Map<String, Object>> reviews;
    private Integer reviewCount;
    
    // NEW: Clinic Location Information
    private String clinicName;
    private String clinicAddress;
    private String clinicCity;
    private String clinicState;
    private String clinicPincode;
    private String clinicPhone;
    
    // NEW: Clinic Timings
    private String mondayTiming;
    private String tuesdayTiming;
    private String wednesdayTiming;
    private String thursdayTiming;
    private String fridayTiming;
    private String saturdayTiming;
    private String sundayTiming;
    private Boolean onlineConsultation;
    
    // NEW: Areas of Expertise for frontend display
    private List<String> areasOfExpertise;
    private List<String> servicesOffered;
    private List<Map<String, Object>> professionalExperience;
    private List<Map<String, Object>> educationTraining;
    private List<Map<String, Object>> awardsRecognitions;

    // Default Constructor
    public DoctorDTO() {}

    // Constructor with basic fields
    public DoctorDTO(Long id, String firstName, String lastName, String specialization) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
    }

    // ===== GETTERS AND SETTERS =====
    
    // Basic Information
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    // Professional Information
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    
    public String getHospitalAffiliation() { return hospitalAffiliation; }
    public void setHospitalAffiliation(String hospitalAffiliation) { this.hospitalAffiliation = hospitalAffiliation; }
    
    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }
    
    public Double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(Double consultationFee) { this.consultationFee = consultationFee; }
    
    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }
    
    public Integer getAverageRating() { return averageRating; }
    public void setAverageRating(Integer averageRating) { this.averageRating = averageRating; }
    
    public boolean isAvailableForEmergency() { return isAvailableForEmergency; }
    public void setAvailableForEmergency(boolean isAvailableForEmergency) { this.isAvailableForEmergency = isAvailableForEmergency; }
    
    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }
    
    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    
    // Extended Profile Information
    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }
    
    public String getPatientCount() { return patientCount; }
    public void setPatientCount(String patientCount) { this.patientCount = patientCount; }
    
    public List<String> getExpertise() { return expertise; }
    public void setExpertise(List<String> expertise) { this.expertise = expertise; }
    
    public List<String> getServices() { return services; }
    public void setServices(List<String> services) { this.services = services; }
    
    public List<Map<String, Object>> getExperiences() { return experiences; }
    public void setExperiences(List<Map<String, Object>> experiences) { this.experiences = experiences; }
    
    public List<Map<String, Object>> getEducationDetails() { return educationDetails; }
    public void setEducationDetails(List<Map<String, Object>> educationDetails) { this.educationDetails = educationDetails; }
    
    public List<Map<String, Object>> getAwards() { return awards; }
    public void setAwards(List<Map<String, Object>> awards) { this.awards = awards; }
    
    public List<Map<String, Object>> getClinics() { return clinics; }
    public void setClinics(List<Map<String, Object>> clinics) { this.clinics = clinics; }
    
    public List<Map<String, Object>> getReviews() { return reviews; }
    public void setReviews(List<Map<String, Object>> reviews) { this.reviews = reviews; }
    
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    
    // NEW: Clinic Location Information
    public String getClinicName() { return clinicName; }
    public void setClinicName(String clinicName) { this.clinicName = clinicName; }
    
    public String getClinicAddress() { return clinicAddress; }
    public void setClinicAddress(String clinicAddress) { this.clinicAddress = clinicAddress; }
    
    public String getClinicCity() { return clinicCity; }
    public void setClinicCity(String clinicCity) { this.clinicCity = clinicCity; }
    
    public String getClinicState() { return clinicState; }
    public void setClinicState(String clinicState) { this.clinicState = clinicState; }
    
    public String getClinicPincode() { return clinicPincode; }
    public void setClinicPincode(String clinicPincode) { this.clinicPincode = clinicPincode; }
    
    public String getClinicPhone() { return clinicPhone; }
    public void setClinicPhone(String clinicPhone) { this.clinicPhone = clinicPhone; }
    
    // NEW: Clinic Timings
    public String getMondayTiming() { return mondayTiming; }
    public void setMondayTiming(String mondayTiming) { this.mondayTiming = mondayTiming; }
    
    public String getTuesdayTiming() { return tuesdayTiming; }
    public void setTuesdayTiming(String tuesdayTiming) { this.tuesdayTiming = tuesdayTiming; }
    
    public String getWednesdayTiming() { return wednesdayTiming; }
    public void setWednesdayTiming(String wednesdayTiming) { this.wednesdayTiming = wednesdayTiming; }
    
    public String getThursdayTiming() { return thursdayTiming; }
    public void setThursdayTiming(String thursdayTiming) { this.thursdayTiming = thursdayTiming; }
    
    public String getFridayTiming() { return fridayTiming; }
    public void setFridayTiming(String fridayTiming) { this.fridayTiming = fridayTiming; }
    
    public String getSaturdayTiming() { return saturdayTiming; }
    public void setSaturdayTiming(String saturdayTiming) { this.saturdayTiming = saturdayTiming; }
    
    public String getSundayTiming() { return sundayTiming; }
    public void setSundayTiming(String sundayTiming) { this.sundayTiming = sundayTiming; }
    
    public Boolean getOnlineConsultation() { return onlineConsultation; }
    public void setOnlineConsultation(Boolean onlineConsultation) { this.onlineConsultation = onlineConsultation; }
    
    // NEW: Frontend Display Properties
    public List<String> getAreasOfExpertise() { return areasOfExpertise; }
    public void setAreasOfExpertise(List<String> areasOfExpertise) { this.areasOfExpertise = areasOfExpertise; }
    
    public List<String> getServicesOffered() { return servicesOffered; }
    public void setServicesOffered(List<String> servicesOffered) { this.servicesOffered = servicesOffered; }
    
    public List<Map<String, Object>> getProfessionalExperience() { return professionalExperience; }
    public void setProfessionalExperience(List<Map<String, Object>> professionalExperience) { this.professionalExperience = professionalExperience; }
    
    public List<Map<String, Object>> getEducationTraining() { return educationTraining; }
    public void setEducationTraining(List<Map<String, Object>> educationTraining) { this.educationTraining = educationTraining; }
    
    public List<Map<String, Object>> getAwardsRecognitions() { return awardsRecognitions; }
    public void setAwardsRecognitions(List<Map<String, Object>> awardsRecognitions) { this.awardsRecognitions = awardsRecognitions; }

    // Utility Methods
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public String getDoctorName() {
        return "Dr. " + getFullName().trim();
    }
    
    // NEW: Get clinic timing for a specific day
    public String getTimingForDay(String day) {
        switch (day.toLowerCase()) {
            case "monday": return mondayTiming;
            case "tuesday": return tuesdayTiming;
            case "wednesday": return wednesdayTiming;
            case "thursday": return thursdayTiming;
            case "friday": return fridayTiming;
            case "saturday": return saturdayTiming;
            case "sunday": return sundayTiming;
            default: return null;
        }
    }
    
    // NEW: Check if clinic is open on a specific day
    public boolean isOpenOnDay(String day) {
        String timing = getTimingForDay(day);
        return timing != null && !timing.equalsIgnoreCase("closed") && !timing.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "DoctorDTO{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", specialization='" + specialization + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", yearsOfExperience=" + yearsOfExperience +
                ", consultationFee=" + consultationFee +
                ", gender='" + gender + '\'' +
                ", isAvailableForEmergency=" + isAvailableForEmergency +
                ", clinicName='" + clinicName + '\'' +
                ", clinicCity='" + clinicCity + '\'' +
                '}';
    }
}