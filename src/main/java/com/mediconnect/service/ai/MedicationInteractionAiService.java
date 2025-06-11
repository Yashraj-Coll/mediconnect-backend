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
import java.util.stream.Collectors;

@Service
public class MedicationInteractionAiService {
    
    private static final Logger logger = LoggerFactory.getLogger(MedicationInteractionAiService.class);
    
    @Value("${huggingface.api.key}")
    private String huggingFaceApiKey;
    
    @Value("${huggingface.endpoints.medication-interaction}")
    private String medicationInteractionUrl;
    
    @Value("${huggingface.medical.max-tokens:400}")
    private int maxTokens;
    
    @Value("${huggingface.medical.temperature:0.3}")
    private double temperature;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Check medication interactions for a list of medications
     * Uses BiomedNLP-PubMedBERT for drug interaction analysis
     */
    public String checkMedicationInteractions(List<String> medications) {
        try {
            if (medications == null || medications.isEmpty()) {
                return "No medications provided for interaction analysis.";
            }

            // Clean and validate medication list
            List<String> cleanMedications = medications.stream()
                .filter(med -> med != null && !med.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());

            if (cleanMedications.isEmpty()) {
                return "No valid medications provided for interaction analysis.";
            }

            HttpHeaders headers = createHeaders();
            String interactionPrompt = buildMedicationInteractionPrompt(cleanMedications);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", interactionPrompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", maxTokens,
                "temperature", temperature,
                "do_sample", true,
                "return_full_text", false,
                "top_p", 0.9,
                "repetition_penalty", 1.1
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(medicationInteractionUrl, entity, String.class);
            
            return processMedicationInteractionResponse(response, interactionPrompt);
            
        } catch (Exception e) {
            logger.error("Error in medication interaction service: {}", e.getMessage(), e);
            return "Unable to analyze medication interactions. Please consult with a pharmacist or healthcare provider for comprehensive drug interaction screening.";
        }
    }

    /**
     * Check drug-drug interactions between two specific medications
     */
    public String checkDrugDrugInteraction(String medication1, String medication2) {
        try {
            if (medication1 == null || medication2 == null || 
                medication1.trim().isEmpty() || medication2.trim().isEmpty()) {
                return "Two valid medication names are required for drug-drug interaction analysis.";
            }

            HttpHeaders headers = createHeaders();
            String interactionPrompt = buildDrugDrugInteractionPrompt(medication1.trim(), medication2.trim());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", interactionPrompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", 300,
                "temperature", 0.2,
                "do_sample", true,
                "return_full_text", false,
                "top_p", 0.85
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(medicationInteractionUrl, entity, String.class);
            
            return processMedicationInteractionResponse(response, interactionPrompt);
            
        } catch (Exception e) {
            logger.error("Error in drug-drug interaction analysis: {}", e.getMessage());
            return "Unable to analyze drug-drug interaction. Please consult with a pharmacist for detailed interaction information.";
        }
    }

    /**
     * Check drug-food interactions
     */
    public String checkDrugFoodInteractions(List<String> medications) {
        try {
            if (medications == null || medications.isEmpty()) {
                return "No medications provided for drug-food interaction analysis.";
            }

            String medicationList = String.join(", ", medications);
            String foodInteractionPrompt = String.format(
                "Analyze potential drug-food interactions for the following medications:\n\n" +
                "Medications: %s\n\n" +
                "Provide:\n" +
                "1. FOODS TO AVOID for each medication\n" +
                "2. FOODS TO TAKE WITH for optimal absorption\n" +
                "3. TIMING RECOMMENDATIONS for meals\n" +
                "4. ALCOHOL INTERACTIONS\n" +
                "5. DIETARY SUPPLEMENTS to avoid\n\n" +
                "Include severity levels and clinical significance for each interaction.",
                medicationList
            );

            return callMedicationInteractionModel(foodInteractionPrompt);
            
        } catch (Exception e) {
            logger.error("Error in drug-food interaction analysis: {}", e.getMessage());
            return "Unable to analyze drug-food interactions. Please consult with a pharmacist or dietitian.";
        }
    }

