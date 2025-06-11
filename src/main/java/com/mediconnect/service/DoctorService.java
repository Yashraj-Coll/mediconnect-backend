package com.mediconnect.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mediconnect.dto.DoctorDTO;
import com.mediconnect.dto.DoctorLanguageDTO;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.DoctorLanguage;
import com.mediconnect.model.ERole;
import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import com.mediconnect.repository.DoctorLanguageRepository;
import com.mediconnect.repository.DoctorRepository;
import com.mediconnect.repository.RoleRepository;
import com.mediconnect.repository.UserRepository;

@Service
public class DoctorService {
    
    private final Path uploadDir = Paths.get("uploads");
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private DoctorLanguageRepository doctorLanguageRepository;
    
    // Basic CRUD Operations
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }
    
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
    }
    
    public Optional<Doctor> getDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId);
    }
    
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }
    
    public List<Doctor> getAvailableDoctorsForEmergency() {
        return doctorRepository.findAvailableForEmergency();
    }
    
    public List<Doctor> searchDoctors(String keyword) {
        return doctorRepository.searchDoctors(keyword);
    }
    
    public List<Doctor> getDoctorsByMaxConsultationFee(Double maxFee) {
        return doctorRepository.findByConsultationFeeLessThanEqual(maxFee);
    }
    
    public List<Doctor> getTopRatedDoctors() {
        return doctorRepository.findTopRatedDoctors();
    }
    
    // Language Management
    public List<DoctorLanguageDTO> getDoctorLanguages(Long doctorId) {
        List<DoctorLanguage> languages = doctorLanguageRepository.findByDoctorId(doctorId);
        return languages.stream()
                .map(lang -> {
                    DoctorLanguageDTO dto = new DoctorLanguageDTO();
                    dto.setId(lang.getId());
                    dto.setDoctorId(doctorId);
                    dto.setLanguage(lang.getLanguage());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    public List<String> getDoctorLanguagesAsStringList(Long doctorId) {
        List<DoctorLanguage> languages = doctorLanguageRepository.findByDoctorId(doctorId);
        return languages.stream()
                .map(DoctorLanguage::getLanguage)
                .collect(Collectors.toList());
    }
    
    // Doctor Profile Management
    @Transactional
    public Doctor createDoctor(DoctorDTO doctorDTO) {
        User user = userRepository.findById(doctorDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + doctorDTO.getUserId()));
        
        if (doctorRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("User already has a doctor profile");
        }
        
        Role doctorRole = roleRepository.findByName(ERole.ROLE_DOCTOR)
                .orElseThrow(() -> new RuntimeException("Error: Doctor Role not found"));
        
        if (!user.getRoles().contains(doctorRole)) {
            user.getRoles().add(doctorRole);
            userRepository.save(user);
        }
        
        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setSpecialization(doctorDTO.getSpecialization());
        doctor.setLicenseNumber(doctorDTO.getLicenseNumber());
        doctor.setEducation(doctorDTO.getEducation());
        doctor.setExperience(doctorDTO.getExperience());
        doctor.setHospitalAffiliation(doctorDTO.getHospitalAffiliation());
        doctor.setYearsOfExperience(doctorDTO.getYearsOfExperience());
        doctor.setConsultationFee(doctorDTO.getConsultationFee());
        doctor.setBiography(doctorDTO.getBiography());
        doctor.setAverageRating(doctorDTO.getAverageRating());
        doctor.setAvailableForEmergency(doctorDTO.isAvailableForEmergency());
        doctor.setProfileImage(doctorDTO.getProfileImage());
        doctor.setGender(doctorDTO.getGender());
        
        // Set extended profile fields
        doctor.setAbout(doctorDTO.getAbout());
        doctor.setPatientCount(doctorDTO.getPatientCount());
        
        // Set clinic information
        doctor.setClinicName(doctorDTO.getClinicName());
        doctor.setClinicAddress(doctorDTO.getClinicAddress());
        doctor.setClinicCity(doctorDTO.getClinicCity());
        doctor.setClinicState(doctorDTO.getClinicState());
        doctor.setClinicPincode(doctorDTO.getClinicPincode());
        doctor.setClinicPhone(doctorDTO.getClinicPhone());
        doctor.setOnlineConsultation(doctorDTO.getOnlineConsultation());
        
        // Set clinic timings
        doctor.setMondayTiming(doctorDTO.getMondayTiming());
        doctor.setTuesdayTiming(doctorDTO.getTuesdayTiming());
        doctor.setWednesdayTiming(doctorDTO.getWednesdayTiming());
        doctor.setThursdayTiming(doctorDTO.getThursdayTiming());
        doctor.setFridayTiming(doctorDTO.getFridayTiming());
        doctor.setSaturdayTiming(doctorDTO.getSaturdayTiming());
        doctor.setSundayTiming(doctorDTO.getSundayTiming());
        
        Doctor savedDoctor = doctorRepository.save(doctor);
        
        // Add languages
        if (doctorDTO.getLanguages() != null && !doctorDTO.getLanguages().isEmpty()) {
            for (String language : doctorDTO.getLanguages()) {
                DoctorLanguage doctorLanguage = new DoctorLanguage();
                doctorLanguage.setDoctor(savedDoctor);
                doctorLanguage.setLanguage(language);
                doctorLanguageRepository.save(doctorLanguage);
            }
        } else {
            // Add default languages
            String[] defaultLanguages = {"English", "Hindi"};
            for (String language : defaultLanguages) {
                DoctorLanguage doctorLanguage = new DoctorLanguage();
                doctorLanguage.setDoctor(savedDoctor);
                doctorLanguage.setLanguage(language);
                doctorLanguageRepository.save(doctorLanguage);
            }
        }
        
        return savedDoctor;
    }
    
    @Transactional
    public Doctor updateDoctor(Long id, DoctorDTO doctorDTO) {
        Doctor doctor = getDoctorById(id);
        
        // Update basic fields
        if (doctorDTO.getSpecialization() != null) {
            doctor.setSpecialization(doctorDTO.getSpecialization());
        }
        if (doctorDTO.getLicenseNumber() != null) {
            doctor.setLicenseNumber(doctorDTO.getLicenseNumber());
        }
        if (doctorDTO.getEducation() != null) {
            doctor.setEducation(doctorDTO.getEducation());
        }
        if (doctorDTO.getExperience() != null) {
            doctor.setExperience(doctorDTO.getExperience());
        }
        if (doctorDTO.getHospitalAffiliation() != null) {
            doctor.setHospitalAffiliation(doctorDTO.getHospitalAffiliation());
        }
        if (doctorDTO.getYearsOfExperience() != null) {
            doctor.setYearsOfExperience(doctorDTO.getYearsOfExperience());
        }
        if (doctorDTO.getConsultationFee() != null) {
            doctor.setConsultationFee(doctorDTO.getConsultationFee());
        }
        if (doctorDTO.getBiography() != null) {
            doctor.setBiography(doctorDTO.getBiography());
        }
        if (doctorDTO.getAverageRating() != null) {
            doctor.setAverageRating(doctorDTO.getAverageRating());
        }
        if (doctorDTO.getProfileImage() != null) {
            doctor.setProfileImage(doctorDTO.getProfileImage());
        }
        
        // Update extended profile fields
        if (doctorDTO.getAbout() != null) {
            doctor.setAbout(doctorDTO.getAbout());
        }
        if (doctorDTO.getPatientCount() != null) {
            doctor.setPatientCount(doctorDTO.getPatientCount());
        }
        
        // Update clinic information
        if (doctorDTO.getClinicName() != null) {
            doctor.setClinicName(doctorDTO.getClinicName());
        }
        if (doctorDTO.getClinicAddress() != null) {
            doctor.setClinicAddress(doctorDTO.getClinicAddress());
        }
        if (doctorDTO.getClinicCity() != null) {
            doctor.setClinicCity(doctorDTO.getClinicCity());
        }
        if (doctorDTO.getClinicState() != null) {
            doctor.setClinicState(doctorDTO.getClinicState());
        }
        if (doctorDTO.getClinicPincode() != null) {
            doctor.setClinicPincode(doctorDTO.getClinicPincode());
        }
        if (doctorDTO.getClinicPhone() != null) {
            doctor.setClinicPhone(doctorDTO.getClinicPhone());
        }
        if (doctorDTO.getOnlineConsultation() != null) {
            doctor.setOnlineConsultation(doctorDTO.getOnlineConsultation());
        }
        
        // Update clinic timings
        if (doctorDTO.getMondayTiming() != null) {
            doctor.setMondayTiming(doctorDTO.getMondayTiming());
        }
        if (doctorDTO.getTuesdayTiming() != null) {
            doctor.setTuesdayTiming(doctorDTO.getTuesdayTiming());
        }
        if (doctorDTO.getWednesdayTiming() != null) {
            doctor.setWednesdayTiming(doctorDTO.getWednesdayTiming());
        }
        if (doctorDTO.getThursdayTiming() != null) {
            doctor.setThursdayTiming(doctorDTO.getThursdayTiming());
        }
        if (doctorDTO.getFridayTiming() != null) {
            doctor.setFridayTiming(doctorDTO.getFridayTiming());
        }
        if (doctorDTO.getSaturdayTiming() != null) {
            doctor.setSaturdayTiming(doctorDTO.getSaturdayTiming());
        }
        if (doctorDTO.getSundayTiming() != null) {
            doctor.setSundayTiming(doctorDTO.getSundayTiming());
        }
        
        doctor.setAvailableForEmergency(doctorDTO.isAvailableForEmergency());
        
        Doctor updatedDoctor = doctorRepository.save(doctor);
        
        // Update languages
        if (doctorDTO.getLanguages() != null && !doctorDTO.getLanguages().isEmpty()) {
            doctorLanguageRepository.deleteByDoctorId(id);
            for (String language : doctorDTO.getLanguages()) {
                DoctorLanguage doctorLanguage = new DoctorLanguage();
                doctorLanguage.setDoctor(updatedDoctor);
                doctorLanguage.setLanguage(language);
                doctorLanguageRepository.save(doctorLanguage);
            }
        }
        
        return updatedDoctor;
    }
    
    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorById(id);
        
        // Delete related languages first
        doctorLanguageRepository.deleteByDoctorId(id);
        
        // Delete doctor profile
        doctorRepository.delete(doctor);
    }
    
    // Language Management Methods
    @Transactional
    public DoctorLanguageDTO addDoctorLanguage(Long doctorId, String language) {
        Doctor doctor = getDoctorById(doctorId);
        List<DoctorLanguage> existingLanguages = doctorLanguageRepository.findByDoctorId(doctorId);
        
        // Check if language already exists
        for (DoctorLanguage existingLang : existingLanguages) {
            if (existingLang.getLanguage().equalsIgnoreCase(language)) {
                DoctorLanguageDTO dto = new DoctorLanguageDTO();
                dto.setId(existingLang.getId());
                dto.setDoctorId(doctorId);
                dto.setLanguage(existingLang.getLanguage());
                return dto;
            }
        }
        
        // Add new language
        DoctorLanguage doctorLanguage = new DoctorLanguage();
        doctorLanguage.setDoctor(doctor);
        doctorLanguage.setLanguage(language);
        DoctorLanguage savedLanguage = doctorLanguageRepository.save(doctorLanguage);
        
        DoctorLanguageDTO dto = new DoctorLanguageDTO();
        dto.setId(savedLanguage.getId());
        dto.setDoctorId(doctorId);
        dto.setLanguage(savedLanguage.getLanguage());
        
        return dto;
    }
    
    @Transactional
    public void removeDoctorLanguage(Long doctorId, String language) {
        List<DoctorLanguage> languages = doctorLanguageRepository.findByDoctorId(doctorId);
        for (DoctorLanguage doctorLanguage : languages) {
            if (doctorLanguage.getLanguage().equalsIgnoreCase(language)) {
                doctorLanguageRepository.delete(doctorLanguage);
                break;
            }
        }
    }
    
    // File Upload Methods
    public void init() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }
    
    @Transactional
    public String storeProfileImage(Long doctorId, MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file");
            }
            
            Files.createDirectories(uploadDir);
            String filename = file.getOriginalFilename();
            Path targetPath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
            doctor.setProfileImage(filename);
            doctorRepository.save(doctor);
            
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }
    
    // Advanced Search Methods
    public List<Doctor> searchDoctorsByCriteria(
            String name, 
            String specialization, 
            String city, 
            String state,
            Boolean emergency, 
            Boolean onlineConsultation,
            Double maxFee,
            Integer minRating) {
        
        List<Doctor> doctors = doctorRepository.findAll();
        
        return doctors.stream()
            .filter(doctor -> {
                // Filter by name
                if (name != null && !name.trim().isEmpty()) {
                    String fullName = (doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName()).toLowerCase();
                    if (!fullName.contains(name.toLowerCase())) {
                        return false;
                    }
                }
                
                // Filter by specialization
                if (specialization != null && !specialization.trim().isEmpty()) {
                    if (doctor.getSpecialization() == null || 
                        !doctor.getSpecialization().toLowerCase().contains(specialization.toLowerCase())) {
                        return false;
                    }
                }
                
                // Filter by city
                if (city != null && !city.trim().isEmpty()) {
                    if (doctor.getClinicCity() == null || 
                        !doctor.getClinicCity().toLowerCase().contains(city.toLowerCase())) {
                        return false;
                    }
                }
                
                // Filter by state
                if (state != null && !state.trim().isEmpty()) {
                    if (doctor.getClinicState() == null || 
                        !doctor.getClinicState().toLowerCase().contains(state.toLowerCase())) {
                        return false;
                    }
                }
                
                // Filter by emergency availability
                if (emergency != null && emergency && !doctor.isAvailableForEmergency()) {
                    return false;
                }
                
                // Filter by online consultation
                if (onlineConsultation != null && onlineConsultation && 
                    (doctor.getOnlineConsultation() == null || !doctor.getOnlineConsultation())) {
                    return false;
                }
                
                // Filter by consultation fee
                if (maxFee != null && doctor.getConsultationFee() != null && 
                    doctor.getConsultationFee() > maxFee) {
                    return false;
                }
                
                // Filter by rating
                if (minRating != null && doctor.getAverageRating() != null && 
                    doctor.getAverageRating() < minRating) {
                    return false;
                }
                
                return true;
            })
            .collect(Collectors.toList());
    }
    
    // Clinic Management Methods
    @Transactional
    public Doctor updateClinicInfo(Long doctorId, Map<String, Object> clinicData) {
        Doctor doctor = getDoctorById(doctorId);
        
        if (clinicData.containsKey("clinicName")) {
            doctor.setClinicName((String) clinicData.get("clinicName"));
        }
        if (clinicData.containsKey("clinicAddress")) {
            doctor.setClinicAddress((String) clinicData.get("clinicAddress"));
        }
        if (clinicData.containsKey("clinicCity")) {
            doctor.setClinicCity((String) clinicData.get("clinicCity"));
        }
        if (clinicData.containsKey("clinicState")) {
            doctor.setClinicState((String) clinicData.get("clinicState"));
        }
        if (clinicData.containsKey("clinicPincode")) {
            doctor.setClinicPincode((String) clinicData.get("clinicPincode"));
        }
        if (clinicData.containsKey("clinicPhone")) {
            doctor.setClinicPhone((String) clinicData.get("clinicPhone"));
        }
        if (clinicData.containsKey("onlineConsultation")) {
            doctor.setOnlineConsultation((Boolean) clinicData.get("onlineConsultation"));
        }
        
        return doctorRepository.save(doctor);
    }
    
    @Transactional
    public Doctor updateClinicTimings(Long doctorId, Map<String, String> timings) {
        Doctor doctor = getDoctorById(doctorId);
        
        if (timings.containsKey("mondayTiming")) {
            doctor.setMondayTiming(timings.get("mondayTiming"));
        }
        if (timings.containsKey("tuesdayTiming")) {
            doctor.setTuesdayTiming(timings.get("tuesdayTiming"));
        }
        if (timings.containsKey("wednesdayTiming")) {
            doctor.setWednesdayTiming(timings.get("wednesdayTiming"));
        }
        if (timings.containsKey("thursdayTiming")) {
            doctor.setThursdayTiming(timings.get("thursdayTiming"));
        }
        if (timings.containsKey("fridayTiming")) {
            doctor.setFridayTiming(timings.get("fridayTiming"));
        }
        if (timings.containsKey("saturdayTiming")) {
            doctor.setSaturdayTiming(timings.get("saturdayTiming"));
        }
        if (timings.containsKey("sundayTiming")) {
            doctor.setSundayTiming(timings.get("sundayTiming"));
        }
        
        return doctorRepository.save(doctor);
    }
    
    // Utility Methods
    public Map<String, Object> getClinicInfo(Long doctorId) {
        Doctor doctor = getDoctorById(doctorId);
        
        Map<String, Object> clinicInfo = new HashMap<>();
        clinicInfo.put("clinicName", doctor.getClinicName());
        clinicInfo.put("clinicAddress", doctor.getClinicAddress());
        clinicInfo.put("clinicCity", doctor.getClinicCity());
        clinicInfo.put("clinicState", doctor.getClinicState());
        clinicInfo.put("clinicPincode", doctor.getClinicPincode());
        clinicInfo.put("clinicPhone", doctor.getClinicPhone());
        clinicInfo.put("onlineConsultation", doctor.getOnlineConsultation());
        
        return clinicInfo;
    }
    
    public Map<String, String> getClinicTimings(Long doctorId) {
        Doctor doctor = getDoctorById(doctorId);
        
        Map<String, String> timings = new HashMap<>();
        timings.put("mondayTiming", doctor.getMondayTiming());
        timings.put("tuesdayTiming", doctor.getTuesdayTiming());
        timings.put("wednesdayTiming", doctor.getWednesdayTiming());
        timings.put("thursdayTiming", doctor.getThursdayTiming());
        timings.put("fridayTiming", doctor.getFridayTiming());
        timings.put("saturdayTiming", doctor.getSaturdayTiming());
        timings.put("sundayTiming", doctor.getSundayTiming());
        
        return timings;
    }
    
    // Check if doctor is available on a specific day
    public boolean isDoctorAvailableOnDay(Long doctorId, String day) {
        Doctor doctor = getDoctorById(doctorId);
        
        String timing = getTimingForDay(doctor, day);
        return timing != null && !timing.equalsIgnoreCase("closed") && !timing.trim().isEmpty();
    }
    
    private String getTimingForDay(Doctor doctor, String day) {
        switch (day.toLowerCase()) {
            case "monday": return doctor.getMondayTiming();
            case "tuesday": return doctor.getTuesdayTiming();
            case "wednesday": return doctor.getWednesdayTiming();
            case "thursday": return doctor.getThursdayTiming();
            case "friday": return doctor.getFridayTiming();
            case "saturday": return doctor.getSaturdayTiming();
            case "sunday": return doctor.getSundayTiming();
            default: return null;
        }
    }
    
    // Get doctors available on a specific day
    public List<Doctor> getDoctorsAvailableOnDay(String day) {
        List<Doctor> allDoctors = doctorRepository.findAll();
        
        return allDoctors.stream()
            .filter(doctor -> isDoctorAvailableOnDay(doctor.getId(), day))
            .collect(Collectors.toList());
    }
    
 // Get doctors by city
    public List<Doctor> getDoctorsByCity(String city) {
        List<Doctor> allDoctors = doctorRepository.findAll();
        
        return allDoctors.stream()
            .filter(doctor -> doctor.getClinicCity() != null && 
                             doctor.getClinicCity().toLowerCase().contains(city.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    // Get doctors by state
    public List<Doctor> getDoctorsByState(String state) {
        List<Doctor> allDoctors = doctorRepository.findAll();
        
        return allDoctors.stream()
            .filter(doctor -> doctor.getClinicState() != null && 
                             doctor.getClinicState().toLowerCase().contains(state.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    // Get doctors offering online consultation
    public List<Doctor> getDoctorsOfferingOnlineConsultation() {
        List<Doctor> allDoctors = doctorRepository.findAll();
        
        return allDoctors.stream()
            .filter(doctor -> doctor.getOnlineConsultation() != null && doctor.getOnlineConsultation())
            .collect(Collectors.toList());
    }
    
    // Statistics Methods
    public Map<String, Object> getDoctorStatistics() {
        List<Doctor> allDoctors = doctorRepository.findAll();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDoctors", allDoctors.size());
        
        // Count by specialization
        Map<String, Long> specializationCount = allDoctors.stream()
            .filter(doctor -> doctor.getSpecialization() != null)
            .collect(Collectors.groupingBy(Doctor::getSpecialization, Collectors.counting()));
        stats.put("specializationDistribution", specializationCount);
        
        // Count by city
        Map<String, Long> cityCount = allDoctors.stream()
            .filter(doctor -> doctor.getClinicCity() != null)
            .collect(Collectors.groupingBy(Doctor::getClinicCity, Collectors.counting()));
        stats.put("cityDistribution", cityCount);
        
        // Average consultation fee
        OptionalDouble avgFee = allDoctors.stream()
            .filter(doctor -> doctor.getConsultationFee() != null)
            .mapToDouble(Doctor::getConsultationFee)
            .average();
        stats.put("averageConsultationFee", avgFee.isPresent() ? avgFee.getAsDouble() : 0.0);
        
        // Emergency availability count
        long emergencyAvailable = allDoctors.stream()
            .filter(Doctor::isAvailableForEmergency)
            .count();
        stats.put("emergencyAvailableDoctors", emergencyAvailable);
        
        // Online consultation count
        long onlineConsultationAvailable = allDoctors.stream()
            .filter(doctor -> doctor.getOnlineConsultation() != null && doctor.getOnlineConsultation())
            .count();
        stats.put("onlineConsultationAvailable", onlineConsultationAvailable);
        
        return stats;
    }
    
    // Validation Methods
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return true; // Allow empty phone numbers
        }
        
        // Remove all non-digit characters
        String cleanedNumber = phoneNumber.replaceAll("[^0-9]", "");
        
        // Check if length is between 10-15 digits
        return cleanedNumber.length() >= 10 && cleanedNumber.length() <= 15;
    }
    
    public boolean isValidPincode(String pincode) {
        if (pincode == null || pincode.trim().isEmpty()) {
            return true; // Allow empty pincode
        }
        
        // Check if pincode is 5-6 digits
        return pincode.matches("^[0-9]{5,6}$");
    }
    
    public boolean isValidTimingFormat(String timing) {
        if (timing == null || timing.trim().isEmpty() || timing.equalsIgnoreCase("Closed")) {
            return true;
        }
        
        // Check format: HH:MM-HH:MM
        return timing.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]-([01]?[0-9]|2[0-3]):[0-5][0-9]$");
    }
    
    // Professional Information Management
    @Transactional
    public Doctor updateProfessionalInfo(Long doctorId, Map<String, Object> professionalData) {
        Doctor doctor = getDoctorById(doctorId);
        
        if (professionalData.containsKey("expertise")) {
            @SuppressWarnings("unchecked")
            List<String> expertiseList = (List<String>) professionalData.get("expertise");
            if (expertiseList != null) {
                doctor.setExpertise(String.join(",", expertiseList));
            }
        }
        
        if (professionalData.containsKey("services")) {
            @SuppressWarnings("unchecked")
            List<String> servicesList = (List<String>) professionalData.get("services");
            if (servicesList != null) {
                doctor.setServices(String.join(",", servicesList));
            }
        }
        
        if (professionalData.containsKey("experiences")) {
            // Convert to JSON string for storage
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String experiencesJson = mapper.writeValueAsString(professionalData.get("experiences"));
                doctor.setExperiences(experiencesJson);
            } catch (Exception e) {
                System.err.println("Error converting experiences to JSON: " + e.getMessage());
            }
        }
        
        if (professionalData.containsKey("educationDetails")) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String educationJson = mapper.writeValueAsString(professionalData.get("educationDetails"));
                doctor.setEducationDetails(educationJson);
            } catch (Exception e) {
                System.err.println("Error converting education details to JSON: " + e.getMessage());
            }
        }
        
        if (professionalData.containsKey("awards")) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String awardsJson = mapper.writeValueAsString(professionalData.get("awards"));
                doctor.setAwards(awardsJson);
            } catch (Exception e) {
                System.err.println("Error converting awards to JSON: " + e.getMessage());
            }
        }
        
        return doctorRepository.save(doctor);
    }
    
    // Get recommended doctors based on specialization and rating
    public List<Doctor> getRecommendedDoctors(String specialization, int limit) {
        List<Doctor> doctors = doctorRepository.findAll();
        
        return doctors.stream()
            .filter(doctor -> specialization == null || 
                             (doctor.getSpecialization() != null && 
                              doctor.getSpecialization().toLowerCase().contains(specialization.toLowerCase())))
            .sorted((d1, d2) -> {
                // Sort by rating (descending), then by years of experience (descending)
                int ratingCompare = Integer.compare(
                    d2.getAverageRating() != null ? d2.getAverageRating() : 0,
                    d1.getAverageRating() != null ? d1.getAverageRating() : 0
                );
                if (ratingCompare != 0) {
                    return ratingCompare;
                }
                return Integer.compare(
                    d2.getYearsOfExperience() != null ? d2.getYearsOfExperience() : 0,
                    d1.getYearsOfExperience() != null ? d1.getYearsOfExperience() : 0
                );
            })
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    // Account Management
    @Transactional
    public void deactivateDoctor(Long doctorId) {
        Doctor doctor = getDoctorById(doctorId);
        User user = doctor.getUser();
        user.setEnabled(false);
        userRepository.save(user);
    }
    
    @Transactional
    public void reactivateDoctor(Long doctorId) {
        Doctor doctor = getDoctorById(doctorId);
        User user = doctor.getUser();
        user.setEnabled(true);
        userRepository.save(user);
    }
    
    public boolean isDoctorActive(Long doctorId) {
        Doctor doctor = getDoctorById(doctorId);
        return doctor.getUser().isEnabled();
    }
}