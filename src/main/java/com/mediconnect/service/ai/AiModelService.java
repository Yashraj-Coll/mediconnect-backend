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

@Service
public class AiModelService {
    
    private static final Logger logger = LoggerFactory.getLogger(AiModelService.class);
    
    @Value("${huggingface.api.key}")
    private String huggingFaceApiKey;
    
    @Value("${huggingface.endpoints.chat}")
    private String chatModelUrl;
    
    @Value("${huggingface.endpoints.ocr}")
    private String ocrModelUrl;
    
    @Value("${huggingface.medical.max-tokens:400}")
    private int medicalMaxTokens;
    
    @Value("${huggingface.medical.temperature:0.6}")
    private double medicalTemperature;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Diagnose from symptom description using Falcon-7B
     * Replaces OpenAI diagnosis functionality
     */
    public String diagnoseFromDescription(String symptoms) {
        try {
            HttpHeaders headers = createHeaders();

            String diagnosticPrompt = buildDiagnosticPrompt(symptoms);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", diagnosticPrompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", medicalMaxTokens,
                "temperature", medicalTemperature,
                "do_sample", true,
                "return_full_text", false,
                "top_p", 0.9,
                "repetition_penalty", 1.1
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(chatModelUrl, entity, String.class);
            
            return processDiagnosticResponse(response, diagnosticPrompt);
            
        } catch (Exception e) {
            logger.error("Error in diagnostic service: {}", e.getMessage(), e);
            return "Unable to analyze symptoms. Please consult with a healthcare professional for proper diagnosis and evaluation.";
        }
    }
    
    /**
     * Analyze medical images using Donut OCR model
     * Replaces OpenAI vision functionality
     */
    public String analyzeImage(byte[] imageBytes) {
        try {
            if (!isValidMedicalImage(imageBytes)) {
                return "Invalid image format or size. Please upload a clear medical image (JPEG/PNG, max 10MB).";
            }

            HttpHeaders headers = createHeaders();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", Map.of(
                "image", "data:image/jpeg;base64," + base64Image,
                "question", "Analyze this medical image and identify any visible conditions, abnormalities, structures, or notable features. Provide detailed observations and potential clinical significance."
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(ocrModelUrl, entity, String.class);
            
            return processImageAnalysisResponse(response);
            
        } catch (Exception e) {
            logger.error("Error in image analysis service: {}", e.getMessage(), e);
            return "Medical image analysis failed. Please ensure the image is clear and try again, or consult with a radiologist for professional interpretation.";
        }
    }

    /**
     * Advanced diagnostic analysis with patient context
     */
    public String advancedDiagnosis(String symptoms, String patientHistory, String vitalSigns) {
        String comprehensivePrompt = buildComprehensiveDiagnosticPrompt(symptoms, patientHistory, vitalSigns);
        
        try {
            HttpHeaders headers = createHeaders();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", comprehensivePrompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", 500,
                "temperature", 0.5,
                "do_sample", true,
                "return_full_text", false,
                "top_p", 0.85
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(chatModelUrl, entity, String.class);
            
            return processDiagnosticResponse(response, comprehensivePrompt);
            
        } catch (Exception e) {
            logger.error("Error in advanced diagnosis: {}", e.getMessage(), e);
            return "Unable to complete comprehensive analysis. Please consult with a healthcare professional for thorough evaluation.";
        }
    }

    /**
     * Generate differential diagnosis list
     */
    public List<String> generateDifferentialDiagnosis(String symptoms) {
        String diffDxPrompt = String.format(
            "Based on these symptoms: %s\n\n" +
            "Generate a differential diagnosis list with the most likely conditions ranked by probability. " +
            "Format each as: 'Condition Name - Brief explanation of why it fits'",
            symptoms
        );

        try {
            String response = callHuggingFaceModel(diffDxPrompt, chatModelUrl);
            return parseDifferentialDiagnosis(response);
        } catch (Exception e) {
            logger.error("Error generating differential diagnosis: {}", e.getMessage());
            List<String> errorList = new ArrayList<>();
            errorList.add("Unable to generate differential diagnosis. Please consult a healthcare professional.");
            return errorList;
        }
    }

