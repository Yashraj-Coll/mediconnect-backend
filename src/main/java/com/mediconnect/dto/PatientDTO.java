package com.mediconnect.dto;

import com.mediconnect.model.Patient;
import java.time.LocalDate;

public class PatientDTO {
    
    private Long id;
    private Long userId;
    private LocalDate dateOfBirth;
    private Patient.Gender gender;
    private String bloodGroup;
    private String allergies;
    private String chronicDiseases;
    private String emergencyContactNumber;
    private String profileImage;
    
    // User fields from User entity
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    
    // Default constructor
    public PatientDTO() {
    }
    
    // Constructor with fields
    public PatientDTO(Long id, Long userId, LocalDate dateOfBirth, Patient.Gender gender, 
                     String bloodGroup, Double height, Double weight, String allergies,
                     String chronicDiseases, String insuranceProvider, String insurancePolicyNumber,
                     String emergencyContactName, String emergencyContactNumber,
                     String emergencyContactRelation, String preferredLanguage, String profileImage) {
        this.id = id;
        this.userId = userId;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.allergies = allergies;
        this.chronicDiseases = chronicDiseases;
        this.emergencyContactNumber = emergencyContactNumber;
        this.profileImage = profileImage;
    }
    
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
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public Patient.Gender getGender() {
        return gender;
    }
    
    public void setGender(Patient.Gender gender) {
        this.gender = gender;
    }
    
    public String getBloodGroup() {
        return bloodGroup;
    }
    
    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }
    
    public String getAllergies() {
        return allergies;
    }
    
    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }
    
    public String getChronicDiseases() {
        return chronicDiseases;
    }
    
    public void setChronicDiseases(String chronicDiseases) {
        this.chronicDiseases = chronicDiseases;
    }
    
    public String getEmergencyContactNumber() {
        return emergencyContactNumber;
    }
    
    public void setEmergencyContactNumber(String emergencyContactNumber) {
        this.emergencyContactNumber = emergencyContactNumber;
    }
    
    public String getProfileImage() {
        return profileImage;
    }
    
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
    
    // User field getters and setters
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    // Conversion methods
    public static PatientDTO fromEntity(Patient patient) {
        PatientDTO dto = new PatientDTO();
        
        // Patient fields
        dto.setId(patient.getId());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setGender(patient.getGender());
        dto.setBloodGroup(patient.getBloodGroup());        
        dto.setAllergies(patient.getAllergies());
        dto.setChronicDiseases(patient.getChronicDiseases());        
        dto.setEmergencyContactNumber(patient.getEmergencyContactNumber());        
        dto.setProfileImage(patient.getProfileImage());
        
        // User fields - THIS IS THE KEY FIX
        if (patient.getUser() != null) {
            dto.setUserId(patient.getUser().getId());
            dto.setFirstName(patient.getUser().getFirstName());
            dto.setLastName(patient.getUser().getLastName());
            dto.setEmail(patient.getUser().getEmail());
            dto.setPhoneNumber(patient.getUser().getPhoneNumber());
        }
        
        return dto;
    }
    
    public Patient toEntity() {
        Patient patient = new Patient();
        
        patient.setId(this.getId());
        patient.setDateOfBirth(this.getDateOfBirth());
        patient.setGender(this.getGender());
        patient.setBloodGroup(this.getBloodGroup());
        patient.setAllergies(this.getAllergies());
        patient.setChronicDiseases(this.getChronicDiseases());
        patient.setEmergencyContactNumber(this.getEmergencyContactNumber());        
        patient.setProfileImage(this.getProfileImage());
        
        return patient;
    }
}