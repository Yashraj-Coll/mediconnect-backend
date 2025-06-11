package com.mediconnect.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.Prescription;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    /**
     * Find prescriptions by patient ID ordered by date (newest first)
     */
    List<Prescription> findByPatientIdOrderByPrescriptionDateDesc(Long patientId);
    
    /**
     * Find prescriptions by doctor ID ordered by date (newest first)
     */
    List<Prescription> findByDoctorIdOrderByPrescriptionDateDesc(Long doctorId);
    
    /**
     * Find prescription by appointment ID
     */
    Optional<Prescription> findByAppointmentId(Long appointmentId);
    
    /**
     * Find valid prescriptions for a patient (not expired)
     */
    List<Prescription> findByPatientIdAndValidUntilAfter(Long patientId, LocalDateTime now);
    
    /**
     * Find active prescriptions for a patient (prescriptions that are still valid)
     * This is the missing method referenced in ChatbotService
     */
    default List<Prescription> findActiveByPatientId(Long patientId, LocalDateTime currentDate) {
        return findByPatientIdAndValidUntilAfter(patientId, currentDate);
    }
}