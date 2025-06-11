package com.mediconnect.service.ai;

import com.mediconnect.model.User;
import com.mediconnect.model.Patient;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.MedicalRecord;
import com.mediconnect.model.Role;
import com.mediconnect.model.ERole;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.repository.DoctorRepository;
import com.mediconnect.repository.MedicalRecordRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.Period;

@Service
public class MedicalContextService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    /**
     * Get current authenticated user from JWT token
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        String email = authentication.getName();
        Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.orElse(null);
    }

    /**
     * Get current user's Patient profile (if user is a patient)
     */
    public Patient getCurrentPatient() {
        User user = getCurrentUser();
        if (user == null) {
            return null;
        }
        
        Optional<Patient> patientOptional = patientRepository.findByUserId(user.getId());
        return patientOptional.orElse(null);
    }

    /**
     * Get current user's Doctor profile (if user is a doctor)
     */
    public Doctor getCurrentDoctor() {
        User user = getCurrentUser();
        if (user == null) {
            return null;
        }
        
        Optional<Doctor> doctorOptional = doctorRepository.findByUserId(user.getId());
        return doctorOptional.orElse(null);
    }

    /**
     * Determine user type based on ERole enum
     * FIXED: Convert ERole to String properly
     */
    public String determineUserType(User user) {
        if (user == null || user.getRoles() == null) {
            return "UNKNOWN";
        }
        
        // FIXED: Convert Set<Role> with ERole enum to Set<ERole>
        Set<ERole> roleEnums = user.getRoles().stream()
            .map(Role::getName)  // This returns ERole enum
            .collect(Collectors.toSet());
        
        if (roleEnums.contains(ERole.ROLE_PATIENT)) {
            return "PATIENT";
        } else if (roleEnums.contains(ERole.ROLE_DOCTOR)) {
            return "DOCTOR";
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * Get complete user profile with gender, profile image, etc.
     * Fetches from Patient or Doctor entity based on user role
     */
    public Map<String, Object> getUserProfile() {
        User user = getCurrentUser();
        if (user == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> profile = new HashMap<>();
        
        // Basic user info from User entity
        profile.put("id", user.getId());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("email", user.getEmail());
        profile.put("phoneNumber", user.getPhoneNumber());
        profile.put("enabled", user.isEnabled());
        profile.put("createdAt", user.getCreatedAt());
        profile.put("updatedAt", user.getUpdatedAt());
        
        // Determine user type
        String userType = determineUserType(user);
        profile.put("userType", userType);
        
        // Get role-specific profile data
        if ("PATIENT".equals(userType)) {
            Patient patient = getCurrentPatient();
            if (patient != null) {
                addPatientProfileData(profile, patient);
            }
        } else if ("DOCTOR".equals(userType)) {
            Doctor doctor = getCurrentDoctor();
            if (doctor != null) {
                addDoctorProfileData(profile, doctor);
            }
        }
        
        return profile;
    }

    /**
     * Add patient-specific profile data
     */
    private void addPatientProfileData(Map<String, Object> profile, Patient patient) {
        profile.put("patientId", patient.getId());
        profile.put("dateOfBirth", patient.getDateOfBirth());
        profile.put("gender", patient.getGender() != null ? patient.getGender().toString() : null);
        profile.put("profileImage", patient.getProfileImage());
        profile.put("bloodGroup", patient.getBloodGroup());
        profile.put("allergies", patient.getAllergies());
        profile.put("chronicDiseases", patient.getChronicDiseases());
        profile.put("emergencyContactName", patient.getEmergencyContactName());
        profile.put("emergencyContactNumber", patient.getEmergencyContactNumber());
        profile.put("emergencyContactRelation", patient.getEmergencyContactRelation());
        profile.put("insuranceProvider", patient.getInsuranceProvider());
        profile.put("insurancePolicyNumber", patient.getInsurancePolicyNumber());
        profile.put("height", patient.getHeight());
        profile.put("weight", patient.getWeight());
        profile.put("preferredLanguage", patient.getPreferredLanguage());
        
        // Calculate age if date of birth exists
        if (patient.getDateOfBirth() != null) {
            profile.put("age", calculateAge(patient.getDateOfBirth()));
        }
        
        // Calculate BMI if height and weight exist
        if (patient.getHeight() != null && patient.getWeight() != null && patient.getHeight() > 0) {
            double heightInMeters = patient.getHeight() / 100.0;
            double bmi = patient.getWeight() / (heightInMeters * heightInMeters);
            profile.put("bmi", Math.round(bmi * 100.0) / 100.0);
        }
    }

    /**
     * Add doctor-specific profile data
     */
    private void addDoctorProfileData(Map<String, Object> profile, Doctor doctor) {
        profile.put("doctorId", doctor.getId());
        profile.put("gender", doctor.getGender());
        profile.put("profileImage", doctor.getProfileImage());
        profile.put("specialization", doctor.getSpecialization());
        profile.put("licenseNumber", doctor.getLicenseNumber());
        profile.put("education", doctor.getEducation());
        profile.put("experience", doctor.getExperience());
        profile.put("yearsOfExperience", doctor.getYearsOfExperience());
        profile.put("hospitalAffiliation", doctor.getHospitalAffiliation());
        profile.put("biography", doctor.getBiography());
        profile.put("consultationFee", doctor.getConsultationFee());
        profile.put("averageRating", doctor.getAverageRating());
        profile.put("isAvailableForEmergency", doctor.isAvailableForEmergency());
    }

    /**
     * Get medical context for AI - Only works for patients
     */
    public List<Map<String, Object>> getMedicalContext() {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return new ArrayList<>();
        }
        
        List<MedicalRecord> records = medicalRecordRepository.findByPatientIdOrderByRecordDateDesc(patient.getId());
        
        return records.stream()
                .limit(10)
                .map(this::mapMedicalRecordToContext)
                .collect(Collectors.toList());
    }

    /**
     * Get medical summary for AI prompts - Only for patients
     */
    public String getMedicalSummary() {
        User user = getCurrentUser();
        Patient patient = getCurrentPatient();
        
        if (user == null || patient == null) {
            return "";
        }
        
        StringBuilder summary = new StringBuilder();
        
        // Patient basic info
        summary.append("Patient: ").append(user.getFirstName()).append(" ").append(user.getLastName());
        
        if (patient.getDateOfBirth() != null) {
            summary.append(", Age: ").append(calculateAge(patient.getDateOfBirth()));
        }
        
        if (patient.getGender() != null) {
            summary.append(", Gender: ").append(patient.getGender());
        }
        
        if (patient.getBloodGroup() != null) {
            summary.append(", Blood Group: ").append(patient.getBloodGroup());
        }
        
        summary.append("\n");
        
        // Add allergies and chronic diseases
        if (patient.getAllergies() != null && !patient.getAllergies().trim().isEmpty()) {
            summary.append("Allergies: ").append(patient.getAllergies()).append("\n");
        }
        
        if (patient.getChronicDiseases() != null && !patient.getChronicDiseases().trim().isEmpty()) {
            summary.append("Chronic Diseases: ").append(patient.getChronicDiseases()).append("\n");
        }
        
        // Recent medical records
        List<MedicalRecord> recentRecords = medicalRecordRepository.findByPatientIdOrderByRecordDateDesc(patient.getId());
        if (!recentRecords.isEmpty()) {
            summary.append("Recent Medical History:\n");
            recentRecords.stream()
                    .limit(5)
                    .forEach(record -> {
                        summary.append("- ").append(record.getType() != null ? record.getType() : "Medical Record").append(": ");
                        if (record.getDiagnosis() != null) {
                            summary.append(record.getDiagnosis()).append(" ");
                        }
                        if (record.getSymptoms() != null) {
                            summary.append("(Symptoms: ").append(record.getSymptoms()).append(") ");
                        }
                        summary.append("\n");
                    });
        }
        
        return summary.toString();
    }

    /**
     * Get current medications - Only for patients
     */
    public List<String> getCurrentMedications() {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return new ArrayList<>();
        }
        
        List<MedicalRecord> records = medicalRecordRepository.findByPatientIdOrderByRecordDateDesc(patient.getId());
        
        return records.stream()
                .filter(record -> record.getTreatment() != null && !record.getTreatment().trim().isEmpty())
                .limit(5)
                .map(MedicalRecord::getTreatment)
                .collect(Collectors.toList());
    }

    /**
     * Get allergies and conditions - Only for patients
     */
    public Map<String, List<String>> getAllergiesAndConditions() {
        Patient patient = getCurrentPatient();
        Map<String, List<String>> result = new HashMap<>();
        result.put("allergies", new ArrayList<>());
        result.put("conditions", new ArrayList<>());
        
        if (patient == null) {
            return result;
        }
        
        // Get allergies from Patient entity
        if (patient.getAllergies() != null && !patient.getAllergies().trim().isEmpty()) {
            List<String> allergies = Arrays.stream(patient.getAllergies().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            result.put("allergies", allergies);
        }
        
        // Get chronic diseases from Patient entity
        if (patient.getChronicDiseases() != null && !patient.getChronicDiseases().trim().isEmpty()) {
            List<String> conditions = Arrays.stream(patient.getChronicDiseases().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            result.put("conditions", conditions);
        }
        
        // Also get conditions from medical records
        List<MedicalRecord> records = medicalRecordRepository.findByPatientIdOrderByRecordDateDesc(patient.getId());
        Set<String> additionalConditions = records.stream()
                .filter(record -> record.getDiagnosis() != null && !record.getDiagnosis().trim().isEmpty())
                .map(record -> record.getDiagnosis().trim())
                .collect(Collectors.toSet());
        
        result.get("conditions").addAll(additionalConditions);
        
        return result;
    }

    /**
     * Enhanced prompt with medical context
     */
    public String enhancePromptWithContext(String originalPrompt) {
        String medicalSummary = getMedicalSummary();
        Map<String, List<String>> allergiesAndConditions = getAllergiesAndConditions();
        
        StringBuilder enhancedPrompt = new StringBuilder(originalPrompt);
        
        if (!medicalSummary.trim().isEmpty()) {
            enhancedPrompt.append("\n\nPatient Medical Context:\n").append(medicalSummary);
        }
        
        if (!allergiesAndConditions.get("allergies").isEmpty()) {
            enhancedPrompt.append("\nKnown Allergies: ").append(String.join(", ", allergiesAndConditions.get("allergies")));
        }
        
        if (!allergiesAndConditions.get("conditions").isEmpty()) {
            enhancedPrompt.append("\nKnown Conditions: ").append(String.join(", ", allergiesAndConditions.get("conditions")));
        }
        
        enhancedPrompt.append("\n\nPlease consider this medical context when providing your response and mention any relevant interactions, contraindications, or personalized advice.");
        
        return enhancedPrompt.toString();
    }

    // Helper Methods
    
    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    private Map<String, Object> mapMedicalRecordToContext(MedicalRecord record) {
        Map<String, Object> context = new HashMap<>();
        context.put("id", record.getId());
        context.put("type", record.getType());
        context.put("title", record.getTitle());
        context.put("diagnosis", record.getDiagnosis());
        context.put("symptoms", record.getSymptoms());
        context.put("treatment", record.getTreatment());
        context.put("notes", record.getNotes());
        context.put("recordDate", record.getRecordDate());
        context.put("hospital", record.getHospital());
        
        // Vital signs
        Map<String, Object> vitals = new HashMap<>();
        vitals.put("temperature", record.getTemperature());
        vitals.put("bloodPressure", record.getBloodPressure());
        vitals.put("heartRate", record.getHeartRate());
        vitals.put("oxygenSaturation", record.getOxygenSaturation());
        context.put("vitalSigns", vitals);
        
        return context;
    }

    public boolean hasCondition(String condition) {
        Map<String, List<String>> allergiesAndConditions = getAllergiesAndConditions();
        return allergiesAndConditions.get("conditions").stream()
                .anyMatch(c -> c.toLowerCase().contains(condition.toLowerCase()));
    }

    public boolean hasAllergy(String allergy) {
        Map<String, List<String>> allergiesAndConditions = getAllergiesAndConditions();
        return allergiesAndConditions.get("allergies").stream()
                .anyMatch(a -> a.toLowerCase().contains(allergy.toLowerCase()));
    }

    /**
     * Get vital signs history - Only for patients
     */
    public List<Map<String, Object>> getVitalSignsHistory() {
        Patient patient = getCurrentPatient();
        if (patient == null) {
            return new ArrayList<>();
        }
        
        List<MedicalRecord> records = medicalRecordRepository.findByPatientIdOrderByRecordDateDesc(patient.getId());
        
        return records.stream()
                .filter(record -> hasVitalSigns(record))
                .limit(10)
                .map(record -> {
                    Map<String, Object> vitals = new HashMap<>();
                    vitals.put("date", record.getRecordDate());
                    vitals.put("temperature", record.getTemperature());
                    vitals.put("bloodPressure", record.getBloodPressure());
                    vitals.put("heartRate", record.getHeartRate());
                    vitals.put("oxygenSaturation", record.getOxygenSaturation());
                    return vitals;
                })
                .collect(Collectors.toList());
    }
    
    private boolean hasVitalSigns(MedicalRecord record) {
        return record.getTemperature() != null || 
               record.getBloodPressure() != null || 
               record.getHeartRate() != null || 
               record.getOxygenSaturation() != null;
    }
}