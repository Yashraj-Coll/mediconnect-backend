package com.mediconnect.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediconnect.dto.DoctorDTO;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.ERole;
import com.mediconnect.model.Role;
import com.mediconnect.model.User;
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
        
        return doctorRepository.save(doctor);
    }
    
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
        
        return doctorRepository.save(doctor);
    }
    
    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorById(id);
        doctorRepository.delete(doctor);
    }
}