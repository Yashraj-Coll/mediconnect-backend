package com.mediconnect.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
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
import org.springframework.web.multipart.MultipartFile;

import com.mediconnect.dto.VoiceTranscriptionDTO;
import com.mediconnect.exception.TranscriptionException;
import com.mediconnect.model.MedicalRecord;
import com.mediconnect.repository.MedicalRecordRepository;
import com.mediconnect.util.AudioUtils;

@Service
public class VoiceTranscriptionService {

    @Value("${mediconnect.speech-to-text.api-url}")
    private String speechToTextApiUrl;
    
    @Value("${mediconnect.speech-to-text.api-key}")
    private String speechToTextApiKey;
    
    @Value("${mediconnect.medical-coding.api-url}")
    private String medicalCodingApiUrl;
    
    @Value("${mediconnect.medical-coding.api-key}")
    private String medicalCodingApiKey;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    
    @SuppressWarnings("unused")
	@Autowired
    private FileStorageService fileStorageService;
    
    /**
     * Transcribe an audio file to text
     */
    @SuppressWarnings("unchecked")
	public VoiceTranscriptionDTO transcribeAudio(MultipartFile audioFile) {
        try {
            // Store audio file temporarily
            Path tempFile = Files.createTempFile("audio_", ".wav");
            audioFile.transferTo(tempFile.toFile());
            
            // Convert audio to required format if needed
            Path processedAudio = AudioUtils.convertToRequiredFormat(tempFile);
            
            // Encode audio as base64 for API
            byte[] audioBytes = Files.readAllBytes(processedAudio);
            String base64Audio = Base64.getEncoder().encodeToString(audioBytes);
            
            // Prepare request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("audio", base64Audio);
            requestBody.put("language", "en-US");
            requestBody.put("domain", "medical");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", speechToTextApiKey);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Call speech-to-text API
            Map<String, Object> apiResponse = restTemplate.postForObject(
                    speechToTextApiUrl, 
                    requestEntity, 
                    Map.class);
            
            // Clean up temporary files
            Files.deleteIfExists(tempFile);
            if (!tempFile.equals(processedAudio)) {
                Files.deleteIfExists(processedAudio);
            }
            
            // Process response
            String transcribedText = (String) apiResponse.get("transcription");
            
            // Suggest medical codes
            Map<String, Object> codingRequestBody = new HashMap<>();
            codingRequestBody.put("clinicalText", transcribedText);
            
            HttpHeaders codingHeaders = new HttpHeaders();
            codingHeaders.setContentType(MediaType.APPLICATION_JSON);
            codingHeaders.set("X-API-Key", medicalCodingApiKey);
            
            HttpEntity<Map<String, Object>> codingRequestEntity = new HttpEntity<>(codingRequestBody, codingHeaders);
            
            // Call medical coding API
            Map<String, Object> codingResponse = restTemplate.postForObject(
                    medicalCodingApiUrl, 
                    codingRequestEntity, 
                    Map.class);
            
            // Check transcription quality and completeness
            double completenessScore = calculateCompleteness(transcribedText);
            boolean hasPatientInfo = transcribedText.toLowerCase().contains("patient") || 
                                    transcribedText.toLowerCase().contains("name") ||
                                    transcribedText.toLowerCase().contains("age");
            
            boolean hasDiagnosis = transcribedText.toLowerCase().contains("diagnos") || 
                                 transcribedText.toLowerCase().contains("condition") ||
                                 transcribedText.toLowerCase().contains("assessment");
            
            boolean hasTreatment = transcribedText.toLowerCase().contains("treatment") || 
                                 transcribedText.toLowerCase().contains("medication") ||
                                 transcribedText.toLowerCase().contains("prescription") ||
                                 transcribedText.toLowerCase().contains("plan");
            
            // Prepare response
            VoiceTranscriptionDTO result = new VoiceTranscriptionDTO();
            result.setTranscribedText(transcribedText);
            result.setIcdCodes((List<Map<String, Object>>) codingResponse.get("icdCodes"));
            result.setCptCodes((List<Map<String, Object>>) codingResponse.get("cptCodes"));
            result.setConfidenceScore((Double) apiResponse.get("confidenceScore"));
            result.setCompletenessScore(completenessScore);
            result.setHasPatientInfo(hasPatientInfo);
            result.setHasDiagnosis(hasDiagnosis);
            result.setHasTreatment(hasTreatment);
            result.setSuggestions(generateSuggestions(hasPatientInfo, hasDiagnosis, hasTreatment));
            
            return result;
            
        } catch (IOException e) {
            throw new TranscriptionException("Failed to process audio file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new TranscriptionException("Transcription failed: " + e.getMessage(), e);
        }
    }
    
 // Continue VoiceTranscriptionService.java

    /**
     * Apply transcription to medical record
     */
    public MedicalRecord applyTranscriptionToMedicalRecord(Long medicalRecordId, VoiceTranscriptionDTO transcription) {
        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));
        
