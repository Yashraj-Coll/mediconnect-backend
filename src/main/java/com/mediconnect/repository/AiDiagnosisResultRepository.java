package com.mediconnect.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.AiDiagnosisResult;
import com.mediconnect.model.AiDiagnosisResult.AiDiagnosisStatus;

@Repository
public interface AiDiagnosisResultRepository extends JpaRepository<AiDiagnosisResult, Long> {
    
    Optional<AiDiagnosisResult> findByMedicalRecordId(Long medicalRecordId);
    
    List<AiDiagnosisResult> findByStatus(AiDiagnosisStatus status);
    
    @Query("SELECT a FROM AiDiagnosisResult a WHERE a.analyzedAt BETWEEN :startDate AND :endDate")
    List<AiDiagnosisResult> findByAnalyzedDateRange(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM AiDiagnosisResult a WHERE a.reviewedByDoctor = :reviewed")
    List<AiDiagnosisResult> findByReviewStatus(@Param("reviewed") Boolean reviewed);
    
    @Query("SELECT a FROM AiDiagnosisResult a WHERE " +
           "a.medicalRecord.patient.id = :patientId")
    List<AiDiagnosisResult> findByPatientId(@Param("patientId") Long patientId);
    
    @Query("SELECT a FROM AiDiagnosisResult a WHERE " +
           "a.medicalRecord.doctor.id = :doctorId")
    List<AiDiagnosisResult> findByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT a FROM AiDiagnosisResult a JOIN a.predictions p WHERE " +
           "LOWER(p.conditionName) LIKE LOWER(CONCAT('%', :condition, '%'))")
    List<AiDiagnosisResult> findByPredictedCondition(@Param("condition") String condition);
    
    @Query("SELECT a FROM AiDiagnosisResult a WHERE a.confidenceScore >= :minScore")
    List<AiDiagnosisResult> findByMinimumConfidence(@Param("minScore") Double minScore);
    
    @Query("SELECT a FROM AiDiagnosisResult a WHERE a.modelVersion = :version")
    List<AiDiagnosisResult> findByModelVersion(@Param("version") String version);
}