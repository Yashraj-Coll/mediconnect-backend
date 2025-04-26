// src/main/java/com/mediconnect/service/HealthPredictorService.java

package com.mediconnect.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mediconnect.dto.HealthRiskAssessmentDTO;
import com.mediconnect.exception.HealthPredictionException;
import com.mediconnect.model.MedicalRecord;
import com.mediconnect.model.Patient;
import com.mediconnect.model.HealthRiskAssessment;
import com.mediconnect.repository.HealthRiskAssessmentRepository;
import com.mediconnect.repository.MedicalRecordRepository;
import com.mediconnect.repository.PatientRepository;

@Service
public class HealthPredictorService {

    public String getPredictorApiUrl() {
		return predictorApiUrl;
	}

	public void setPredictorApiUrl(String predictorApiUrl) {
		this.predictorApiUrl = predictorApiUrl;
	}

	public String getPredictorApiKey() {
		return predictorApiKey;
	}

	public void setPredictorApiKey(String predictorApiKey) {
		this.predictorApiKey = predictorApiKey;
	}

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public PatientRepository getPatientRepository() {
		return patientRepository;
	}

	public void setPatientRepository(PatientRepository patientRepository) {
		this.patientRepository = patientRepository;
	}

	public MedicalRecordRepository getMedicalRecordRepository() {
		return medicalRecordRepository;
	}

	public void setMedicalRecordRepository(MedicalRecordRepository medicalRecordRepository) {
		this.medicalRecordRepository = medicalRecordRepository;
	}

	public HealthRiskAssessmentRepository getHealthRiskAssessmentRepository() {
		return healthRiskAssessmentRepository;
	}

	public void setHealthRiskAssessmentRepository(HealthRiskAssessmentRepository healthRiskAssessmentRepository) {
		this.healthRiskAssessmentRepository = healthRiskAssessmentRepository;
	}

	@Value("${mediconnect.ai.health-predictor.api-url}")
    private String predictorApiUrl;
    
    @Value("${mediconnect.ai.health-predictor.api-key}")
    private String predictorApiKey;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    
    @Autowired
    private HealthRiskAssessmentRepository healthRiskAssessmentRepository;
    
    /**
     * Predict health risks and provide preventative recommendations
     */
    @SuppressWarnings("unchecked")
	public HealthRiskAssessmentDTO predictHealthRisks(Long patientId) {
        try {
            // Retrieve patient information
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new HealthPredictionException("Patient not found"));
            
            // Retrieve patient's medical records
            List<MedicalRecord> medicalRecords = medicalRecordRepository.findLatestRecordsByPatientId(patientId);
            
            // Extract relevant health data for prediction
            Map<String, Object> healthData = extractHealthData(patient, medicalRecords);
            
            // Prepare request to health predictor service
            Map<String, Object> requestBody = new HashMap<>(healthData);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", predictorApiKey);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Call health predictor API
            Map<String, Object> apiResponse = restTemplate.postForObject(
                    predictorApiUrl + "/predict", 
                    requestEntity, 
                    Map.class);
            
            // Save health risk assessment
            HealthRiskAssessment assessment = new HealthRiskAssessment();
            assessment.setPatient(patient);
            assessment.setOverallRiskLevel((String) apiResponse.get("overallRiskLevel"));
            assessment.setHealthRisks(apiResponse.get("healthRisks").toString());
            assessment.setRecommendations(apiResponse.get("recommendations").toString());
            assessment.setLifestyleSuggestions(apiResponse.get("lifestyleSuggestions").toString());
            assessment.setAssessedAt(java.time.LocalDateTime.now());
            
            healthRiskAssessmentRepository.save(assessment);
            
            // Create response DTO
            HealthRiskAssessmentDTO result = new HealthRiskAssessmentDTO();
            result.setAssessmentId(assessment.getId());
            result.setOverallRiskLevel(assessment.getOverallRiskLevel());
            result.setHealthRisks((Map<String, Double>) apiResponse.get("healthRisks"));
            result.setRecommendations((Map<String, List<String>>) apiResponse.get("recommendations"));
            result.setLifestyleSuggestions((Map<String, String>) apiResponse.get("lifestyleSuggestions"));
            result.setNextCheckupRecommendation((String) apiResponse.get("nextCheckupRecommendation"));
            
            return result;
            
        } catch (Exception e) {
            throw new HealthPredictionException("Failed to predict health risks: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract relevant health data from patient and medical records
     */
    private Map<String, Object> extractHealthData(Patient patient, List<MedicalRecord> medicalRecords) {
        Map<String, Object> healthData = new HashMap<>();
        
        // Basic patient information
        healthData.put("age", calculateAge(patient.getDateOfBirth()));
        healthData.put("gender", patient.getGender().toString());
        healthData.put("height", patient.getHeight());
        healthData.put("weight", patient.getWeight());
        healthData.put("bmi", calculateBMI(patient.getHeight(), patient.getWeight()));
        healthData.put("allergies", patient.getAllergies());
        healthData.put("chronicDiseases", patient.getChronicDiseases());
        
        // Extract data from medical records if available
        if (!medicalRecords.isEmpty()) {
            MedicalRecord latestRecord = medicalRecords.get(0);
            
            healthData.put("bloodPressure", latestRecord.getBloodPressure());
            healthData.put("heartRate", latestRecord.getHeartRate());
            healthData.put("oxygenSaturation", latestRecord.getOxygenSaturation());
            healthData.put("temperature", latestRecord.getTemperature());
            
            // Add historical diagnoses
            List<String> diagnoses = medicalRecords.stream()
                    .map(MedicalRecord::getDiagnosis)
                    .filter(diagnosis -> diagnosis != null && !diagnosis.isEmpty())
                    .toList();
            
            healthData.put("medicalHistory", diagnoses);
        }
        
        return healthData;
    }
    
    /**
     * Calculate patient age in years
     */
    private int calculateAge(java.time.LocalDate dateOfBirth) {
        return java.time.Period.between(dateOfBirth, java.time.LocalDate.now()).getYears();
    }
    
    /**
     * Calculate BMI (Body Mass Index)
     */
    private double calculateBMI(Double heightCm, Double weightKg) {
        if (heightCm == null || weightKg == null || heightCm == 0) {
            return 0;
        }
        
        // Convert height from cm to meters
        double heightM = heightCm / 100.0;
        
        // Calculate BMI: weight (kg) / (height (m))²
        return Math.round((weightKg / (heightM * heightM)) * 10) / 10.0;
    }
}