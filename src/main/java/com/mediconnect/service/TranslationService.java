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

import com.mediconnect.dto.TranslationRequestDTO;
import com.mediconnect.dto.TranslationResponseDTO;
import com.mediconnect.exception.TranslationException;
import com.mediconnect.model.Patient;
import com.mediconnect.repository.PatientRepository;

@Service
public class TranslationService {

    @Value("${mediconnect.translation.api-url}")
    private String translationApiUrl;
    
    @Value("${mediconnect.translation.api-key}")
    private String translationApiKey;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private PatientRepository patientRepository;
    
    /**
     * Translate medical content to target language
     */
    public TranslationResponseDTO translateContent(TranslationRequestDTO request) {
        try {
            // Prepare request for translation API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", request.getText());
            requestBody.put("sourceLanguage", request.getSourceLanguage());
            requestBody.put("targetLanguage", request.getTargetLanguage());
            requestBody.put("domain", "medical");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", translationApiKey);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Call translation API
            @SuppressWarnings("unchecked")
			Map<String, Object> apiResponse = restTemplate.postForObject(
                    translationApiUrl, 
                    requestEntity, 
                    Map.class);
            
            // Process response
            String translatedText = (String) apiResponse.get("translatedText");
            Double confidenceScore = (Double) apiResponse.get("confidenceScore");
            
            // Check for cultural adaptations
            boolean hasCulturalAdaptations = false;
            String culturalNotes = "";
            
            if (apiResponse.containsKey("culturalNotes")) {
                culturalNotes = (String) apiResponse.get("culturalNotes");
                hasCulturalAdaptations = culturalNotes != null && !culturalNotes.isEmpty();
            }
            
            // Prepare response
            TranslationResponseDTO response = new TranslationResponseDTO();
            response.setOriginalText(request.getText());
            response.setTranslatedText(translatedText);
            response.setSourceLanguage(request.getSourceLanguage());
            response.setTargetLanguage(request.getTargetLanguage());
            response.setConfidenceScore(confidenceScore);
            response.setHasCulturalAdaptations(hasCulturalAdaptations);
            response.setCulturalNotes(culturalNotes);
            
            return response;
            
        } catch (Exception e) {
            throw new TranslationException("Translation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Translate medical instructions for a specific patient
     */
    public TranslationResponseDTO translateForPatient(Long patientId, String text) {
        try {
            // Get patient's preferred language from profile
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new TranslationException("Patient not found"));
            
            String preferredLanguage = patient.getPreferredLanguage();
            if (preferredLanguage == null || preferredLanguage.isEmpty() || preferredLanguage.equals("en")) {
                // Default to English if no preference or already English
                TranslationResponseDTO response = new TranslationResponseDTO();
                response.setOriginalText(text);
                response.setTranslatedText(text);
                response.setSourceLanguage("en");
                response.setTargetLanguage("en");
                response.setConfidenceScore(1.0);
                response.setHasCulturalAdaptations(false);
                
                return response;
            }
            
            // Prepare translation request
            TranslationRequestDTO request = new TranslationRequestDTO();
            request.setText(text);
            request.setSourceLanguage("en");
            request.setTargetLanguage(preferredLanguage);
            
            return translateContent(request);
            
        } catch (Exception e) {
            throw new TranslationException("Translation for patient failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get cultural health recommendations for a specific language/region
     */
    public Map<String, String> getCulturalHealthRecommendations(String language, String region) {
        try {
            // Prepare request for cultural recommendations API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("language", language);
            requestBody.put("region", region);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", translationApiKey);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Call cultural recommendations API
            @SuppressWarnings("unchecked")
			Map<String, String> apiResponse = restTemplate.postForObject(
                    translationApiUrl + "/cultural-recommendations", 
                    requestEntity, 
                    Map.class);
            
            return apiResponse;
            
        } catch (Exception e) {
            throw new TranslationException("Failed to get cultural health recommendations: " + e.getMessage(), e);
        }
    }
}