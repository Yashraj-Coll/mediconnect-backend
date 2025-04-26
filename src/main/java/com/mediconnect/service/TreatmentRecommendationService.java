// src/main/java/com/mediconnect/service/TreatmentRecommendationService.java

package com.mediconnect.service;

import java.util.ArrayList;
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

import com.mediconnect.dto.TreatmentOptionDTO;
import com.mediconnect.dto.TreatmentRecommendationDTO;
import com.mediconnect.exception.TreatmentRecommendationException;
import com.mediconnect.model.MedicalRecord;
import com.mediconnect.model.Patient;
import com.mediconnect.model.TreatmentRecommendation;
import com.mediconnect.repository.MedicalRecordRepository;
import com.mediconnect.repository.TreatmentRecommendationRepository;

@Service
public class TreatmentRecommendationService {

    @Value("${mediconnect.ai.treatment.api-url}")
    private String treatmentApiUrl;
    
    @Value("${mediconnect.ai.treatment.api-key}")
    private String treatmentApiKey;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    
    @Autowired
    private TreatmentRecommendationRepository treatmentRecommendationRepository;
    
    /**
     * Generate personalized treatment recommendations
     */
    @SuppressWarnings("unchecked")
	public TreatmentRecommendationDTO recommendTreatments(Long medicalRecordId) {
        try {
            // Retrieve medical record information
            MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                    .orElseThrow(() -> new TreatmentRecommendationException("Medical record not found"));
            
            Patient patient = medicalRecord.getPatient();
            
            // Prepare request to AI treatment service
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("diagnosis", medicalRecord.getDiagnosis());
            requestBody.put("symptoms", medicalRecord.getSymptoms());
            requestBody.put("patientAge", calculateAge(patient.getDateOfBirth()));
            requestBody.put("patientGender", patient.getGender().toString());
            requestBody.put("allergies", patient.getAllergies());
            requestBody.put("chronicDiseases", patient.getChronicDiseases());
            requestBody.put("vitalSigns", Map.of(
                "bloodPressure", medicalRecord.getBloodPressure(),
                "temperature", medicalRecord.getTemperature(),
                "heartRate", medicalRecord.getHeartRate(),
                "oxygenSaturation", medicalRecord.getOxygenSaturation()
            ));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", treatmentApiKey);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Call treatment recommendation API
            Map<String, Object> apiResponse = restTemplate.postForObject(
                    treatmentApiUrl + "/recommend", 
                    requestEntity, 
                    Map.class);
            
            // Process treatment options from API response
            List<Map<String, Object>> apiTreatmentOptions = (List<Map<String, Object>>) apiResponse.get("treatmentOptions");
            List<TreatmentOptionDTO> treatmentOptions = new ArrayList<>();
            
            for (Map<String, Object> option : apiTreatmentOptions) {
                TreatmentOptionDTO treatmentOption = new TreatmentOptionDTO();
                treatmentOption.setName((String) option.get("name"));
                treatmentOption.setDescription((String) option.get("description"));
                treatmentOption.setType((String) option.get("type"));
                treatmentOption.setEffectiveness((Double) option.get("effectiveness"));
                treatmentOption.setRisks((List<String>) option.get("risks"));
                treatmentOption.setBenefits((List<String>) option.get("benefits"));
                treatmentOption.setContraindications((List<String>) option.get("contraindications"));
                treatmentOption.setSideEffects((List<String>) option.get("sideEffects"));
                treatmentOption.setTimeToEffect((String) option.get("timeToEffect"));
                treatmentOption.setCost((Integer) option.get("cost"));
                
                treatmentOptions.add(treatmentOption);
            }
            
            // Save treatment recommendation
            TreatmentRecommendation recommendation = new TreatmentRecommendation();
            recommendation.setMedicalRecord(medicalRecord);
            recommendation.setOverview((String) apiResponse.get("overview"));
            recommendation.setPersonalizationFactors((String) apiResponse.get("personalizationFactors"));
            recommendation.setLifestyleRecommendations((String) apiResponse.get("lifestyleRecommendations"));
            recommendation.setFollowUpRecommendations((String) apiResponse.get("followUpRecommendations"));
            recommendation.setTreatmentOptions(apiResponse.get("treatmentOptions").toString());
            recommendation.setGeneratedAt(java.time.LocalDateTime.now());
            
            treatmentRecommendationRepository.save(recommendation);
            
            // Create response DTO
            TreatmentRecommendationDTO result = new TreatmentRecommendationDTO();
            result.setRecommendationId(recommendation.getId());
            result.setOverview(recommendation.getOverview());
            result.setPersonalizationFactors(recommendation.getPersonalizationFactors());
            result.setLifestyleRecommendations(recommendation.getLifestyleRecommendations());
            result.setFollowUpRecommendations(recommendation.getFollowUpRecommendations());
            result.setTreatmentOptions(treatmentOptions);
            
            return result;
            
        } catch (Exception e) {
            throw new TreatmentRecommendationException("Failed to generate treatment recommendations: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calculate patient age in years
     */
    private int calculateAge(java.time.LocalDate dateOfBirth) {
        return java.time.Period.between(dateOfBirth, java.time.LocalDate.now()).getYears();
    }
}