    /**
     * Analyze lab results
     */
    public String analyzeLabResults(String labValues) {
        String labPrompt = String.format(
            "Analyze these laboratory results and provide clinical interpretation:\n\n%s\n\n" +
            "Please provide:\n" +
            "1. Abnormal values and their significance\n" +
            "2. Possible clinical correlations\n" +
            "3. Recommended follow-up tests\n" +
            "4. Clinical recommendations",
            labValues
        );

        try {
            return callHuggingFaceModel(labPrompt, chatModelUrl);
        } catch (Exception e) {
            logger.error("Error analyzing lab results: {}", e.getMessage());
            return "Unable to analyze laboratory results. Please have a healthcare professional review these findings.";
        }
    }

    /**
     * Generate clinical assessment summary
     */
    public String generateClinicalAssessment(String symptoms, String examination, String history) {
        String assessmentPrompt = String.format(
            "Generate a comprehensive clinical assessment based on:\n\n" +
            "Chief Complaint/Symptoms: %s\n\n" +
            "Physical Examination: %s\n\n" +
            "Medical History: %s\n\n" +
            "Provide a structured assessment including:\n" +
            "1. Clinical Impression\n" +
            "2. Differential Diagnosis\n" +
            "3. Recommended Investigations\n" +
            "4. Management Plan\n" +
            "5. Follow-up Recommendations",
            symptoms, examination, history
        );

        try {
            return callHuggingFaceModel(assessmentPrompt, chatModelUrl);
        } catch (Exception e) {
            logger.error("Error generating clinical assessment: {}", e.getMessage());
            return "Unable to generate clinical assessment. Please consult with a healthcare professional for comprehensive evaluation.";
        }
    }

    /**
     * Process health query with context
     */
    public String processHealthQuery(String query, String patientContext) {
        String enhancedQuery = String.format(
            "Health Query: %s\n\n" +
            "Patient Context: %s\n\n" +
            "Please provide a comprehensive health-related response that includes relevant medical information, potential considerations, and recommendations for next steps.",
            query,
            patientContext != null ? patientContext : "No additional context provided"
        );
        
        return diagnoseFromDescription(enhancedQuery);
    }

    /**
     * Validate medical image format and size
     */
    public boolean isValidMedicalImage(byte[] imageBytes) {
        try {
            if (imageBytes == null || imageBytes.length == 0) {
                return false;
            }
            
            // Check file size (max 10MB)
            if (imageBytes.length > 10_000_000) {
                return false;
            }
            
            // Basic image format validation
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            return base64.length() > 100;
            
        } catch (Exception e) {
            logger.error("Error validating image: {}", e.getMessage());
            return false;
        }
    }

