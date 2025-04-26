package com.mediconnect.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediconnect.dto.AiDiagnosisRequestDTO;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.model.AiDiagnosisResult;
import com.mediconnect.model.AiDiagnosisResult.AiDiagnosisStatus;
import com.mediconnect.model.DiagnosisPrediction;
import com.mediconnect.model.MedicalRecord;
import com.mediconnect.repository.AiDiagnosisResultRepository;
import com.mediconnect.repository.DiagnosisPredictionRepository;
import com.mediconnect.repository.MedicalRecordRepository;
import com.mediconnect.util.AiModelUtil;

@Service
public class AiDiagnosticService {
    
    @Autowired
    private AiDiagnosisResultRepository aiDiagnosisResultRepository;
    
    @Autowired
    private DiagnosisPredictionRepository diagnosisPredictionRepository;
    
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    
    @Autowired
    private AiModelUtil aiModelUtil;
    
    @Value("${mediconnect.ai.model.version}")
    private String aiModelVersion;
    
    /**
     * Get all AI diagnosis results
     */
    public List<AiDiagnosisResult> getAllDiagnosisResults() {
        return aiDiagnosisResultRepository.findAll();
    }
    
    /**
     * Get AI diagnosis result by ID
     */
    public AiDiagnosisResult getDiagnosisResultById(Long id) {
        return aiDiagnosisResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI diagnosis result not found with id: " + id));
    }
    
    /**
     * Get AI diagnosis result by medical record ID
     */
    public Optional<AiDiagnosisResult> getDiagnosisResultByMedicalRecordId(Long medicalRecordId) {
        return aiDiagnosisResultRepository.findByMedicalRecordId(medicalRecordId);
    }
    
    /**
     * Get AI diagnosis results by patient ID
     */
    public List<AiDiagnosisResult> getDiagnosisResultsByPatientId(Long patientId) {
        return aiDiagnosisResultRepository.findByPatientId(patientId);
    }
    
    /**
     * Get AI diagnosis results by doctor ID
     */
    public List<AiDiagnosisResult> getDiagnosisResultsByDoctorId(Long doctorId) {
        return aiDiagnosisResultRepository.findByDoctorId(doctorId);
    }
    
    /**
     * Get AI diagnosis results by predicted condition
     */
    public List<AiDiagnosisResult> getDiagnosisResultsByCondition(String condition) {
        return aiDiagnosisResultRepository.findByPredictedCondition(condition);
    }
    
