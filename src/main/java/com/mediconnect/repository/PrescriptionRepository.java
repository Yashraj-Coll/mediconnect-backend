package com.mediconnect.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.Prescription;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    
    List<Prescription> findByPatientId(Long patientId);
    
    List<Prescription> findByDoctorId(Long doctorId);
    
    List<Prescription> findByAppointmentId(Long appointmentId);
    
    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId " +
           "ORDER BY p.prescriptionDate DESC")
    List<Prescription> findLatestPrescriptionsByPatientId(@Param("patientId") Long patientId);
    
    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId AND " +
           "p.prescriptionDate BETWEEN :startDate AND :endDate")
    List<Prescription> findByPatientIdAndDateRange(
            @Param("patientId") Long patientId, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p FROM Prescription p JOIN p.prescriptionItems pi WHERE " +
           "LOWER(pi.medicationName) LIKE LOWER(CONCAT('%', :medicationName, '%'))")
    List<Prescription> findByMedicationName(@Param("medicationName") String medicationName);
    
    @Query("SELECT p FROM Prescription p WHERE p.validUntil >= :now AND p.isRefillable = true")
    List<Prescription> findActiveRefillablePrescriptions(@Param("now") LocalDateTime now);
    
    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId AND p.isDigitallySigned = true")
    List<Prescription> findSignedPrescriptionsByPatientId(@Param("patientId") Long patientId);
    
    // Added this method to fix the compilation error
    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId AND p.validUntil >= :currentTime")
    List<Prescription> findActiveByPatientId(@Param("patientId") Long patientId, @Param("currentTime") LocalDateTime currentTime);
}