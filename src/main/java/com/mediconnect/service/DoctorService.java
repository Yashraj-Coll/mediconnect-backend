package com.mediconnect.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private DoctorLanguageRepository doctorLanguageRepository;
    
    /**
     * Get all doctors
     */
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }
    
    /**
     * Get doctor by id
     */
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
    }
    
    /**
     * Get doctor by user id
     */
    public Optional<Doctor> getDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId);
    }
    
    /**
     * Get doctors by specialization
     */
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }
    
    /**
     * Get doctors available for emergency
     */
    public List<Doctor> getAvailableDoctorsForEmergency() {
        return doctorRepository.findAvailableForEmergency();
    }
    
    /**
     * Search doctors by keyword
     */
    public List<Doctor> searchDoctors(String keyword) {
        return doctorRepository.searchDoctors(keyword);
    }
    
    /**
     * Get doctors by max consultation fee
     */
    public List<Doctor> getDoctorsByMaxConsultationFee(Double maxFee) {
        return doctorRepository.findByConsultationFeeLessThanEqual(maxFee);
    }
    
    /**
     * Get top rated doctors
     */
    public List<Doctor> getTopRatedDoctors() {
        return doctorRepository.findTopRatedDoctors();
    }
    
    /**
     * Get languages for a doctor as DTOs
     */
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
    
    /**
     * Get languages as string list for simplified access
     */
    public List<String> getDoctorLanguagesAsStringList(Long doctorId) {
        List<DoctorLanguage> languages = doctorLanguageRepository.findByDoctorId(doctorId);
        return languages.stream()
                .map(DoctorLanguage::getLanguage)
                .collect(Collectors.toList());
    }
    
    /**
     * Create a new doctor
     */
    @Transactional
    public Doctor createDoctor(DoctorDTO doctorDTO) {
        // Get or create the user
        User user = userRepository.findById(doctorDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + doctorDTO.getUserId()));
        
        // Check if user already has a doctor profile
        if (doctorRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("User already has a doctor profile");
        }
        
        // Add DOCTOR role to user if not already present
        Role doctorRole = roleRepository.findByName(ERole.ROLE_DOCTOR)
                .orElseThrow(() -> new RuntimeException("Error: Doctor Role not found"));
        
        if (!user.getRoles().contains(doctorRole)) {
            user.getRoles().add(doctorRole);
            userRepository.save(user);
        }
        
        // Create new doctor profile
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
        
        Doctor savedDoctor = doctorRepository.save(doctor);
        
        // Save doctor languages if provided
        if (doctorDTO.getLanguages() != null && !doctorDTO.getLanguages().isEmpty()) {
            for (String language : doctorDTO.getLanguages()) {
                DoctorLanguage doctorLanguage = new DoctorLanguage();
                doctorLanguage.setDoctor(savedDoctor);
                doctorLanguage.setLanguage(language);
                doctorLanguageRepository.save(doctorLanguage);
            }
        } else {
            // Add default languages if none provided
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
    
    /**
     * Update an existing doctor
     */
    @Transactional
    public Doctor updateDoctor(Long id, DoctorDTO doctorDTO) {
        Doctor doctor = getDoctorById(id);
        
        // Update doctor fields
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
        
        doctor.setAvailableForEmergency(doctorDTO.isAvailableForEmergency());
        
        Doctor updatedDoctor = doctorRepository.save(doctor);
        
        // Update doctor languages if provided
        if (doctorDTO.getLanguages() != null && !doctorDTO.getLanguages().isEmpty()) {
            // Delete existing languages
            doctorLanguageRepository.deleteByDoctorId(id);
            
            // Add new languages
            for (String language : doctorDTO.getLanguages()) {
                DoctorLanguage doctorLanguage = new DoctorLanguage();
                doctorLanguage.setDoctor(updatedDoctor);
                doctorLanguage.setLanguage(language);
                doctorLanguageRepository.save(doctorLanguage);
            }
        }
        
        return updatedDoctor;
    }
    
    /**
     * Delete a doctor
     */
    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorById(id);
        
        // Delete doctor languages first
        doctorLanguageRepository.deleteByDoctorId(id);
        
        // Delete doctor
        doctorRepository.delete(doctor);
    }
    
    /**
     * Add a language to a doctor
     */
    @Transactional
    public DoctorLanguageDTO addDoctorLanguage(Long doctorId, String language) {
        Doctor doctor = getDoctorById(doctorId);
        
        // Check if language already exists for doctor
        List<DoctorLanguage> existingLanguages = doctorLanguageRepository.findByDoctorId(doctorId);
        for (DoctorLanguage existingLang : existingLanguages) {
            if (existingLang.getLanguage().equalsIgnoreCase(language)) {
                // Language already exists, return as DTO
                DoctorLanguageDTO dto = new DoctorLanguageDTO();
                dto.setId(existingLang.getId());
                dto.setDoctorId(doctorId);
                dto.setLanguage(existingLang.getLanguage());
                return dto;
            }
        }
        
        // Create new language for doctor
        DoctorLanguage doctorLanguage = new DoctorLanguage();
        doctorLanguage.setDoctor(doctor);
        doctorLanguage.setLanguage(language);
        DoctorLanguage savedLanguage = doctorLanguageRepository.save(doctorLanguage);
        
        // Map to DTO and return
        DoctorLanguageDTO dto = new DoctorLanguageDTO();
        dto.setId(savedLanguage.getId());
        dto.setDoctorId(doctorId);
        dto.setLanguage(savedLanguage.getLanguage());
        return dto;
    }
    
    /**
     * Remove a language from a doctor
     */
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
}