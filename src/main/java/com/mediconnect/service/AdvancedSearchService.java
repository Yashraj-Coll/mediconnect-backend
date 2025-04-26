package com.mediconnect.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.mediconnect.dto.AppointmentSearchDTO;
import com.mediconnect.dto.DoctorSearchDTO;
import com.mediconnect.dto.PatientSearchDTO;
import com.mediconnect.model.Appointment;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.Patient;
import com.mediconnect.repository.AppointmentRepository;
import com.mediconnect.repository.DoctorRepository;
import com.mediconnect.repository.PatientRepository;

@Service
public class AdvancedSearchService {

    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    /**
     * Advanced patient search
     */
    public Page<Patient> searchPatients(PatientSearchDTO searchDTO) {
        // Create pageable with sorting
        Sort sort = Sort.by(
                Direction.fromString(searchDTO.getSortDirection()), 
                searchDTO.getSortBy());
        
        Pageable pageable = PageRequest.of(
                searchDTO.getPage(), 
                searchDTO.getSize(), 
                sort);
        
        // Call repository with all search criteria
        return patientRepository.advancedSearch(
                searchDTO.getName(),
                searchDTO.getEmail(),
                searchDTO.getGender(),
                searchDTO.getBloodGroup(),
                searchDTO.getCondition(),
                searchDTO.getInsuranceProvider(),
                searchDTO.getMinAge(),
                searchDTO.getMaxAge(),
                searchDTO.getMinWeight(),
                searchDTO.getMaxWeight(),
                searchDTO.getMinHeight(),
                searchDTO.getMaxHeight(),
                pageable);
    }
    
    /**
     * Advanced doctor search
     */
    public Page<Doctor> searchDoctors(DoctorSearchDTO searchDTO) {
        // Create pageable with sorting
        Sort sort = Sort.by(
                Direction.fromString(searchDTO.getSortDirection()), 
                searchDTO.getSortBy());
        
        Pageable pageable = PageRequest.of(
                searchDTO.getPage(), 
                searchDTO.getSize(), 
                sort);
        
        // Call repository with all search criteria
        Page<Doctor> results = doctorRepository.advancedSearch(
                searchDTO.getName(),
                searchDTO.getSpecialization(),
                searchDTO.getHospitalAffiliation(),
                searchDTO.getMinYearsExperience(),
                searchDTO.getMaxFee(),
                searchDTO.getMinRating(),
                searchDTO.getIsEmergencyAvailable(),
                pageable);
        
        // Apply additional filters if needed (these can't be easily done in a single query)
        if (searchDTO.getLanguage() != null && !searchDTO.getLanguage().isEmpty()) {
            List<Doctor> languageDoctors = doctorRepository.findByLanguage(searchDTO.getLanguage());
            // Filter the results (this is simplified, in a real app you'd need to handle pagination properly)
            results = filterPageResults(results, languageDoctors);
        }
        
        if (searchDTO.getInsuranceProvider() != null && !searchDTO.getInsuranceProvider().isEmpty()) {
            List<Doctor> insuranceDoctors = doctorRepository.findByInsuranceAccepted(searchDTO.getInsuranceProvider());
            results = filterPageResults(results, insuranceDoctors);
        }
        
        if (searchDTO.getDayOfWeek() != null && !searchDTO.getDayOfWeek().isEmpty() &&
            searchDTO.getTime() != null && !searchDTO.getTime().isEmpty()) {
            List<Doctor> availableDoctors = doctorRepository.findByAvailability(searchDTO.getDayOfWeek(), searchDTO.getTime());
            results = filterPageResults(results, availableDoctors);
        }
        
        return results;
    }
    
    /**
     * Advanced appointment search
     */
    public Page<Appointment> searchAppointments(AppointmentSearchDTO searchDTO) {
        // Create pageable with sorting
        Sort sort = Sort.by(
                Direction.fromString(searchDTO.getSortDirection()), 
                searchDTO.getSortBy());
        
        Pageable pageable = PageRequest.of(
                searchDTO.getPage(), 
                searchDTO.getSize(), 
                sort);
        
        // Call repository with all search criteria
        Page<Appointment> results = appointmentRepository.advancedSearch(
                searchDTO.getDoctorId(),
                searchDTO.getPatientId(),
                searchDTO.getStatus(),
                searchDTO.getType(),
                searchDTO.getIsPaid(),
                searchDTO.getStartDate(),
                searchDTO.getEndDate(),
                searchDTO.getDoctorName(),
                searchDTO.getPatientName(),
                searchDTO.getSpecialization(),
                pageable);
        
        // Apply additional filters if needed
        if (searchDTO.getMinDuration() != null && searchDTO.getMaxDuration() != null) {
            List<Appointment> durationAppointments = appointmentRepository.findByDurationRange(
                    searchDTO.getMinDuration(), searchDTO.getMaxDuration());
            results = filterPageResults(results, durationAppointments);
        }
        
        return results;
    }
    
    /**
     * Helper method to filter page results by a list of entities
     * This is a simplified approach - in a production app you might want to improve the efficiency
     */
    private <T> Page<T> filterPageResults(Page<T> page, List<T> filterList) {
        if (filterList == null || filterList.isEmpty()) {
            return page;
        }
        
        // Create a new page with only the items that are in the filter list
        List<T> filteredContent = page.getContent().stream()
                .filter(filterList::contains)
                .toList();
        
        // Create a new Page implementation (simplified)
        return new org.springframework.data.domain.PageImpl<>(
                filteredContent, 
                page.getPageable(), 
                filteredContent.size());
    }
    
    /**
     * Find patients with specific vital signs
     */
    public List<Patient> findPatientsByVitalSigns(
            Integer minHeartRate, Integer maxHeartRate,
            Integer minSystolicBP, Integer maxSystolicBP,
            Double minTemperature, Double maxTemperature,
            Double minOxygenSaturation, Double maxOxygenSaturation) {
        
        return patientRepository.findPatientsByVitalSigns(
                minHeartRate, maxHeartRate,
                minSystolicBP, maxSystolicBP,
                minTemperature, maxTemperature,
                minOxygenSaturation, maxOxygenSaturation);
    }
    
    /**
     * Find patients by BMI range
     */
    public List<Patient> findPatientsByBMIRange(Double minBMI, Double maxBMI) {
        return patientRepository.findByBMIRange(minBMI, maxBMI);
    }
    
    /**
     * Find overlapping appointments for a doctor
     */
    public List<Appointment> findOverlappingAppointments(
            Long doctorId, LocalDateTime startTime, LocalDateTime endTime) {
        return appointmentRepository.findOverlappingAppointments(doctorId, startTime, endTime);
    }
}