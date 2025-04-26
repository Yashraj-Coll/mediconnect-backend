package com.mediconnect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.TreatmentRecommendation;

@Repository
public interface TreatmentRecommendationRepository extends JpaRepository<TreatmentRecommendation, Long> {
    
    Optional<TreatmentRecommendation> findByMedicalRecordId(Long medicalRecordId);
    
}