package com.mediconnect.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mediconnect.model.DiagnosisPrediction;
import com.mediconnect.repository.DiagnosisPredictionRepository;

@Service
public class DiagnosisPredictionService {
    
    @Autowired
    private DiagnosisPredictionRepository predictionRepository;
    
    /**
     * Find all predictions
     */
    public List<DiagnosisPrediction> findAll() {
        return predictionRepository.findAll();
    }
    
    /**
     * Find prediction by ID
     */
    public DiagnosisPrediction findById(Long id) {
        return predictionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prediction not found with id: " + id));
    }
    
    /**
     * Find predictions by AI diagnosis result ID
     */
    public List<DiagnosisPrediction> findByAiDiagnosisResultId(Long aiDiagnosisResultId) {
        return predictionRepository.findByAiDiagnosisResultId(aiDiagnosisResultId);
    }
    
    /**
     * Find predictions by condition name
     */
    public List<DiagnosisPrediction> findByConditionName(String condition) {
        return predictionRepository.findByConditionName(condition);
    }
    
    /**
     * Find predictions with minimum probability
     */
    public List<DiagnosisPrediction> findByMinimumProbability(Double minProbability) {
        return predictionRepository.findByMinimumProbability(minProbability);
    }
    
    /**
     * Find predictions with minimum severity level
     */
    public List<DiagnosisPrediction> findByMinimumSeverity(Integer minSeverity) {
        return predictionRepository.findByMinimumSeverity(minSeverity);
    }
    
    /**
     * Find predictions by patient ID
     */
    public List<DiagnosisPrediction> findByPatientId(Long patientId) {
        return predictionRepository.findByPatientId(patientId);
    }
    
    /**
     * Find top predictions ordered by probability
     */
    public List<DiagnosisPrediction> findTopPredictions() {
        return predictionRepository.findTopPredictions();
    }
}