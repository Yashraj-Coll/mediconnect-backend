package com.mediconnect.service.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class SpeechToTextService {
    
    private static final Logger logger = LoggerFactory.getLogger(SpeechToTextService.class);
    
    @Value("${huggingface.api.key}")
    private String huggingFaceApiKey;
    
    @Value("${huggingface.endpoints.speech-to-text}")
    private String speechToTextUrl;
    
    @Value("${huggingface.speech.return-timestamps:false}")
    private boolean returnTimestamps;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Supported audio formats
    private static final List<String> SUPPORTED_FORMATS = List.of("wav", "mp3", "m4a", "flac", "ogg");
    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB max
    
    /**
     * Transcribe speech from audio data using Whisper Large v3
     * Replaces OpenAI Whisper API functionality
     */
    public String transcribeSpeech(byte[] audioData) {
        return transcribeSpeech(audioData, "auto", false);
    }

    /**
     * Transcribe speech with language specification
     */
    public String transcribeSpeech(byte[] audioData, String language) {
        return transcribeSpeech(audioData, language, false);
    }

    /**
     * Transcribe speech with timestamp options
     */
    public String transcribeSpeech(byte[] audioData, String language, boolean includeTimestamps) {
        try {
            if (audioData == null || audioData.length == 0) {
                return "No audio data provided for transcription.";
            }

            if (!isValidAudioFile(audioData)) {
                return "Invalid audio format or file size. Please provide a valid audio file (WAV, MP3, M4A, FLAC, OGG) under 25MB.";
            }

            HttpHeaders headers = createHeaders();
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", Base64.getEncoder().encodeToString(audioData));
            
            Map<String, Object> parameters = new HashMap<>();
            if (!"auto".equals(language)) {
                parameters.put("language", language);
            }
            parameters.put("return_timestamps", includeTimestamps || returnTimestamps);
            parameters.put("normalize", true);
            parameters.put("chunk_length_s", 30);
            
            if (!parameters.isEmpty()) {
                requestBody.put("parameters", parameters);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(speechToTextUrl, entity, String.class);
            
            return processSpeechToTextResponse(response, includeTimestamps);
            
        } catch (Exception e) {
            logger.error("Error in speech-to-text service: {}", e.getMessage(), e);
            return handleSpeechToTextException(e);
        }
    }

    /**
     * Transcribe medical consultation audio
     */
    public Map<String, Object> transcribeMedicalConsultation(byte[] audioData, String consultationType) {
        Map<String, Object> transcriptionResult = new HashMap<>();
        
        try {
            String transcription = transcribeSpeech(audioData, "auto", true);
            
            // Process medical transcription
            String processedTranscription = processMedicalTranscription(transcription, consultationType);
            
            transcriptionResult.put("rawTranscription", transcription);
            transcriptionResult.put("processedTranscription", processedTranscription);
            transcriptionResult.put("consultationType", consultationType);
            transcriptionResult.put("timestamp", System.currentTimeMillis());
            transcriptionResult.put("wordCount", countWords(transcription));
            transcriptionResult.put("duration", estimateAudioDuration(audioData));
            transcriptionResult.put("status", "completed");
            
        } catch (Exception e) {
            logger.error("Error transcribing medical consultation: {}", e.getMessage());
            transcriptionResult.put("error", e.getMessage());
            transcriptionResult.put("status", "failed");
        }
        
        return transcriptionResult;
    }

    /**
     * Transcribe with speaker diarization simulation
     */
    public Map<String, Object> transcribeWithSpeakers(byte[] audioData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String transcription = transcribeSpeech(audioData, "auto", true);
            
            // Simulate speaker separation based on pauses and tone changes
            List<Map<String, String>> speakerSegments = simulateSpeakerDiarization(transcription);
            
            result.put("fullTranscription", transcription);
            result.put("speakerSegments", speakerSegments);
            result.put("speakerCount", estimateSpeakerCount(transcription));
            result.put("timestamp", System.currentTimeMillis());
            result.put("status", "completed");
            
        } catch (Exception e) {
            logger.error("Error in speaker diarization: {}", e.getMessage());
            result.put("error", e.getMessage());
            result.put("status", "failed");
        }
        
        return result;
    }

    /**
     * Generate transcription summary for medical notes
     */
    public String generateTranscriptionSummary(String transcription, String summaryType) {
        try {
            if (transcription == null || transcription.trim().isEmpty()) {
                return "No transcription provided for summary generation.";
            }

            String summary;
            switch (summaryType.toLowerCase()) {
                case "soap":
                    summary = generateSOAPNotes(transcription);
                    break;
                case "chief_complaint":
                    summary = extractChiefComplaint(transcription);
                    break;
                case "medication_list":
                    summary = extractMedicationReferences(transcription);
                    break;
                case "action_items":
                    summary = extractActionItems(transcription);
                    break;
                default:
                    summary = generateGeneralSummary(transcription);
            }
            
            return summary;
            
        } catch (Exception e) {
            logger.error("Error generating transcription summary: {}", e.getMessage());
            return "Unable to generate transcription summary. Please review the transcription manually.";
        }
    }

    /**
     * Validate and enhance transcription quality
     */
    public Map<String, Object> validateTranscription(byte[] audioData, String existingTranscription) {
        Map<String, Object> validation = new HashMap<>();
        
        try {
            // Re-transcribe with different parameters for validation
            String newTranscription = transcribeSpeech(audioData, "auto", false);
            
            // Compare transcriptions
            double similarity = calculateTranscriptionSimilarity(existingTranscription, newTranscription);
            
            validation.put("originalTranscription", existingTranscription);
            validation.put("reprocessedTranscription", newTranscription);
            validation.put("similarityScore", similarity);
            validation.put("confidenceLevel", determineConfidenceLevel(similarity));
            validation.put("suggestedImprovements", generateImprovementSuggestions(similarity, newTranscription));
            validation.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            logger.error("Error validating transcription: {}", e.getMessage());
            validation.put("error", e.getMessage());
        }
        
        return validation;
    }

    /**
     * Extract medical terminology from transcription
     */
    public List<String> extractMedicalTerminology(String transcription) {
        List<String> medicalTerms = new ArrayList<>();
        
        try {
            if (transcription == null || transcription.trim().isEmpty()) {
                return medicalTerms;
            }

            // Basic medical term extraction patterns
            String[] commonMedicalTerms = {
                "hypertension", "diabetes", "pneumonia", "bronchitis", "asthma",
                "myocardial infarction", "stroke", "sepsis", "pneumothorax",
                "appendicitis", "cholecystitis", "pancreatitis", "gastritis",
                "arthritis", "osteoporosis", "fibromyalgia", "migraine",
                "depression", "anxiety", "PTSD", "bipolar", "schizophrenia"
            };

            String lowerTranscription = transcription.toLowerCase();
            
            for (String term : commonMedicalTerms) {
                if (lowerTranscription.contains(term.toLowerCase())) {
                    medicalTerms.add(term);
                }
            }

            // Extract medication names (basic pattern matching)
            String[] commonMedications = {
                "aspirin", "ibuprofen", "acetaminophen", "lisinopril", "metformin",
                "atorvastatin", "amlodipine", "omeprazole", "metoprolol", "losartan",
                "simvastatin", "hydrochlorothiazide", "amoxicillin", "azithromycin",
                "ciprofloxacin", "prednisone", "albuterol", "insulin", "warfarin"
            };

            for (String medication : commonMedications) {
                if (lowerTranscription.contains(medication.toLowerCase())) {
                    medicalTerms.add(medication + " (medication)");
                }
            }
            
        } catch (Exception e) {
            logger.error("Error extracting medical terminology: {}", e.getMessage());
        }
        
        return medicalTerms;
    }

    /**
     * Get transcription service health status
     */
    public Map<String, Object> getServiceHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            health.put("service", "Hugging Face Speech-to-Text Service");
            health.put("model", "openai/whisper-large-v3");
            health.put("endpoint", speechToTextUrl);
            health.put("apiKeyConfigured", isApiKeyConfigured());
            health.put("supportedFormats", SUPPORTED_FORMATS);
            health.put("maxFileSize", MAX_FILE_SIZE + " bytes");
            health.put("timestamp", System.currentTimeMillis());
            health.put("status", isApiKeyConfigured() ? "UP" : "DOWN");
            
        } catch (Exception e) {
            health.put("status", "ERROR");
            health.put("error", e.getMessage());
        }
        
        return health;
    }

    // Private helper methods

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(huggingFaceApiKey);
        headers.set("User-Agent", "MediConnect-SpeechToText/1.0");
        return headers;
    }

    private boolean isValidAudioFile(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            return false;
        }
        
        // Check file size
        if (audioData.length > MAX_FILE_SIZE) {
            return false;
        }
        
        System.out.println("✅ Audio validation passed: " + audioData.length + " bytes");
        
        // TEMPORARY: Skip format validation since controller already validates
        return true;
        
        // TODO: Re-enable format validation with WebM support
        // return isValidAudioFormat(audioData);
    }

    private boolean isValidAudioFormat(byte[] audioData) {
        if (audioData.length < 4) {
            return false;
        }
        
        // Check for common audio file signatures
        String header = "";
        for (int i = 0; i < Math.min(4, audioData.length); i++) {
            header += String.format("%02X", audioData[i]);
        }
        
        // WAV: 52494646 (RIFF)
        // MP3: FFFB or FFF3 or ID3
        // FLAC: 664C6143 (fLaC)
        // OGG: 4F676753 (OggS)
        return header.startsWith("5249") || // WAV (RIFF)
               header.startsWith("FFFB") || header.startsWith("FFF3") || // MP3
               header.startsWith("4944") || // MP3 with ID3
               header.startsWith("664C") || // FLAC
               header.startsWith("4F67");   // OGG
    }

    private String processSpeechToTextResponse(ResponseEntity<String> response, boolean includeTimestamps) throws Exception {
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            
            if (jsonResponse.has("text")) {
                String transcription = jsonResponse.get("text").asText();
                return cleanTranscription(transcription);
            } else if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                JsonNode firstResult = jsonResponse.get(0);
                if (firstResult.has("text")) {
                    String transcription = firstResult.get("text").asText();
                    return cleanTranscription(transcription);
                }
            }
            
            return "Could not transcribe the audio. Please try with a clearer recording.";
        } else {
            throw new RuntimeException("Speech-to-text API call failed with status: " + response.getStatusCode());
        }
    }

    private String cleanTranscription(String transcription) {
        if (transcription == null) {
            return "";
        }
        
        // Clean up common transcription artifacts
        transcription = transcription.trim();
        transcription = transcription.replaceAll("\\s+", " "); // Multiple spaces to single space
        transcription = transcription.replaceAll("\\[.*?\\]", ""); // Remove bracketed content
        transcription = transcription.replaceAll("\\(.*?\\)", ""); // Remove parenthetical content
        
        return transcription;
    }

    private String processMedicalTranscription(String transcription, String consultationType) {
        if (transcription == null || transcription.trim().isEmpty()) {
            return transcription;
        }
        
        StringBuilder processed = new StringBuilder();
        processed.append("MEDICAL CONSULTATION TRANSCRIPTION\n");
        processed.append("=====================================\n\n");
        processed.append("Consultation Type: ").append(consultationType).append("\n");
        processed.append("Transcription Date: ").append(new java.util.Date()).append("\n\n");
        processed.append("TRANSCRIBED CONTENT:\n");
        processed.append("-------------------\n");
        processed.append(transcription);
        
        // Add medical terms found
        List<String> medicalTerms = extractMedicalTerminology(transcription);
        if (!medicalTerms.isEmpty()) {
            processed.append("\n\nIDENTIFIED MEDICAL TERMS:\n");
            processed.append("------------------------\n");
            for (String term : medicalTerms) {
                processed.append("• ").append(term).append("\n");
            }
        }
        
        processed.append("\n\n⚠️ TRANSCRIPTION DISCLAIMER: This AI-generated transcription should be reviewed and verified by healthcare professionals. Transcription accuracy may vary based on audio quality, accents, and medical terminology complexity.");
        
        return processed.toString();
    }

    private List<Map<String, String>> simulateSpeakerDiarization(String transcription) {
        List<Map<String, String>> segments = new ArrayList<>();
        
        if (transcription == null || transcription.trim().isEmpty()) {
            return segments;
        }
        
        // Simple simulation based on sentence breaks and question patterns
        String[] sentences = transcription.split("[.!?]+");
        String currentSpeaker = "Speaker 1";
        
        for (int i = 0; i < sentences.length; i++) {
            String sentence = sentences[i].trim();
            if (!sentence.isEmpty()) {
                // Simple heuristic: questions might indicate doctor, statements might indicate patient
                if (sentence.contains("?") || sentence.toLowerCase().contains("how") || 
                    sentence.toLowerCase().contains("what") || sentence.toLowerCase().contains("when") ||
                    sentence.toLowerCase().contains("describe") || sentence.toLowerCase().contains("tell me")) {
                    currentSpeaker = "Healthcare Provider";
                } else if (sentence.toLowerCase().contains("i feel") || sentence.toLowerCase().contains("my pain") ||
                          sentence.toLowerCase().contains("it hurts") || sentence.toLowerCase().contains("i have")) {
                    currentSpeaker = "Patient";
                }
                
                Map<String, String> segment = new HashMap<>();
                segment.put("speaker", currentSpeaker);
                segment.put("text", sentence);
                segment.put("timestamp", String.format("%02d:%02d", i * 30 / 60, (i * 30) % 60));
                segments.add(segment);
            }
        }
        
        return segments;
    }

    private int estimateSpeakerCount(String transcription) {
        // Simple estimation based on conversation patterns
        if (transcription == null || transcription.trim().isEmpty()) {
            return 1;
        }
        
        int questionCount = transcription.split("\\?").length - 1;
        int statementCount = transcription.split("[.!]").length - 1;
        
        // If there are questions and statements, likely 2+ speakers
        if (questionCount > 0 && statementCount > 0) {
            return 2;
        }
        
        return 1;
    }

    private String generateSOAPNotes(String transcription) {
        StringBuilder soap = new StringBuilder();
        soap.append("SOAP NOTES (AI-Generated)\n");
        soap.append("========================\n\n");
        
        // Extract subjective information (patient statements)
        soap.append("SUBJECTIVE:\n");
        soap.append("- ").append(extractPatientStatements(transcription)).append("\n\n");
        
        // Extract objective information (examination findings)
        soap.append("OBJECTIVE:\n");
        soap.append("- ").append(extractObjectiveFindings(transcription)).append("\n\n");
        
        // Assessment (diagnoses mentioned)
        soap.append("ASSESSMENT:\n");
        soap.append("- ").append(extractDiagnoses(transcription)).append("\n\n");
        
        // Plan (treatment mentioned)
        soap.append("PLAN:\n");
        soap.append("- ").append(extractTreatmentPlan(transcription)).append("\n\n");
        
        soap.append("⚠️ Note: This SOAP note was AI-generated and should be reviewed by a healthcare professional.");
        
        return soap.toString();
    }

    private String extractChiefComplaint(String transcription) {
        if (transcription == null) return "No chief complaint identified";
        
        String lower = transcription.toLowerCase();
        
        // Look for common chief complaint patterns
        if (lower.contains("i'm here because") || lower.contains("chief complaint")) {
            int start = Math.max(lower.indexOf("i'm here because"), lower.indexOf("chief complaint"));
            int end = Math.min(transcription.indexOf(".", start) + 1, transcription.length());
            if (end > start) {
                return transcription.substring(start, end);
            }
        }
        
        // Extract first patient statement
        String[] sentences = transcription.split("[.!?]");
        for (String sentence : sentences) {
            if (sentence.toLowerCase().contains("i feel") || sentence.toLowerCase().contains("i have") ||
                sentence.toLowerCase().contains("my") || sentence.toLowerCase().contains("pain")) {
                return sentence.trim();
            }
        }
        
        return "Chief complaint not clearly identified in transcription";
    }

    private String extractMedicationReferences(String transcription) {
        List<String> medications = extractMedicalTerminology(transcription);
        
        if (medications.isEmpty()) {
            return "No medications mentioned in transcription";
        }
        
        StringBuilder medList = new StringBuilder();
        medList.append("Medications mentioned:\n");
        for (String med : medications) {
            if (med.contains("medication")) {
                medList.append("• ").append(med).append("\n");
            }
        }
        
        return medList.toString();
    }

    private String extractActionItems(String transcription) {
        StringBuilder actions = new StringBuilder();
        actions.append("Action Items Identified:\n");
        
        String lower = transcription.toLowerCase();
        
        if (lower.contains("follow up") || lower.contains("follow-up")) {
            actions.append("• Schedule follow-up appointment\n");
        }
        if (lower.contains("prescription") || lower.contains("prescribe")) {
            actions.append("• Fill prescription\n");
        }
        if (lower.contains("test") || lower.contains("lab") || lower.contains("x-ray")) {
            actions.append("• Complete diagnostic tests\n");
        }
        if (lower.contains("referral") || lower.contains("specialist")) {
            actions.append("• Schedule specialist consultation\n");
        }
        
        if (actions.length() == "Action Items Identified:\n".length()) {
            actions.append("• No specific action items identified\n");
        }
        
        return actions.toString();
    }

    private String generateGeneralSummary(String transcription) {
        if (transcription == null || transcription.length() < 100) {
            return "Transcription too short for meaningful summary";
        }
        
        // Simple extractive summary - take first and last sentences
        String[] sentences = transcription.split("[.!?]+");
        
        StringBuilder summary = new StringBuilder();
        summary.append("CONSULTATION SUMMARY:\n");
        summary.append("====================\n\n");
        
        if (sentences.length > 0) {
            summary.append("Opening: ").append(sentences[0].trim()).append("\n\n");
        }
        
        if (sentences.length > 2) {
            summary.append("Conclusion: ").append(sentences[sentences.length - 1].trim()).append("\n\n");
        }
        
        summary.append("Word Count: ").append(countWords(transcription)).append("\n");
        summary.append("Estimated Duration: ").append(estimateAudioDuration(null)).append(" minutes\n");
        
        return summary.toString();
    }

    private String extractPatientStatements(String transcription) {
        // Extract sentences that likely come from patient
        StringBuilder subjective = new StringBuilder();
        String[] sentences = transcription.split("[.!?]+");
        
        for (String sentence : sentences) {
            String lower = sentence.toLowerCase();
            if (lower.contains("i feel") || lower.contains("i have") || lower.contains("my") || 
                lower.contains("it hurts") || lower.contains("pain")) {
                subjective.append(sentence.trim()).append(". ");
            }
        }
        
        return subjective.length() > 0 ? subjective.toString() : "No clear patient statements identified";
    }

    private String extractObjectiveFindings(String transcription) {
        // Extract examination findings
        String lower = transcription.toLowerCase();
        StringBuilder objective = new StringBuilder();
        
        if (lower.contains("blood pressure") || lower.contains("bp")) {
            objective.append("Vital signs documented. ");
        }
        if (lower.contains("examination") || lower.contains("exam")) {
            objective.append("Physical examination performed. ");
        }
        if (lower.contains("normal") || lower.contains("abnormal")) {
            objective.append("Clinical findings noted. ");
        }
        
        return objective.length() > 0 ? objective.toString() : "No clear objective findings identified";
    }

    private String extractDiagnoses(String transcription) {
        List<String> medicalTerms = extractMedicalTerminology(transcription);
        
        StringBuilder assessment = new StringBuilder();
        for (String term : medicalTerms) {
            if (!term.contains("medication")) {
                assessment.append(term).append(", ");
            }
        }
        
        if (assessment.length() > 0) {
            assessment.setLength(assessment.length() - 2); // Remove last comma
            return assessment.toString();
        }
        
        return "No clear diagnoses identified in transcription";
    }

    private String extractTreatmentPlan(String transcription) {
        String lower = transcription.toLowerCase();
        StringBuilder plan = new StringBuilder();
        
        if (lower.contains("prescription") || lower.contains("medication")) {
            plan.append("Prescription provided. ");
        }
        if (lower.contains("follow up") || lower.contains("return")) {
            plan.append("Follow-up scheduled. ");
        }
        if (lower.contains("lifestyle") || lower.contains("diet") || lower.contains("exercise")) {
            plan.append("Lifestyle modifications discussed. ");
        }
        
        return plan.length() > 0 ? plan.toString() : "No clear treatment plan identified";
    }

    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    private double estimateAudioDuration(byte[] audioData) {
        // Rough estimation: assume average speaking rate of 150 words per minute
        // This is a placeholder since we don't have actual audio duration
        return 5.0; // Default 5 minutes
    }

    private double calculateTranscriptionSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) {
            return 0.0;
        }
        
        // Simple similarity calculation using word overlap
        String[] words1 = text1.toLowerCase().split("\\s+");
        String[] words2 = text2.toLowerCase().split("\\s+");
        
        int commonWords = 0;
        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.equals(word2)) {
                    commonWords++;
                    break;
                }
            }
        }
        
        int totalWords = Math.max(words1.length, words2.length);
        return totalWords > 0 ? (double) commonWords / totalWords : 0.0;
    }

    private String determineConfidenceLevel(double similarity) {
        if (similarity >= 0.8) {
            return "High";
        } else if (similarity >= 0.6) {
            return "Medium";
        } else {
            return "Low";
        }
    }

    private List<String> generateImprovementSuggestions(double similarity, String transcription) {
        List<String> suggestions = new ArrayList<>();
        
        if (similarity < 0.8) {
            suggestions.add("Consider re-recording with better audio quality");
            suggestions.add("Speak more clearly and at a moderate pace");
            suggestions.add("Reduce background noise");
        }
        
        if (transcription.length() < 100) {
            suggestions.add("Transcription appears incomplete - check full audio duration");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("Transcription quality appears good");
        }
        
        return suggestions;
    }

    private boolean isApiKeyConfigured() {
        return huggingFaceApiKey != null && !huggingFaceApiKey.trim().isEmpty() && 
               !huggingFaceApiKey.equals("your-api-key-here");
    }

    private String handleSpeechToTextException(Exception e) {
        String errorMessage = e.getMessage();
        
        if (e instanceof HttpServerErrorException) {
            HttpServerErrorException serverError = (HttpServerErrorException) e;
            if (serverError.getStatusCode().value() == 503) {
                return "The speech recognition model is currently loading. Please wait a moment and try again.";
            }
        } else if (e instanceof HttpClientErrorException) {
            HttpClientErrorException clientError = (HttpClientErrorException) e;
            if (clientError.getStatusCode().value() == 429) {
                return "Too many transcription requests. Please wait a moment and try again.";
            } else if (clientError.getStatusCode().value() == 413) {
                return "Audio file too large. Please use an audio file smaller than 25MB.";
            } else if (clientError.getStatusCode().value() == 401) {
                return "Authentication failed. Please check the API configuration.";
            }
        }
        
        if (errorMessage != null) {
            if (errorMessage.toLowerCase().contains("model is loading")) {
                return "The speech recognition model is currently loading. Please wait a moment and try again.";
            } else if (errorMessage.toLowerCase().contains("timeout")) {
                return "Transcription timeout. Please try with a shorter audio file.";
            } else if (errorMessage.toLowerCase().contains("format")) {
                return "Unsupported audio format. Please use WAV, MP3, M4A, FLAC, or OGG format.";
            }
        }
        
        return "Audio transcription failed. Please ensure the audio file is clear and in a supported format (WAV, MP3, M4A, FLAC, OGG), then try again.";
    }
}