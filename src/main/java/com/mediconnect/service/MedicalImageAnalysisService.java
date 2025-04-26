// src/main/java/com/mediconnect/service/MedicalImageAnalysisService.java

package com.mediconnect.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediconnect.exception.ImageAnalysisException;
import com.mediconnect.model.ImageAnalysisResult;
import com.mediconnect.model.MedicalRecord;
import com.mediconnect.repository.ImageAnalysisResultRepository;
import com.mediconnect.repository.MedicalRecordRepository;

@Service
public class MedicalImageAnalysisService {

    @Value("${mediconnect.ai.image-analysis.api-url}")
    private String apiUrl;
    
    @Value("${mediconnect.ai.image-analysis.api-key}")
    private String apiKey;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    
    @Autowired
    private ImageAnalysisResultRepository imageAnalysisResultRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Analyze a medical image and store the result
     */
    @Async("aiTaskExecutor")
    public CompletableFuture<ImageAnalysisResult> analyzeImage(Long medicalRecordId, MultipartFile imageFile, String imageType) {
        try {
            // Validate the medical record exists
            MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                    .orElseThrow(() -> new ImageAnalysisException("Medical record not found"));
            
            // Create analysis result with pending status
            ImageAnalysisResult result = new ImageAnalysisResult();
            result.setMedicalRecord(medicalRecord);
            result.setImageType(imageType);
            result.setStatus(ImageAnalysisResult.AnalysisStatus.PROCESSING);
            result.setSubmittedAt(java.time.LocalDateTime.now());
            imageAnalysisResultRepository.save(result);
            
            // Prepare headers for API call
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("X-API-Key", apiKey);
            
            // Prepare request body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", imageFile.getResource());
            body.add("type", imageType);
            body.add("patient_age", calculateAge(medicalRecord.getPatient().getDateOfBirth()));
            body.add("patient_gender", medicalRecord.getPatient().getGender().toString());
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // Make API call
            @SuppressWarnings("unchecked")
			Map<String, Object> apiResponse = restTemplate.postForObject(
                    apiUrl + "/analyze", 
                    requestEntity, 
                    Map.class);
            
            // Process the response
            result.setStatus(ImageAnalysisResult.AnalysisStatus.COMPLETED);
            result.setFindings(new ObjectMapper().writeValueAsString(apiResponse.get("findings")));
            result.setConfidenceScore((Double) apiResponse.get("confidence"));
            result.setAnalysisReport((String) apiResponse.get("report"));
            result.setCompletedAt(java.time.LocalDateTime.now());
            
            // Save the updated result
            imageAnalysisResultRepository.save(result);
            
            // Notify doctor if high urgency findings
            if (apiResponse.containsKey("urgency") && "HIGH".equals(apiResponse.get("urgency"))) {
                notificationService.sendUrgentImageAnalysisNotification(result);
            }
            
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            // Handle API or processing error
            handleAnalysisError(medicalRecordId, e);
            throw new ImageAnalysisException("Failed to analyze image: " + e.getMessage(), e);
        }
    }
    
    /**
     * Analyze X-ray image
     */
    public CompletableFuture<ImageAnalysisResult> analyzeXRay(Long medicalRecordId, MultipartFile imageFile) {
        return analyzeImage(medicalRecordId, imageFile, "X_RAY");
    }
    
    /**
     * Analyze MRI scan
     */
    public CompletableFuture<ImageAnalysisResult> analyzeMRI(Long medicalRecordId, MultipartFile imageFile) {
        return analyzeImage(medicalRecordId, imageFile, "MRI");
    }
    
    /**
     * Analyze CT scan
     */
    public CompletableFuture<ImageAnalysisResult> analyzeCTScan(Long medicalRecordId, MultipartFile imageFile) {
        return analyzeImage(medicalRecordId, imageFile, "CT_SCAN");
    }
    
    /**
     * Analyze dermatology image
     */
    public CompletableFuture<ImageAnalysisResult> analyzeDermatologyImage(Long medicalRecordId, MultipartFile imageFile) {
        return analyzeImage(medicalRecordId, imageFile, "DERMATOLOGY");
    }
    
    /**
     * Handle errors in analysis process
     */
    private void handleAnalysisError(Long medicalRecordId, Exception e) {
        try {
            // Find any pending analysis and mark as failed
            imageAnalysisResultRepository.findPendingByMedicalRecordId(medicalRecordId)
                .ifPresent(result -> {
                    result.setStatus(ImageAnalysisResult.AnalysisStatus.FAILED);
                    result.setErrorMessage(e.getMessage());
                    result.setCompletedAt(java.time.LocalDateTime.now());
                    imageAnalysisResultRepository.save(result);
                });
        } catch (Exception ex) {
            // Log the error but don't throw it to avoid masking the original error
            System.err.println("Error updating failed status: " + ex.getMessage());
        }
    }
    
    /**
     * Calculate patient age
     */
    private int calculateAge(java.time.LocalDate dateOfBirth) {
        return java.time.Period.between(dateOfBirth, java.time.LocalDate.now()).getYears();
    }
}