    /**
     * Analyze medication contraindications based on patient conditions
     */
    public String analyzeContraindications(List<String> medications, List<String> medicalConditions) {
        try {
            if (medications == null || medications.isEmpty()) {
                return "No medications provided for contraindication analysis.";
            }

            String medicationList = String.join(", ", medications);
            String conditionList = medicalConditions != null ? String.join(", ", medicalConditions) : "No conditions specified";
            
            String contraindicationPrompt = String.format(
                "Analyze contraindications and precautions for the following medications in a patient with these conditions:\n\n" +
                "Medications: %s\n\n" +
                "Medical Conditions: %s\n\n" +
                "Provide:\n" +
                "1. ABSOLUTE CONTRAINDICATIONS\n" +
                "2. RELATIVE CONTRAINDICATIONS\n" +
                "3. DOSE ADJUSTMENTS needed\n" +
                "4. MONITORING REQUIREMENTS\n" +
                "5. ALTERNATIVE MEDICATIONS if contraindicated\n\n" +
                "Include severity levels and clinical recommendations.",
                medicationList, conditionList
            );

            return callMedicationInteractionModel(contraindicationPrompt);
            
        } catch (Exception e) {
            logger.error("Error in contraindication analysis: {}", e.getMessage());
            return "Unable to analyze medication contraindications. Please consult with a healthcare provider.";
        }
    }

    /**
     * Generate medication safety profile
     */
    public Map<String, Object> generateMedicationSafetyProfile(String medication) {
        Map<String, Object> safetyProfile = new HashMap<>();
        
        try {
            if (medication == null || medication.trim().isEmpty()) {
                safetyProfile.put("error", "No medication provided for safety analysis");
                return safetyProfile;
            }

            String safetyPrompt = String.format(
                "Generate a comprehensive safety profile for: %s\n\n" +
                "Include:\n" +
                "1. Common side effects\n" +
                "2. Serious adverse reactions\n" +
                "3. Drug interactions\n" +
                "4. Contraindications\n" +
                "5. Special populations (pregnancy, pediatric, elderly)\n" +
                "6. Monitoring requirements\n" +
                "7. Overdose information",
                medication.trim()
            );

            String safetyInfo = callMedicationInteractionModel(safetyPrompt);
            
            safetyProfile.put("medication", medication.trim());
            safetyProfile.put("safetyInformation", safetyInfo);
            safetyProfile.put("timestamp", System.currentTimeMillis());
            safetyProfile.put("status", "completed");
            
        } catch (Exception e) {
            logger.error("Error generating medication safety profile: {}", e.getMessage());
            safetyProfile.put("error", e.getMessage());
            safetyProfile.put("status", "failed");
        }
        
        return safetyProfile;
    }

    /**
     * Check medication allergies and cross-sensitivities
     */
    public String checkMedicationAllergies(List<String> medications, List<String> knownAllergies) {
        try {
            String medicationList = medications != null ? String.join(", ", medications) : "No medications specified";
            String allergyList = knownAllergies != null ? String.join(", ", knownAllergies) : "No known allergies";
            
            String allergyPrompt = String.format(
                "Analyze potential medication allergies and cross-sensitivities:\n\n" +
                "Medications to assess: %s\n\n" +
                "Known allergies: %s\n\n" +
                "Provide:\n" +
                "1. POTENTIAL ALLERGIC REACTIONS\n" +
                "2. CROSS-SENSITIVITY risks\n" +
                "3. ALTERNATIVE MEDICATIONS if allergic\n" +
                "4. EMERGENCY MEASURES for severe reactions\n" +
                "5. ALLERGY TESTING recommendations\n\n" +
                "Include severity assessments and precautionary measures.",
                medicationList, allergyList
            );

            return callMedicationInteractionModel(allergyPrompt);
            
        } catch (Exception e) {
            logger.error("Error checking medication allergies: {}", e.getMessage());
            return "Unable to analyze medication allergies. Please consult with an allergist or pharmacist.";
        }
    }

