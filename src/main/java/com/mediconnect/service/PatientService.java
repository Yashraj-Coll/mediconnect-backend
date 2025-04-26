package com.mediconnect.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediconnect.dto.PatientDTO;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.model.ERole;
import com.mediconnect.model.Patient;
import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.repository.RoleRepository;
import com.mediconnect.repository.UserRepository;

@Service
public class PatientService {
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
    
    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }
    
    public Optional<Patient> getPatientByUserId(Long userId) {
        return patientRepository.findByUserId(userId);
    }
    
    public List<Patient> searchPatients(String keyword) {
        return patientRepository.searchPatients(keyword);
    }
    
    public List<Patient> getPatientsByMedicalCondition(String condition) {
        return patientRepository.findByMedicalCondition(condition);
    }
    
    public List<Patient> getPatientsByDoctor(Long doctorId) {
        return patientRepository.findPatientsByDoctor(doctorId);
    }
    
    public List<Patient> getPatientsByInsuranceProvider(String provider) {
        return patientRepository.findByInsuranceProvider(provider);
    }
    
    @Transactional
    public Patient createPatient(PatientDTO patientDTO) {
        // Get or create the user
        User user = userRepository.findById(patientDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + patientDTO.getUserId()));
        
        // Check if user already has a patient profile
        if (patientRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("User already has a patient profile");
        }
        
        // Add PATIENT role to user if not already present
        Role patientRole = roleRepository.findByName(ERole.ROLE_PATIENT)
                .orElseThrow(() -> new RuntimeException("Error: Patient Role not found"));
        
        if (!user.getRoles().contains(patientRole)) {
            user.getRoles().add(patientRole);
            userRepository.save(user);
        }
        
        // Create new patient profile
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setDateOfBirth(patientDTO.getDateOfBirth());
        patient.setGender(patientDTO.getGender());
        patient.setBloodGroup(patientDTO.getBloodGroup());
        patient.setAllergies(patientDTO.getAllergies());
        patient.setChronicDiseases(patientDTO.getChronicDiseases());
        patient.setEmergencyContactName(patientDTO.getEmergencyContactName());
        patient.setEmergencyContactNumber(patientDTO.getEmergencyContactNumber());
        patient.setEmergencyContactRelation(patientDTO.getEmergencyContactRelation());
        patient.setInsuranceProvider(patientDTO.getInsuranceProvider());
        patient.setInsurancePolicyNumber(patientDTO.getInsurancePolicyNumber());
        patient.setHeight(patientDTO.getHeight());
        patient.setWeight(patientDTO.getWeight());
        
        return patientRepository.save(patient);
    }
    
    @Transactional
    public Patient updatePatient(Long id, PatientDTO patientDTO) {
        Patient patient = getPatientById(id);
        
        // Update patient fields
        if (patientDTO.getDateOfBirth() != null) {
            patient.setDateOfBirth(patientDTO.getDateOfBirth());
        }
        
        if (patientDTO.getGender() != null) {
            patient.setGender(patientDTO.getGender());
        }
        
        if (patientDTO.getBloodGroup() != null) {
            patient.setBloodGroup(patientDTO.getBloodGroup());
        }
        
        if (patientDTO.getAllergies() != null) {
            patient.setAllergies(patientDTO.getAllergies());
        }
        
        if (patientDTO.getChronicDiseases() != null) {
            patient.setChronicDiseases(patientDTO.getChronicDiseases());
        }
        
        if (patientDTO.getEmergencyContactName() != null) {
            patient.setEmergencyContactName(patientDTO.getEmergencyContactName());
        }
        
        if (patientDTO.getEmergencyContactNumber() != null) {
            patient.setEmergencyContactNumber(patientDTO.getEmergencyContactNumber());
        }
        
        if (patientDTO.getEmergencyContactRelation() != null) {
            patient.setEmergencyContactRelation(patientDTO.getEmergencyContactRelation());
        }
        
        if (patientDTO.getInsuranceProvider() != null) {
            patient.setInsuranceProvider(patientDTO.getInsuranceProvider());
        }
        
        if (patientDTO.getInsurancePolicyNumber() != null) {
            patient.setInsurancePolicyNumber(patientDTO.getInsurancePolicyNumber());
        }
        
        if (patientDTO.getHeight() != null) {
            patient.setHeight(patientDTO.getHeight());
        }
        
        if (patientDTO.getWeight() != null) {
            patient.setWeight(patientDTO.getWeight());
        }
        
        return patientRepository.save(patient);
    }
    
    public void deletePatient(Long id) {
        Patient patient = getPatientById(id);
        patientRepository.delete(patient);
    }
}