    /**
     * Get AI diagnosis results by analyzed date range
     */
    public List<AiDiagnosisResult> getDiagnosisResultsByAnalyzedDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return aiDiagnosisResultRepository.findByAnalyzedDateRange(startDate, endDate);
    }
    
    /**
     * Get AI diagnosis results by review status
     */
    public List<AiDiagnosisResult> getDiagnosisResultsByReviewStatus(Boolean reviewed) {
        return aiDiagnosisResultRepository.findByReviewStatus(reviewed);
    }
    
    /**
     * Get AI diagnosis results by status
     */
    public List<AiDiagnosisResult> getDiagnosisResultsByStatus(AiDiagnosisStatus status) {
        return aiDiagnosisResultRepository.findByStatus(status);
    }
    
    /**
     * Get AI diagnosis results by minimum confidence score
     */
    public List<AiDiagnosisResult> getDiagnosisResultsByMinimumConfidence(Double minScore) {
        return aiDiagnosisResultRepository.findByMinimumConfidence(minScore);
    }
    
    /**
     * Get AI diagnosis results by model version
     */
    public List<AiDiagnosisResult> getDiagnosisResultsByModelVersion(String version) {
        return aiDiagnosisResultRepository.findByModelVersion(version);
    }
    
    /**
     * Generate AI diagnosis for a medical record
     */
    @Transactional
    public AiDiagnosisResult generateDiagnosis(Long medicalRecordId, AiDiagnosisRequestDTO requestDTO) {
        // Get the medical record
        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + medicalRecordId));
        
        // Check if diagnosis already exists for this medical record
        Optional<AiDiagnosisResult> existingDiagnosis = aiDiagnosisResultRepository.findByMedicalRecordId(medicalRecordId);
        if (existingDiagnosis.isPresent()) {
            throw new BadRequestException("AI diagnosis already exists for this medical record");
        }
        
        // Create new AI diagnosis result
        AiDiagnosisResult aiDiagnosisResult = new AiDiagnosisResult();
        aiDiagnosisResult.setMedicalRecord(medicalRecord);
        aiDiagnosisResult.setModelVersion(aiModelVersion);
        aiDiagnosisResult.setInputParameters(requestDTO.getInputParameters());
        aiDiagnosisResult.setSymptoms(medicalRecord.getSymptoms());
        aiDiagnosisResult.setStatus(AiDiagnosisStatus.PROCESSING);
        aiDiagnosisResult.setReviewedByDoctor(false);
        aiDiagnosisResult.setAnalyzedAt(LocalDateTime.now());
        
        // Save initial state
        AiDiagnosisResult savedResult = aiDiagnosisResultRepository.save(aiDiagnosisResult);
        
        try {
            // Call AI model for analysis (non-blocking)
            processAiDiagnosisAsync(savedResult, requestDTO);
            
            return savedResult;
        } catch (Exception e) {
            // Mark as failed if there's an error
            savedResult.setStatus(AiDiagnosisStatus.FAILED);
            aiDiagnosisResultRepository.save(savedResult);
            
            throw new RuntimeException("Error processing AI diagnosis: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process AI diagnosis asynchronously
     */
    private void processAiDiagnosisAsync(AiDiagnosisResult aiDiagnosisResult, AiDiagnosisRequestDTO requestDTO) {
        // In a real application, this would be an async process using Spring's @Async or a message queue
        new Thread(() -> {
            try {
                // Call AI model
                Map<String, Object> aiResponse = aiModelUtil.analyzeSymptoms(
                        aiDiagnosisResult.getSymptoms(),
                        requestDTO.getAdditionalInfo());
                
                // Update AI diagnosis result with response
                updateDiagnosisWithAiResponse(aiDiagnosisResult.getId(), aiResponse);
            } catch (Exception e) {
                // Mark as failed if there's an error
                AiDiagnosisResult result = aiDiagnosisResultRepository.findById(aiDiagnosisResult.getId()).orElse(null);
                if (result != null) {
                    result.setStatus(AiDiagnosisStatus.FAILED);
                    aiDiagnosisResultRepository.save(result);
                }
            }
        }).start();
    }
    
    /**
     * Update diagnosis with AI model response
     */
    @Transactional
    public AiDiagnosisResult updateDiagnosisWithAiResponse(Long diagnosisId, Map<String, Object> aiResponse) {
        AiDiagnosisResult aiDiagnosisResult = getDiagnosisResultById(diagnosisId);
        
        // Update fields from AI response
        aiDiagnosisResult.setAnalysisReport((String) aiResponse.get("analysisReport"));
        aiDiagnosisResult.setRecommendedTests((String) aiResponse.get("recommendedTests"));
        aiDiagnosisResult.setTreatmentSuggestions((String) aiResponse.get("treatmentSuggestions"));
        aiDiagnosisResult.setSpecialNotes((String) aiResponse.get("specialNotes"));
        aiDiagnosisResult.setConfidenceScore((Double) aiResponse.get("confidenceScore"));
        aiDiagnosisResult.setStatus(AiDiagnosisStatus.COMPLETED);
        
        // Save updated result
        AiDiagnosisResult savedResult = aiDiagnosisResultRepository.save(aiDiagnosisResult);
        
        // Process predictions
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> predictions = (List<Map<String, Object>>) aiResponse.get("predictions");
        if (predictions != null) {
            processPredictions(savedResult, predictions);
        }
        
        return savedResult;
    }
    
    /**
     * Process and save predictions
     */
    private void processPredictions(AiDiagnosisResult aiDiagnosisResult, List<Map<String, Object>> predictions) {
        List<DiagnosisPrediction> diagnosisPredictions = new ArrayList<>();
        
        for (Map<String, Object> prediction : predictions) {
            DiagnosisPrediction diagnosisPrediction = new DiagnosisPrediction();
            diagnosisPrediction.setAiDiagnosisResult(aiDiagnosisResult);
            diagnosisPrediction.setConditionName((String) prediction.get("conditionName"));
            diagnosisPrediction.setProbability((Double) prediction.get("probability"));
            diagnosisPrediction.setReasonForDiagnosis((String) prediction.get("reasonForDiagnosis"));
            diagnosisPrediction.setSupportingEvidence((String) prediction.get("supportingEvidence"));
            diagnosisPrediction.setSeverityLevel((Integer) prediction.get("severityLevel"));
            diagnosisPrediction.setRelatedConditions((String) prediction.get("relatedConditions"));
            
            diagnosisPredictions.add(diagnosisPrediction);
        }
        
        // Save all predictions
        diagnosisPredictionRepository.saveAll(diagnosisPredictions);
    }
    
    /**
     * Mark diagnosis as reviewed by doctor
     */
    @Transactional
    public AiDiagnosisResult markAsReviewed(Long diagnosisId, String doctorFeedback) {
        AiDiagnosisResult aiDiagnosisResult = getDiagnosisResultById(diagnosisId);
        
        aiDiagnosisResult.setReviewedByDoctor(true);
        aiDiagnosisResult.setDoctorFeedback(doctorFeedback);
        
        return aiDiagnosisResultRepository.save(aiDiagnosisResult);
    }
    
    /**
     * Delete AI diagnosis result
     */
    @Transactional
    public void deleteDiagnosisResult(Long id) {
        AiDiagnosisResult aiDiagnosisResult = getDiagnosisResultById(id);
        
        // Delete predictions first
        List<DiagnosisPrediction> predictions = diagnosisPredictionRepository.findByAiDiagnosisResultId(id);
        diagnosisPredictionRepository.deleteAll(predictions);
        
        // Delete diagnosis result
        aiDiagnosisResultRepository.delete(aiDiagnosisResult);
    }
}