    /**
     * Analyze medication adherence factors
     */
    public String analyzeMedicationAdherence(List<String> medications, Map<String, Object> patientFactors) {
        try {
            String medicationList = medications != null ? String.join(", ", medications) : "No medications specified";
            String factors = formatPatientFactors(patientFactors);
            
            String adherencePrompt = String.format(
                "Analyze medication adherence challenges and solutions:\n\n" +
                "Medications: %s\n\n" +
                "Patient Factors: %s\n\n" +
                "Provide:\n" +
                "1. ADHERENCE CHALLENGES specific to these medications\n" +
                "2. STRATEGIES to improve adherence\n" +
                "3. SIMPLIFICATION opportunities\n" +
                "4. PATIENT EDUCATION needs\n" +
                "5. MONITORING methods\n\n" +
                "Include practical, actionable recommendations.",
                medicationList, factors
            );

            return callMedicationInteractionModel(adherencePrompt);
            
        } catch (Exception e) {
            logger.error("Error analyzing medication adherence: {}", e.getMessage());
            return "Unable to analyze medication adherence. Please consult with a pharmacist for adherence strategies.";
        }
    }

    /**
     * Generate comprehensive drug interaction report
     */
    public Map<String, Object> generateInteractionReport(List<String> medications) {
        Map<String, Object> report = new HashMap<>();
        
        try {
            report.put("medications", medications);
            report.put("timestamp", System.currentTimeMillis());
            
            // Basic interaction check
            String basicInteractions = checkMedicationInteractions(medications);
            report.put("basicInteractions", basicInteractions);
            
            // Drug-food interactions
            String foodInteractions = checkDrugFoodInteractions(medications);
            report.put("foodInteractions", foodInteractions);
            
            // Generate summary
            String summary = generateInteractionSummary(medications.size(), basicInteractions, foodInteractions);
            report.put("summary", summary);
            
            report.put("status", "completed");
            
        } catch (Exception e) {
            logger.error("Error generating interaction report: {}", e.getMessage());
            report.put("error", e.getMessage());
            report.put("status", "failed");
        }
        
        return report;
    }

