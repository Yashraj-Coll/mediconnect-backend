package com.mediconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.HealthRiskAssessment;

@Repository
public interface HealthRiskAssessmentRepository extends JpaRepository<HealthRiskAssessment, Long> {
    
    List<HealthRiskAssessment> findByPatientIdOrderByAssessedAtDesc(Long patientId);
    
}