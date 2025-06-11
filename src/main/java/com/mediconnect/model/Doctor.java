package com.mediconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Basic Professional Information
    @Column(name = "specialization")
    private String specialization;
    
    @Column(name = "license_number")
    private String licenseNumber;
    
    @Column(name = "education")
    private String education;
    
    @Column(name = "experience")
    private String experience;
    
    @Column(name = "hospital_affiliation")
    private String hospitalAffiliation;
    
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;
    
    @Column(name = "consultation_fee")
    private Double consultationFee;
    
    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;
    
    @Column(name = "average_rating")
    private Integer averageRating;
    
    @Column(name = "is_available_for_emergency")
    private boolean isAvailableForEmergency;
    
    @Column(name = "profile_image")
    private String profileImage;
    
    @Column(name = "gender")
    private String gender = "MALE";

    // Extended Profile Information
    @Column(name = "about_doctor", columnDefinition = "TEXT")
    private String about;
    
    @Column(name = "patient_count")
    private String patientCount = "500+";
    
    @Column(name = "expertise", columnDefinition = "TEXT")
    private String expertise;
    
    @Column(name = "services", columnDefinition = "TEXT")
    private String services;
    
    @Column(name = "experiences", columnDefinition = "TEXT")
    private String experiences;
    
    @Column(name = "education_details", columnDefinition = "TEXT")
    private String educationDetails;
    
    @Column(name = "awards", columnDefinition = "TEXT")
    private String awards;
    
    @Column(name = "clinics", columnDefinition = "TEXT")
    private String clinics;
    
    @Column(name = "review_count")
    private Integer reviewCount = 0;
    
    // NEW: Clinic Location Information
    @Column(name = "clinic_name")
    private String clinicName;
    
    @Column(name = "clinic_address", columnDefinition = "TEXT")
    private String clinicAddress;
    
    @Column(name = "clinic_city")
    private String clinicCity;
    
    @Column(name = "clinic_state")
    private String clinicState;
    
    @Column(name = "clinic_pincode", length = 10)
    private String clinicPincode;
    
    @Column(name = "clinic_phone", length = 15)
    private String clinicPhone;
    
    // NEW: Clinic Timings (Format: "09:00-17:00" or "Closed")
    @Column(name = "monday_timing")
    private String mondayTiming;
    
    @Column(name = "tuesday_timing")
    private String tuesdayTiming;
    
    @Column(name = "wednesday_timing")
    private String wednesdayTiming;
    
    @Column(name = "thursday_timing")
    private String thursdayTiming;
    
    @Column(name = "friday_timing")
    private String fridayTiming;
    
    @Column(name = "saturday_timing")
    private String saturdayTiming;
    
    @Column(name = "sunday_timing")
    private String sundayTiming;
    
    @Column(name = "online_consultation")
    private Boolean onlineConsultation = false;

    // Relationships
    @OneToMany(mappedBy = "doctor")
    private Set<Appointment> appointments = new HashSet<>();

    // Timestamps
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { 
        createdAt = LocalDateTime.now(); 
        updatedAt = LocalDateTime.now(); 
    }
    
    @PreUpdate
    protected void onUpdate() { 
        updatedAt = LocalDateTime.now(); 
    }

    // Default Constructor
    public Doctor() {}

    // Constructors
    public Doctor(User user, String specialization, String licenseNumber) {
        this.user = user;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
    }

    // ===== GETTERS AND SETTERS =====
    
    // Basic Fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
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
    
    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    // Extended Profile Fields
    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }
    
    public String getPatientCount() { return patientCount; }
    public void setPatientCount(String patientCount) { this.patientCount = patientCount; }
    
    public String getExpertise() { return expertise; }
    public void setExpertise(String expertise) { this.expertise = expertise; }
    
    public String getServices() { return services; }
    public void setServices(String services) { this.services = services; }
    
    public String getExperiences() { return experiences; }
    public void setExperiences(String experiences) { this.experiences = experiences; }
    
    public String getEducationDetails() { return educationDetails; }
    public void setEducationDetails(String educationDetails) { this.educationDetails = educationDetails; }
    
    public String getAwards() { return awards; }
    public void setAwards(String awards) { this.awards = awards; }
    
    public String getClinics() { return clinics; }
    public void setClinics(String clinics) { this.clinics = clinics; }
    
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    
    // NEW: Clinic Location Getters/Setters
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
    
    // NEW: Clinic Timings Getters/Setters
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
    
    // Relationships
    public Set<Appointment> getAppointments() { return appointments; }
    public void setAppointments(Set<Appointment> appointments) { this.appointments = appointments; }
    
    // Timestamps
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Utility Methods
    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", specialization='" + specialization + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", yearsOfExperience=" + yearsOfExperience +
                ", consultationFee=" + consultationFee +
                ", gender='" + gender + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Doctor)) return false;
        Doctor doctor = (Doctor) o;
        return id != null && id.equals(doctor.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}