    // Private helper methods

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(huggingFaceApiKey);
        headers.set("User-Agent", "MediConnect-AI/1.0");
        return headers;
    }

    private String buildDiagnosticPrompt(String symptoms) {
        return String.format(
            "You are an experienced diagnostic expert assistant. Analyze the following symptoms and provide a comprehensive medical assessment.\n\n" +
            "Symptoms: %s\n\n" +
            "Please provide:\n" +
            "1. POSSIBLE DIAGNOSES (ranked by likelihood)\n" +
            "2. RECOMMENDED TESTS for confirmation\n" +
            "3. IMMEDIATE CARE STEPS\n" +
            "4. WARNING SIGNS that require urgent medical attention\n" +
            "5. NEXT STEPS for the patient\n\n" +
            "Format your response clearly with headers and maintain a professional medical tone. " +
            "Always emphasize the importance of consulting with a healthcare professional for proper diagnosis and treatment.",
            symptoms
        );
    }

    private String buildComprehensiveDiagnosticPrompt(String symptoms, String history, String vitals) {
        return String.format(
            "Comprehensive Diagnostic Analysis:\n\n" +
            "PRESENTING SYMPTOMS: %s\n\n" +
            "MEDICAL HISTORY: %s\n\n" +
            "VITAL SIGNS: %s\n\n" +
            "Please provide a detailed clinical analysis including:\n" +
            "1. PRIMARY DIFFERENTIAL DIAGNOSIS\n" +
            "2. SUPPORTING EVIDENCE from history and vitals\n" +
            "3. ADDITIONAL WORKUP needed\n" +
            "4. RISK STRATIFICATION\n" +
            "5. MANAGEMENT RECOMMENDATIONS\n\n" +
            "Consider all provided information in your analysis.",
            symptoms, 
            history != null ? history : "No significant medical history provided",
            vitals != null ? vitals : "Vital signs not provided"
        );
    }

    private String processDiagnosticResponse(ResponseEntity<String> response, String originalPrompt) throws Exception {
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            
            if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                JsonNode firstResult = jsonResponse.get(0);
                String generatedText = firstResult.get("generated_text").asText();
                
                String diagnosis = generatedText.replace(originalPrompt, "").trim();
                
                if (diagnosis.isEmpty()) {
                    diagnosis = "Unable to provide diagnostic insights based on the symptoms provided. Please consult with a healthcare professional for proper evaluation.";
                }
                
                return addMedicalDisclaimer(diagnosis);
            } else {
                return "Diagnostic analysis could not be completed. Please consult with a healthcare professional.";
            }
        } else {
            throw new RuntimeException("Diagnostic API call failed with status: " + response.getStatusCode());
        }
    }

    private String processImageAnalysisResponse(ResponseEntity<String> response) throws Exception {
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            
            String analysisResult = "";
            
            if (jsonResponse.has("answer")) {
                analysisResult = jsonResponse.get("answer").asText();
            } else if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                JsonNode firstResult = jsonResponse.get(0);
                if (firstResult.has("answer")) {
                    analysisResult = firstResult.get("answer").asText();
                } else if (firstResult.has("generated_text")) {
                    analysisResult = firstResult.get("generated_text").asText();
                }
            }
            
            if (analysisResult.isEmpty()) {
                analysisResult = "Unable to analyze the medical image. The image may not be clear enough or may require specialized imaging analysis.";
            }
            
            return addImageAnalysisDisclaimer(analysisResult);
        } else {
            throw new RuntimeException("Image analysis API call failed with status: " + response.getStatusCode());
        }
    }

    private String callHuggingFaceModel(String prompt, String modelUrl) throws Exception {
        HttpHeaders headers = createHeaders();
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("inputs", prompt);
        requestBody.put("parameters", Map.of(
            "max_new_tokens", medicalMaxTokens,
            "temperature", medicalTemperature,
            "do_sample", true,
            "return_full_text", false
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(modelUrl, entity, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                return jsonResponse.get(0).get("generated_text").asText().replace(prompt, "").trim();
            }
        }
        
        throw new RuntimeException("API call failed");
    }

    private List<String> parseDifferentialDiagnosis(String response) {
        List<String> diagnoses = new ArrayList<>();
        
        if (response != null && !response.trim().isEmpty()) {
            String[] lines = response.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty() && (line.contains("-") || line.matches("\\d+\\."))) {
                    diagnoses.add(line);
                }
            }
        }
        
        if (diagnoses.isEmpty()) {
            diagnoses.add("Unable to generate differential diagnosis. Please consult a healthcare professional.");
        }
        
        return diagnoses;
    }

    private String addMedicalDisclaimer(String diagnosis) {
        if (!diagnosis.toLowerCase().contains("disclaimer") && 
            !diagnosis.toLowerCase().contains("consult") &&
            !diagnosis.toLowerCase().contains("healthcare professional")) {
            
            diagnosis += "\n\n⚠️ IMPORTANT MEDICAL DISCLAIMER: This AI analysis is not a substitute for professional medical advice, diagnosis, or treatment. Please consult with a qualified healthcare provider for proper medical evaluation and treatment decisions.";
        }
        return diagnosis;
    }

    private String addImageAnalysisDisclaimer(String analysis) {
        analysis += "\n\n⚠️ IMPORTANT RADIOLOGY DISCLAIMER: AI image analysis is preliminary and should not replace professional radiological interpretation. Please consult with a radiologist or healthcare provider for accurate medical imaging interpretation.";
        return analysis;
    }
}