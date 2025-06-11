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

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class MedicalCodingService {
    
    private static final Logger logger = LoggerFactory.getLogger(MedicalCodingService.class);
    
    @Value("${huggingface.api.key}")
    private String huggingFaceApiKey;
    
    @Value("${huggingface.endpoints.medical-coding}")
    private String medicalCodingUrl;
    
    @Value("${huggingface.medical.max-tokens:400}")
    private int maxTokens;
    
    @Value("${huggingface.medical.temperature:0.3}")
    private double temperature;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // ICD-10 code validation pattern
    private static final Pattern ICD10_PATTERN = Pattern.compile("^[A-Z]\\d{2}(\\.\\d{1,4})?$");
    private static final Pattern CPT_PATTERN = Pattern.compile("^\\d{5}$");
    
    /**
     * Generate ICD-10 medical coding from clinical text
     * Uses BiomedNLP-PubMedBERT model
     */
    public String generateMedicalCoding(String clinicalText) {
        try {
            if (clinicalText == null || clinicalText.trim().isEmpty()) {
                return "No clinical text provided for medical coding analysis.";
            }

            HttpHeaders headers = createHeaders();
            String codingPrompt = buildICD10CodingPrompt(clinicalText);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", codingPrompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", maxTokens,
                "temperature", temperature,
                "do_sample", true,
                "return_full_text", false,
                "top_p", 0.9,
                "repetition_penalty", 1.1
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(medicalCodingUrl, entity, String.class);
            
            return processMedicalCodingResponse(response, codingPrompt);
            
        } catch (Exception e) {
            logger.error("Error in medical coding service: {}", e.getMessage(), e);
            return "Unable to generate medical codes. Please try again or consult with a medical coding specialist.";
        }
    }

    /**
     * Generate CPT codes for procedures
     */
    public String generateCPTCoding(String procedureText) {
        try {
            if (procedureText == null || procedureText.trim().isEmpty()) {
                return "No procedure text provided for CPT coding analysis.";
            }

            HttpHeaders headers = createHeaders();
            String cptPrompt = buildCPTCodingPrompt(procedureText);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", cptPrompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", 250,
                "temperature", 0.2,
                "do_sample", true,
                "return_full_text", false,
                "top_p", 0.85
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(medicalCodingUrl, entity, String.class);
            
            return processCPTCodingResponse(response, cptPrompt);
            
        } catch (Exception e) {
            logger.error("Error in CPT coding service: {}", e.getMessage(), e);
            return "Unable to generate CPT codes. Please consult with a medical coding specialist.";
        }
    }

    /**
     * Generate comprehensive medical coding (ICD-10 + CPT)
     */
    public Map<String, Object> generateComprehensiveCoding(String clinicalText, String procedureText) {
        Map<String, Object> codingResult = new HashMap<>();
        
        try {
            // Generate ICD-10 codes
            String icd10Codes = generateMedicalCoding(clinicalText);
            codingResult.put("icd10Codes", icd10Codes);
            codingResult.put("icd10Status", "success");
            
            // Generate CPT codes if procedure text is provided
            if (procedureText != null && !procedureText.trim().isEmpty()) {
                String cptCodes = generateCPTCoding(procedureText);
                codingResult.put("cptCodes", cptCodes);
                codingResult.put("cptStatus", "success");
            } else {
                codingResult.put("cptCodes", "No procedure text provided");
                codingResult.put("cptStatus", "skipped");
            }
            
            codingResult.put("timestamp", System.currentTimeMillis());
            codingResult.put("overallStatus", "completed");
            
        } catch (Exception e) {
            logger.error("Error in comprehensive coding: {}", e.getMessage());
            codingResult.put("error", e.getMessage());
            codingResult.put("overallStatus", "failed");
        }
        
        return codingResult;
    }

    /**
     * Validate ICD-10 code format
     */
    public boolean validateICD10Code(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        
        String cleanCode = code.trim().toUpperCase();
        return ICD10_PATTERN.matcher(cleanCode).matches();
    }

    /**
     * Validate CPT code format
     */
    public boolean validateCPTCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        
        String cleanCode = code.trim().replaceAll("\\D", ""); // Remove non-digits
        return CPT_PATTERN.matcher(cleanCode).matches();
    }

    /**
     * Extract and validate codes from generated text
     */
    public List<String> extractValidICD10Codes(String text) {
        List<String> validCodes = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            return validCodes;
        }
        
        // Pattern to find ICD-10 codes in text
        Pattern extractPattern = Pattern.compile("\\b[A-Z]\\d{2}(?:\\.\\d{1,4})?\\b");
        Matcher matcher = extractPattern.matcher(text.toUpperCase());
        
        while (matcher.find()) {
            String code = matcher.group();
            if (validateICD10Code(code)) {
                validCodes.add(code);
            }
        }
        
        return validCodes;
    }

    /**
     * Extract and validate CPT codes from generated text
     */
    public List<String> extractValidCPTCodes(String text) {
        List<String> validCodes = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            return validCodes;
        }
        
        // Pattern to find CPT codes in text
        Pattern extractPattern = Pattern.compile("\\b\\d{5}\\b");
        Matcher matcher = extractPattern.matcher(text);
        
        while (matcher.find()) {
            String code = matcher.group();
            if (validateCPTCode(code)) {
                validCodes.add(code);
            }
        }
        
        return validCodes;
    }

    /**
     * Get coding statistics for clinical text
     */
    public Map<String, Object> getCodingStatistics(String clinicalText) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            String codingResult = generateMedicalCoding(clinicalText);
            List<String> extractedCodes = extractValidICD10Codes(codingResult);
            
            stats.put("totalCodesGenerated", extractedCodes.size());
            stats.put("extractedCodes", extractedCodes);
            stats.put("clinicalTextLength", clinicalText != null ? clinicalText.length() : 0);
            stats.put("codingComplexity", determineCodingComplexity(clinicalText));
            stats.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            logger.error("Error generating coding statistics: {}", e.getMessage());
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    // Private helper methods

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(huggingFaceApiKey);
        headers.set("User-Agent", "MediConnect-Coding/1.0");
        return headers;
    }

    private String buildICD10CodingPrompt(String clinicalText) {
        return String.format(
            "You are a certified medical coding expert specializing in ICD-10-CM coding. " +
            "Analyze the following clinical documentation and provide appropriate ICD-10-CM codes with descriptions.\n\n" +
            "INSTRUCTIONS:\n" +
            "- Provide the most specific ICD-10-CM codes possible\n" +
            "- Include both primary and secondary diagnoses if applicable\n" +
            "- Format each code as: 'CODE - Description'\n" +
            "- List codes in order of clinical priority\n" +
            "- Ensure codes are current and valid\n\n" +
            "CLINICAL DOCUMENTATION:\n%s\n\n" +
            "ICD-10-CM CODES:",
            clinicalText
        );
    }

    private String buildCPTCodingPrompt(String procedureText) {
        return String.format(
            "You are a medical coding expert specializing in CPT (Current Procedural Terminology) coding. " +
            "Analyze the following procedure documentation and provide appropriate CPT codes with descriptions.\n\n" +
            "INSTRUCTIONS:\n" +
            "- Provide specific CPT codes for procedures performed\n" +
            "- Include modifiers if applicable\n" +
            "- Format each code as: 'CPT CODE - Description'\n" +
            "- Consider bundling rules and separate procedures\n\n" +
            "PROCEDURE DOCUMENTATION:\n%s\n\n" +
            "CPT CODES:",
            procedureText
        );
    }

    private String processMedicalCodingResponse(ResponseEntity<String> response, String originalPrompt) throws Exception {
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            
            if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                JsonNode firstResult = jsonResponse.get(0);
                String generatedText = firstResult.get("generated_text").asText();
                
                String codeResult = generatedText.replace(originalPrompt, "").trim();
                
                if (codeResult.isEmpty()) {
                    codeResult = "Unable to generate medical codes for the provided clinical text. Please review the documentation or consult with a medical coding specialist.";
                }
                
                return formatMedicalCodes(codeResult);
            } else {
                return "No medical codes could be generated from the provided clinical text.";
            }
        } else {
            throw new RuntimeException("Medical coding API call failed with status: " + response.getStatusCode());
        }
    }

    private String processCPTCodingResponse(ResponseEntity<String> response, String originalPrompt) throws Exception {
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            
            if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                JsonNode firstResult = jsonResponse.get(0);
                String generatedText = firstResult.get("generated_text").asText();
                
                String cptResult = generatedText.replace(originalPrompt, "").trim();
                
                if (cptResult.isEmpty()) {
                    return "Unable to generate CPT codes for the provided procedure text.";
                }
                
                return formatCPTCodes(cptResult);
            } else {
                return "No CPT codes could be generated.";
            }
        } else {
            throw new RuntimeException("CPT coding API call failed with status: " + response.getStatusCode());
        }
    }

    private String formatMedicalCodes(String rawCodes) {
        if (rawCodes == null || rawCodes.trim().isEmpty()) {
            return "No codes generated";
        }
        
        StringBuilder formatted = new StringBuilder();
        formatted.append("ICD-10-CM CODES:\n");
        formatted.append("==================\n");
        
        String[] lines = rawCodes.split("\n");
        int codeCount = 0;
        
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && (line.matches(".*[A-Z]\\d{2}.*") || line.contains("-"))) {
                formatted.append("• ").append(line).append("\n");
                codeCount++;
            }
        }
        
        if (codeCount == 0) {
            formatted.append("• No specific ICD-10 codes could be extracted\n");
        }
        
        formatted.append("\n⚠️ CODING DISCLAIMER: These codes are AI-generated suggestions. Please verify with current ICD-10-CM guidelines and consult a certified medical coder for final code assignment.");
        
        return formatted.toString();
    }

    private String formatCPTCodes(String rawCodes) {
        if (rawCodes == null || rawCodes.trim().isEmpty()) {
            return "No CPT codes generated";
        }
        
        StringBuilder formatted = new StringBuilder();
        formatted.append("CPT CODES:\n");
        formatted.append("==========\n");
        
        String[] lines = rawCodes.split("\n");
        int codeCount = 0;
        
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && (line.matches(".*\\d{5}.*") || line.contains("CPT") || line.contains("-"))) {
                formatted.append("• ").append(line).append("\n");
                codeCount++;
            }
        }
        
        if (codeCount == 0) {
            formatted.append("• No specific CPT codes could be extracted\n");
        }
        
        formatted.append("\n⚠️ CODING DISCLAIMER: These codes are AI-generated suggestions. Please verify with current CPT guidelines and consult a certified medical coder for final code assignment.");
        
        return formatted.toString();
    }

    private String determineCodingComplexity(String clinicalText) {
        if (clinicalText == null) {
            return "unknown";
        }
        
        int length = clinicalText.length();
        int commaCount = clinicalText.split(",").length - 1;
        
        if (length < 100 && commaCount < 3) {
            return "simple";
        } else if (length < 500 && commaCount < 10) {
            return "moderate";
        } else {
            return "complex";
        }
    }
}