    // Private helper methods

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(huggingFaceApiKey);
        headers.set("User-Agent", "MediConnect-DrugInteraction/1.0");
        return headers;
    }

    private String buildMedicationInteractionPrompt(List<String> medications) {
        String medicationList = String.join(", ", medications);
        
        return String.format(
            "You are a pharmacology expert specializing in drug interactions. Analyze the following medications for potential interactions, contraindications, and side effects.\n\n" +
            "MEDICATIONS TO ANALYZE:\n%s\n\n" +
            "Please provide a comprehensive analysis including:\n\n" +
            "1. DRUG-DRUG INTERACTIONS\n" +
            "   - Major interactions (severe clinical significance)\n" +
            "   - Moderate interactions (monitor closely)\n" +
            "   - Minor interactions (minimal clinical significance)\n\n" +
            "2. MECHANISM OF INTERACTIONS\n" +
            "   - How the drugs interact\n" +
            "   - Clinical consequences\n\n" +
            "3. SEVERITY LEVELS\n" +
            "   - High Risk (contraindicated)\n" +
            "   - Moderate Risk (use with caution)\n" +
            "   - Low Risk (monitor)\n\n" +
            "4. MANAGEMENT RECOMMENDATIONS\n" +
            "   - Dose adjustments\n" +
            "   - Timing modifications\n" +
            "   - Alternative medications\n" +
            "   - Monitoring parameters\n\n" +
            "5. CLINICAL SIGNIFICANCE\n" +
            "   - Patient safety implications\n" +
            "   - Therapeutic effectiveness impact\n\n" +
            "Format your response clearly with headers and provide evidence-based recommendations.",
            medicationList
        );
    }

    private String buildDrugDrugInteractionPrompt(String med1, String med2) {
        return String.format(
            "Analyze the specific drug-drug interaction between:\n\n" +
            "MEDICATION 1: %s\n" +
            "MEDICATION 2: %s\n\n" +
            "Provide detailed analysis including:\n\n" +
            "1. INTERACTION CLASSIFICATION\n" +
            "2. MECHANISM OF INTERACTION\n" +
            "3. CLINICAL EFFECTS\n" +
            "4. SEVERITY ASSESSMENT\n" +
            "5. MANAGEMENT STRATEGIES\n" +
            "6. ALTERNATIVE OPTIONS\n\n" +
            "Include specific clinical recommendations and monitoring requirements.",
            med1, med2
        );
    }

    private String processMedicationInteractionResponse(ResponseEntity<String> response, String originalPrompt) throws Exception {
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            
            if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                JsonNode firstResult = jsonResponse.get(0);
                String generatedText = firstResult.get("generated_text").asText();
                
                String interactions = generatedText.replace(originalPrompt, "").trim();
                
                if (interactions.isEmpty()) {
                    interactions = "No significant interactions detected between the provided medications. However, always consult with a pharmacist for comprehensive drug interaction screening.";
                }
                
                return addMedicationInteractionDisclaimer(interactions);
            } else {
                return "Medication interaction analysis could not be completed.";
            }
        } else {
            throw new RuntimeException("Medication interaction API call failed with status: " + response.getStatusCode());
        }
    }

    private String callMedicationInteractionModel(String prompt) throws Exception {
        HttpHeaders headers = createHeaders();
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("inputs", prompt);
        requestBody.put("parameters", Map.of(
            "max_new_tokens", maxTokens,
            "temperature", temperature,
            "do_sample", true,
            "return_full_text", false
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(medicationInteractionUrl, entity, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                String result = jsonResponse.get(0).get("generated_text").asText().replace(prompt, "").trim();
                return addMedicationInteractionDisclaimer(result);
            }
        }
        
        throw new RuntimeException("Medication interaction API call failed");
    }

    private String formatPatientFactors(Map<String, Object> factors) {
        if (factors == null || factors.isEmpty()) {
            return "No patient factors provided";
        }
        
        StringBuilder formatted = new StringBuilder();
        for (Map.Entry<String, Object> entry : factors.entrySet()) {
            formatted.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        return formatted.toString();
    }

    private String generateInteractionSummary(int medicationCount, String basicInteractions, String foodInteractions) {
        StringBuilder summary = new StringBuilder();
        summary.append("MEDICATION INTERACTION SUMMARY\n");
        summary.append("==============================\n\n");
        summary.append("Number of medications analyzed: ").append(medicationCount).append("\n\n");
        
        // Analyze interaction severity from responses
        if (basicInteractions.toLowerCase().contains("major") || basicInteractions.toLowerCase().contains("severe")) {
            summary.append("⚠️ HIGH PRIORITY: Major drug interactions detected\n");
        } else if (basicInteractions.toLowerCase().contains("moderate")) {
            summary.append("⚠️ MODERATE PRIORITY: Moderate drug interactions detected\n");
        } else {
            summary.append("✅ LOW PRIORITY: No major drug interactions detected\n");
        }
        
        summary.append("\nRECOMMENDATION: Review detailed interaction analysis and consult with pharmacist for comprehensive medication review.\n");
        
        return summary.toString();
    }

    private String addMedicationInteractionDisclaimer(String interactions) {
        if (!interactions.toLowerCase().contains("disclaimer") && 
            !interactions.toLowerCase().contains("consult") &&
            !interactions.toLowerCase().contains("pharmacist")) {
            
            interactions += "\n\n⚠️ MEDICATION INTERACTION DISCLAIMER: This AI analysis is for informational purposes only and should not replace professional pharmaceutical consultation. Always consult with a licensed pharmacist or healthcare provider for comprehensive drug interaction screening and medication management.";
        }
        return interactions;
    }
}