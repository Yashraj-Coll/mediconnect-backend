package com.mediconnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.ImageAnalysisResult;
import com.mediconnect.model.ImageAnalysisResult.AnalysisStatus;

@Repository
public interface ImageAnalysisResultRepository extends JpaRepository<ImageAnalysisResult, Long> {
    
    List<ImageAnalysisResult> findByMedicalRecordId(Long medicalRecordId);
    
    @Query("SELECT r FROM ImageAnalysisResult r WHERE r.medicalRecord.id = :medicalRecordId " +
           "AND r.status IN ('PENDING', 'PROCESSING') " +
           "ORDER BY r.submittedAt DESC")
    Optional<ImageAnalysisResult> findPendingByMedicalRecordId(@Param("medicalRecordId") Long medicalRecordId);
    
    @Query("SELECT r FROM ImageAnalysisResult r WHERE r.medicalRecord.patient.id = :patientId " +
           "ORDER BY r.submittedAt DESC")
    List<ImageAnalysisResult> findByPatientId(@Param("patientId") Long patientId);
    
    // Added missing method
    List<ImageAnalysisResult> findByMedicalRecordIdAndStatus(Long medicalRecordId, AnalysisStatus status);
}