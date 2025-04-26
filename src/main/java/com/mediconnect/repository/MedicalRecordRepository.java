package com.mediconnect.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.MedicalRecord;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    
    List<MedicalRecord> findByPatientId(Long patientId);
    
    List<MedicalRecord> findByDoctorId(Long doctorId);
    
    List<MedicalRecord> findByAppointmentId(Long appointmentId);
    
    @Query("SELECT m FROM MedicalRecord m WHERE m.patient.id = :patientId " +
           "ORDER BY m.recordDate DESC")
    List<MedicalRecord> findLatestRecordsByPatientId(@Param("patientId") Long patientId);
    
    @Query("SELECT m FROM MedicalRecord m WHERE m.patient.id = :patientId AND " +
           "m.recordDate BETWEEN :startDate AND :endDate")
    List<MedicalRecord> findByPatientIdAndDateRange(
            @Param("patientId") Long patientId, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT m FROM MedicalRecord m WHERE " +
           "LOWER(m.diagnosis) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.symptoms) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.treatment) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<MedicalRecord> searchMedicalRecords(@Param("keyword") String keyword);
    
    @Query("SELECT m FROM MedicalRecord m WHERE m.patient.id = :patientId AND " +
           "LOWER(m.diagnosis) LIKE LOWER(CONCAT('%', :condition, '%'))")
    List<MedicalRecord> findByPatientIdAndDiagnosis(
            @Param("patientId") Long patientId, 
            @Param("condition") String condition);
    
    @Query("SELECT m FROM MedicalRecord m WHERE m.followUpDate <= :date AND m.followUpDate >= :now")
    List<MedicalRecord> findUpcomingFollowUps(
            @Param("date") LocalDateTime date, 
            @Param("now") LocalDateTime now);
}