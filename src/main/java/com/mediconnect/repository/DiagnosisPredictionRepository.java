package com.mediconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.DiagnosisPrediction;

@Repository
public interface DiagnosisPredictionRepository extends JpaRepository<DiagnosisPrediction, Long> {
    
    List<DiagnosisPrediction> findByAiDiagnosisResultId(Long aiDiagnosisResultId);
    
    @Query("SELECT dp FROM DiagnosisPrediction dp WHERE " +
           "LOWER(dp.conditionName) LIKE LOWER(CONCAT('%', :condition, '%'))")
    List<DiagnosisPrediction> findByConditionName(@Param("condition") String condition);
    
    @Query("SELECT dp FROM DiagnosisPrediction dp WHERE dp.probability >= :minProbability")
    List<DiagnosisPrediction> findByMinimumProbability(@Param("minProbability") Double minProbability);
    
    @Query("SELECT dp FROM DiagnosisPrediction dp WHERE dp.severityLevel >= :minSeverity")
    List<DiagnosisPrediction> findByMinimumSeverity(@Param("minSeverity") Integer minSeverity);
    
    @Query("SELECT dp FROM DiagnosisPrediction dp JOIN dp.aiDiagnosisResult a " +
           "WHERE a.medicalRecord.patient.id = :patientId")
    List<DiagnosisPrediction> findByPatientId(@Param("patientId") Long patientId);
    
    @Query("SELECT dp FROM DiagnosisPrediction dp " +
           "ORDER BY dp.probability DESC")
    List<DiagnosisPrediction> findTopPredictions();
}