        // Extract diagnosis and treatment plan from transcription
        String notes = transcription.getTranscribedText();
        
        // Update medical record
        medicalRecord.setNotes(notes);
        
        // Try to extract diagnosis if not already set
        if ((medicalRecord.getDiagnosis() == null || medicalRecord.getDiagnosis().isEmpty()) && 
                transcription.getHasDiagnosis()) {
            // Simple extraction - in a real system, this would use more sophisticated NLP
            int diagnosisIndex = notes.toLowerCase().indexOf("diagnos");
            if (diagnosisIndex != -1) {
                String diagnosisSection = notes.substring(diagnosisIndex);
                // Extract until the next major section or end
                int nextSectionIndex = findNextSectionIndex(diagnosisSection);
                String diagnosis = nextSectionIndex != -1 
                        ? diagnosisSection.substring(0, nextSectionIndex).trim() 
                        : diagnosisSection.trim();
                
                medicalRecord.setDiagnosis(diagnosis);
            }
        }
        
        // Try to extract treatment if not already set
        if ((medicalRecord.getTreatment() == null || medicalRecord.getTreatment().isEmpty()) && 
                transcription.getHasTreatment()) {
            // Simple extraction
            int treatmentIndex = notes.toLowerCase().indexOf("treatment");
            if (treatmentIndex != -1) {
                String treatmentSection = notes.substring(treatmentIndex);
                // Extract until the next major section or end
                int nextSectionIndex = findNextSectionIndex(treatmentSection);
                String treatment = nextSectionIndex != -1 
                        ? treatmentSection.substring(0, nextSectionIndex).trim() 
                        : treatmentSection.trim();
                
                medicalRecord.setTreatment(treatment);
            }
        }
        
        // Add ICD codes if present
        if (transcription.getIcdCodes() != null && !transcription.getIcdCodes().isEmpty()) {
            StringBuilder diagnosisCodes = new StringBuilder();
            for (Map<String, Object> code : transcription.getIcdCodes()) {
                diagnosisCodes.append(code.get("code")).append(": ")
                        .append(code.get("description")).append("\n");
            }
            
            String existingDiagnosis = medicalRecord.getDiagnosis();
            if (existingDiagnosis != null && !existingDiagnosis.isEmpty()) {
                medicalRecord.setDiagnosis(existingDiagnosis + "\n\nICD Codes:\n" + diagnosisCodes);
            } else {
                medicalRecord.setDiagnosis("ICD Codes:\n" + diagnosisCodes);
            }
        }
        
        return medicalRecordRepository.save(medicalRecord);
    }
    
    /**
     * Find index of next major section in medical notes
     */
    private int findNextSectionIndex(String text) {
        String[] sectionKeywords = {"assessment", "plan", "treatment", "medication", 
                                   "follow", "recommendation", "patient", "history"};
        
        int earliestIndex = -1;
        
        for (String keyword : sectionKeywords) {
            int index = text.toLowerCase().indexOf(keyword, 10); // Start after current section name
            if (index != -1 && (earliestIndex == -1 || index < earliestIndex)) {
                earliestIndex = index;
            }
        }
        
        return earliestIndex;
    }
    
    /**
     * Calculate completeness score of transcription
     */
    private double calculateCompleteness(String transcription) {
        // Simple implementation - count key medical documentation components
        String[] requiredComponents = {
            "patient", "history", "symptoms", "exam", "assessment", 
            "diagnosis", "plan", "treatment", "follow"
        };
        
        int foundComponents = 0;
        String lowerText = transcription.toLowerCase();
        
        for (String component : requiredComponents) {
            if (lowerText.contains(component)) {
                foundComponents++;
            }
        }
        
        return (double) foundComponents / requiredComponents.length;
    }
    
    /**
     * Generate suggestions for improving documentation
     */
    private List<String> generateSuggestions(boolean hasPatientInfo, boolean hasDiagnosis, boolean hasTreatment) {
        List<String> suggestions = new ArrayList<>();
        
        if (!hasPatientInfo) {
            suggestions.add("Add patient information and relevant history");
        }
        
        if (!hasDiagnosis) {
            suggestions.add("Include diagnosis/assessment of the patient's condition");
        }
        
        if (!hasTreatment) {
            suggestions.add("Specify treatment plan or follow-up recommendations");
        }
        
        return suggestions;
    }
}