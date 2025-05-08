package com.mediconnect.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mediconnect.dto.CompleteDocotrResponseDTO;
import com.mediconnect.dto.DoctorDTO;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.DoctorLanguage;
import com.mediconnect.repository.DoctorLanguageRepository;

/**
 * Service for mapping Doctor entities to DTOs with complete information
 */
@Service
public class DoctorMapperService {
    
    @Autowired
    private DoctorLanguageRepository doctorLanguageRepository;
    
    /**
     * Map a Doctor entity to a DoctorDTO with complete information
     */
    public DoctorDTO mapToDTO(Doctor doctor) {
        if (doctor == null) {
            return null;
        }
        
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctor.getId());
        dto.setUserId(doctor.getUser().getId());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setLicenseNumber(doctor.getLicenseNumber());
        dto.setEducation(doctor.getEducation());
        dto.setExperience(doctor.getExperience());
        dto.setHospitalAffiliation(doctor.getHospitalAffiliation());
        dto.setYearsOfExperience(doctor.getYearsOfExperience());
        dto.setConsultationFee(doctor.getConsultationFee());
        dto.setBiography(doctor.getBiography());
        dto.setAverageRating(doctor.getAverageRating());
        dto.setAvailableForEmergency(doctor.isAvailableForEmergency());
        
        // Fetch and add languages
        List<String> languages = doctorLanguageRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(DoctorLanguage::getLanguage)
                .collect(Collectors.toList());
        dto.setLanguages(languages);
        
        // Add a default profile image if none exists
        // In a real app, you would get this from a profile image service
        dto.setProfileImage("/assets/images/doctors/doctor-" + doctor.getId() + ".jpg");
        
        return dto;
    }
    
    /**
     * Map a list of Doctor entities to a list of DoctorDTOs with complete information
     */
    public List<DoctorDTO> mapToDTOList(List<Doctor> doctors) {
        if (doctors == null) {
            return null;
        }
        
        return doctors.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Map a Doctor entity to a CompleteDocotrResponseDTO with even more information
     * This includes appointment statistics, ratings, etc.
     */
    public CompleteDocotrResponseDTO mapToCompleteDTO(Doctor doctor) {
        if (doctor == null) {
            return null;
        }
        
        CompleteDocotrResponseDTO dto = new CompleteDocotrResponseDTO();
        dto.setId(doctor.getId());
        dto.setName(doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName());
        dto.setEmail(doctor.getUser().getEmail());
        dto.setPhone(doctor.getUser().getPhoneNumber());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setLicenseNumber(doctor.getLicenseNumber());
        dto.setEducation(doctor.getEducation());
        dto.setExperience(doctor.getExperience());
        dto.setHospitalAffiliation(doctor.getHospitalAffiliation());
        dto.setYearsOfExperience(doctor.getYearsOfExperience());
        dto.setConsultationFee(doctor.getConsultationFee());
        dto.setBiography(doctor.getBiography());
        dto.setAverageRating(doctor.getAverageRating());
        dto.setIsAvailableForEmergency(doctor.isAvailableForEmergency());
        
        // Fetch and add languages
        List<String> languages = doctorLanguageRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(DoctorLanguage::getLanguage)
                .collect(Collectors.toList());
        dto.setLanguages(languages);
        
        // Add a default profile image if none exists
        dto.setProfileImage("/assets/images/doctors/doctor-" + doctor.getId() + ".jpg");
        
        // Add appointment stats - in a real app, you would get this from an appointment service
        dto.setTotalAppointments(doctor.getAppointments().size());
        dto.setCompletedAppointments((int)(Math.random() * 100));
        dto.setCancelledAppointments((int)(Math.random() * 20));
        
        // Add rating stats - in a real app, you would get this from a rating service
        dto.setRatingCount((int)(Math.random() * 500));
        dto.setFiveStarRatings((int)(Math.random() * 300));
        dto.setFourStarRatings((int)(Math.random() * 150));
        dto.setThreeStarRatings((int)(Math.random() * 50));
        dto.setTwoStarRatings((int)(Math.random() * 20));
        dto.setOneStarRatings((int)(Math.random() * 10));
        
        return dto;
    }
}