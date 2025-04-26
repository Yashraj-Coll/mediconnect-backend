// src/main/java/com/mediconnect/service/TriageService.java

package com.mediconnect.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mediconnect.dto.TriageRequestDTO;
import com.mediconnect.dto.TriageResultDTO;
import com.mediconnect.exception.TriageException;
import com.mediconnect.model.Patient;
import com.mediconnect.model.TriageRecord;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.repository.TriageRecordRepository;

@Service
public class TriageService {

    @Value("${mediconnect.ai.triage.api-url}")
    private String triageApiUrl;
    
    @Value("${mediconnect.ai.triage.api-key}")
    private String triageApiKey;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private TriageRecordRepository triageRecordRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Assess symptom urgency and recommend action
     */
    @SuppressWarnings("unchecked")
	public TriageResultDTO assessUrgency(TriageRequestDTO request) {
        try {
            // Retrieve patient information
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new TriageException("Patient not found"));
            
            // Prepare request to AI triage service
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("symptoms", request.getSymptoms());
            requestBody.put("age", calculateAge(patient.getDateOfBirth()));
            requestBody.put("gender", patient.getGender().toString());
            requestBody.put("vitalSigns", request.getVitalSigns());
            requestBody.put("medicalHistory", patient.getChronicDiseases());
            requestBody.put("allergies", patient.getAllergies());
            requestBody.put("currentMedications", request.getCurrentMedications());
            requestBody.put("painLevel", request.getPainLevel());
            requestBody.put("symptomDuration", request.getSymptomDuration());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", triageApiKey);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Call triage API
            Map<String, Object> apiResponse = restTemplate.postForObject(
                    triageApiUrl + "/assess", 
                    requestEntity, 
                    Map.class);
            
            // Create triage record
            TriageRecord triageRecord = new TriageRecord();
            triageRecord.setPatient(patient);
            triageRecord.setSymptoms(request.getSymptoms());
            triageRecord.setUrgencyLevel((String) apiResponse.get("urgencyLevel"));
            triageRecord.setRecommendedAction((String) apiResponse.get("recommendedAction"));
            triageRecord.setAssessmentDetails((String) apiResponse.get("assessmentDetails"));
            triageRecord.setConfidenceScore(Double.parseDouble(apiResponse.get("confidenceScore").toString()));
            triageRecord.setTriagedAt(java.time.LocalDateTime.now());
            
            triageRecordRepository.save(triageRecord);
            
            // Send notification for high urgency cases
            if ("HIGH".equals(triageRecord.getUrgencyLevel()) || "EMERGENCY".equals(triageRecord.getUrgencyLevel())) {
                notificationService.sendUrgentTriageNotification(triageRecord);
            }
            
            // Create response DTO
            TriageResultDTO result = new TriageResultDTO();
            result.setTriageId(triageRecord.getId());
            result.setUrgencyLevel(triageRecord.getUrgencyLevel());
            result.setRecommendedAction(triageRecord.getRecommendedAction());
            result.setAssessmentDetails(triageRecord.getAssessmentDetails());
            result.setConfidenceScore(triageRecord.getConfidenceScore());
            result.setPossibleConditions((Map<String, Double>) apiResponse.get("possibleConditions"));
            result.setWarningSignsToMonitor((java.util.List<String>) apiResponse.get("warningSignsToMonitor"));
            
            return result;
            
        } catch (Exception e) {
            throw new TriageException("Failed to perform triage: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calculate patient age in years
     */
    private int calculateAge(java.time.LocalDate dateOfBirth) {
        return java.time.Period.between(dateOfBirth, java.time.LocalDate.now()).getYears();
    }
}