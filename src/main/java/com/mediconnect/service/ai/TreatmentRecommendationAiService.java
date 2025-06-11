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

@Service
public class TreatmentRecommendationAiService {
    
    private static final Logger logger = LoggerFactory.getLogger(TreatmentRecommendationAiService.class);
    
    @Value("${huggingface.api.key}")
    private String huggingFaceApiKey;
    
    @Value("${huggingface.endpoints.treatment-recommendation}")
    private String treatmentRecommendationUrl;
    
    @Value("${huggingface.medical.max-tokens:450}")
    private int maxTokens;
    
    @Value("${huggingface.medical.temperature:0.4}")
    private double temperature;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Recommend treatment based on diagnosis and patient information
     * Uses BioGPT-Large for clinical treatment recommendations
     */
    public String recommendTreatment(String diagnosis, String patientInfo) {
        try {
            if (diagnosis == null || diagnosis.trim().isEmpty()) {
                return "No diagnosis provided for treatment recommendation.";
            }

            HttpHeaders headers = createHeaders();
            String treatmentPrompt = buildTreatmentRecommendationPrompt(diagnosis, patientInfo);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", treatmentPrompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", maxTokens,
                "temperature", temperature,
                "do_sample", true,
                "return_full_text", false,
                "top_p", 0.9,
                "repetition_penalty", 1.1
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(treatmentRecommendationUrl, entity, String.class);
            
            return processTreatmentRecommendationResponse(response, treatmentPrompt);
            
        } catch (Exception e) {
            logger.error("Error in treatment recommendation service: {}", e.getMessage(), e);
            return "Unable to generate treatment recommendations. Please consult with a healthcare specialist for appropriate treatment options.";
        }
    }

    /**
     * Generate comprehensive treatment plan
     */
    public Map<String, Object> generateComprehensiveTreatmentPlan(String diagnosis, String patientHistory, String currentMedications, String allergies) {
        Map<String, Object> treatmentPlan = new HashMap<>();
        
        try {
            String comprehensivePrompt = buildComprehensiveTreatmentPrompt(diagnosis, patientHistory, currentMedications, allergies);
            String recommendations = callTreatmentModel(comprehensivePrompt);
            
            treatmentPlan.put("diagnosis", diagnosis);
            treatmentPlan.put("recommendations", recommendations);
            treatmentPlan.put("patientHistory", patientHistory);
            treatmentPlan.put("currentMedications", currentMedications);
            treatmentPlan.put("allergies", allergies);
            treatmentPlan.put("timestamp", System.currentTimeMillis());
            treatmentPlan.put("status", "completed");
            
        } catch (Exception e) {
            logger.error("Error generating comprehensive treatment plan: {}", e.getMessage());
            treatmentPlan.put("error", e.getMessage());
            treatmentPlan.put("status", "failed");
        }
        
        return treatmentPlan;
    }

    /**
     * Generate medication recommendations
     */
    public String generateMedicationRecommendations(String condition, String patientProfile) {
        try {
            String medicationPrompt = String.format(
                "Generate evidence-based medication recommendations for:\n\n" +
                "CONDITION: %s\n\n" +
                "PATIENT PROFILE: %s\n\n" +
                "Provide:\n" +
                "1. FIRST-LINE MEDICATIONS\n" +
                "   - Drug name, dosage, frequency\n" +
                "   - Mechanism of action\n" +
                "   - Expected outcomes\n\n" +
                "2. ALTERNATIVE OPTIONS\n" +
                "   - Second-line treatments\n" +
                "   - When to consider alternatives\n\n" +
                "3. CONTRAINDICATIONS AND PRECAUTIONS\n" +
                "   - Patient-specific considerations\n" +
                "   - Drug interactions to monitor\n\n" +
                "4. MONITORING PARAMETERS\n" +
                "   - Lab tests required\n" +
                "   - Follow-up schedule\n\n" +
                "5. PATIENT EDUCATION\n" +
                "   - How to take medications\n" +
                "   - Side effects to watch for\n" +
                "   - When to contact healthcare provider",
                condition, patientProfile != null ? patientProfile : "Standard adult patient"
            );

            return callTreatmentModel(medicationPrompt);
            
        } catch (Exception e) {
            logger.error("Error generating medication recommendations: {}", e.getMessage());
            return "Unable to generate medication recommendations. Please consult with a physician or pharmacist.";
        }
    }

    /**
     * Generate lifestyle modification recommendations
     */
    public String generateLifestyleRecommendations(String condition, String currentLifestyle) {
        try {
            String lifestylePrompt = String.format(
                "Generate lifestyle modification recommendations for managing:\n\n" +
                "MEDICAL CONDITION: %s\n\n" +
                "CURRENT LIFESTYLE: %s\n\n" +
                "Provide specific recommendations for:\n\n" +
                "1. DIETARY MODIFICATIONS\n" +
                "   - Foods to include/avoid\n" +
                "   - Meal planning strategies\n" +
                "   - Nutritional goals\n\n" +
                "2. PHYSICAL ACTIVITY\n" +
                "   - Exercise type and intensity\n" +
                "   - Frequency and duration\n" +
                "   - Progressive goals\n\n" +
                "3. STRESS MANAGEMENT\n" +
                "   - Stress reduction techniques\n" +
                "   - Relaxation methods\n" +
                "   - Sleep hygiene\n\n" +
                "4. BEHAVIOR MODIFICATIONS\n" +
                "   - Habit changes\n" +
                "   - Goal setting strategies\n" +
                "   - Support systems\n\n" +
                "5. MONITORING AND TRACKING\n" +
                "   - Key metrics to monitor\n" +
                "   - Tools and apps\n" +
                "   - Progress evaluation",
                condition, currentLifestyle != null ? currentLifestyle : "Sedentary lifestyle"
            );

            return callTreatmentModel(lifestylePrompt);
            
        } catch (Exception e) {
            logger.error("Error generating lifestyle recommendations: {}", e.getMessage());
            return "Unable to generate lifestyle recommendations. Please consult with a healthcare provider or lifestyle counselor.";
        }
    }

    /**
     * Generate emergency treatment protocols
     */
    public String generateEmergencyProtocol(String emergencyCondition, String patientVitals) {
        try {
            String emergencyPrompt = String.format(
                "Generate emergency treatment protocol for:\n\n" +
                "EMERGENCY CONDITION: %s\n\n" +
                "PATIENT VITALS: %s\n\n" +
                "Provide IMMEDIATE EMERGENCY PROTOCOL:\n\n" +
                "1. IMMEDIATE ACTIONS (First 5 minutes)\n" +
                "   - Primary assessment (ABC)\n" +
                "   - Vital sign stabilization\n" +
                "   - Critical interventions\n\n" +
                "2. SECONDARY ASSESSMENT (5-15 minutes)\n" +
                "   - Detailed examination\n" +
                "   - Diagnostic tests\n" +
                "   - Risk stratification\n\n" +
                "3. TREATMENT INTERVENTIONS\n" +
                "   - Medications and dosages\n" +
                "   - Procedures required\n" +
                "   - Monitoring needs\n\n" +
                "4. DISPOSITION PLANNING\n" +
                "   - Admission criteria\n" +
                "   - Discharge criteria\n" +
                "   - Follow-up requirements\n\n" +
                "5. WARNING SIGNS\n" +
                "   - Clinical deterioration indicators\n" +
                "   - When to escalate care\n\n" +
                "⚠️ EMERGENCY DISCLAIMER: This is for educational purposes only. Always follow institutional protocols and contact emergency services immediately.",
                emergencyCondition, patientVitals != null ? patientVitals : "Vitals not provided"
            );

            String protocol = callTreatmentModel(emergencyPrompt);
            
            // Add emergency warning
            protocol = "🚨 EMERGENCY PROTOCOL - FOR HEALTHCARE PROFESSIONALS ONLY 🚨\n\n" + protocol;
            protocol += "\n\n🚨 CRITICAL: This AI-generated protocol must be validated by qualified emergency medicine physicians. Always follow your institution's emergency protocols.";
            
            return protocol;
            
        } catch (Exception e) {
            logger.error("Error generating emergency protocol: {}", e.getMessage());
            return "Unable to generate emergency protocol. Contact emergency services immediately and follow institutional emergency protocols.";
        }
    }

    /**
     * Generate treatment alternatives for medication intolerance
     */
    public String generateTreatmentAlternatives(String originalTreatment, String intoleranceReason, String patientFactors) {
        try {
            String alternativePrompt = String.format(
                "Generate alternative treatment options due to medication intolerance:\n\n" +
                "ORIGINAL TREATMENT: %s\n\n" +
                "INTOLERANCE/CONTRAINDICATION: %s\n\n" +
                "PATIENT FACTORS: %s\n\n" +
                "Provide alternative treatment strategies:\n\n" +
                "1. ALTERNATIVE MEDICATIONS\n" +
                "   - Different drug classes\n" +
                "   - Mechanism variations\n" +
                "   - Efficacy comparisons\n\n" +
                "2. NON-PHARMACOLOGICAL OPTIONS\n" +
                "   - Physical therapies\n" +
                "   - Psychological interventions\n" +
                "   - Lifestyle modifications\n\n" +
                "3. COMBINATION THERAPIES\n" +
                "   - Synergistic approaches\n" +
                "   - Reduced dosing strategies\n" +
                "   - Sequential treatments\n\n" +
                "4. RISK-BENEFIT ANALYSIS\n" +
                "   - Efficacy trade-offs\n" +
                "   - Safety considerations\n" +
                "   - Patient preferences\n\n" +
                "5. MONITORING ADJUSTMENTS\n" +
                "   - Modified follow-up protocols\n" +
                "   - Alternative endpoints\n" +
                "   - Safety parameters",
                originalTreatment, intoleranceReason, patientFactors != null ? patientFactors : "Standard patient"
            );

            return callTreatmentModel(alternativePrompt);
            
        } catch (Exception e) {
            logger.error("Error generating treatment alternatives: {}", e.getMessage());
            return "Unable to generate treatment alternatives. Please consult with a specialist for alternative treatment options.";
        }
    }

    /**
     * Generate follow-up care recommendations
     */
    public String generateFollowUpRecommendations(String treatment, String patientResponse, String timeline) {
        try {
            String followUpPrompt = String.format(
                "Generate follow-up care recommendations for:\n\n" +
                "TREATMENT: %s\n\n" +
                "PATIENT RESPONSE: %s\n\n" +
                "TIMELINE: %s\n\n" +
                "Provide structured follow-up plan:\n\n" +
                "1. SHORT-TERM FOLLOW-UP (1-4 weeks)\n" +
                "   - Assessment parameters\n" +
                "   - Adjustment criteria\n" +
                "   - Warning signs monitoring\n\n" +
                "2. MEDIUM-TERM FOLLOW-UP (1-6 months)\n" +
                "   - Effectiveness evaluation\n" +
                "   - Side effect monitoring\n" +
                "   - Treatment optimization\n\n" +
                "3. LONG-TERM FOLLOW-UP (6+ months)\n" +
                "   - Outcome assessment\n" +
                "   - Maintenance strategies\n" +
                "   - Preventive measures\n\n" +
                "4. MONITORING SCHEDULE\n" +
                "   - Laboratory tests\n" +
                "   - Imaging studies\n" +
                "   - Clinical assessments\n\n" +
                "5. PATIENT EDUCATION\n" +
                "   - Self-monitoring techniques\n" +
                "   - When to seek help\n" +
                "   - Lifestyle maintenance",
                treatment, patientResponse != null ? patientResponse : "Treatment response to be evaluated", 
                timeline != null ? timeline : "Standard follow-up timeline"
            );

            return callTreatmentModel(followUpPrompt);
            
        } catch (Exception e) {
            logger.error("Error generating follow-up recommendations: {}", e.getMessage());
            return "Unable to generate follow-up recommendations. Please consult with your healthcare provider for appropriate follow-up care.";
        }
    }

    /**
     * Generate pediatric treatment recommendations
     */
    public String generatePediatricTreatment(String condition, int ageMonths, double weightKg, String allergies) {
        try {
            String pediatricPrompt = String.format(
                "Generate pediatric treatment recommendations for:\n\n" +
                "CONDITION: %s\n" +
                "AGE: %d months\n" +
                "WEIGHT: %.1f kg\n" +
                "ALLERGIES: %s\n\n" +
                "Provide age-appropriate treatment plan:\n\n" +
                "1. MEDICATION DOSING\n" +
                "   - Weight-based calculations\n" +
                "   - Age-appropriate formulations\n" +
                "   - Safety considerations\n\n" +
                "2. NON-PHARMACOLOGICAL TREATMENTS\n" +
                "   - Age-appropriate interventions\n" +
                "   - Family involvement strategies\n" +
                "   - Developmental considerations\n\n" +
                "3. MONITORING REQUIREMENTS\n" +
                "   - Growth and development tracking\n" +
                "   - Side effect monitoring\n" +
                "   - Safety parameters\n\n" +
                "4. PARENT/CAREGIVER EDUCATION\n" +
                "   - Administration instructions\n" +
                "   - Warning signs to watch\n" +
                "   - Emergency protocols\n\n" +
                "5. SPECIAL CONSIDERATIONS\n" +
                "   - Developmental stage factors\n" +
                "   - School/daycare considerations\n" +
                "   - Long-term implications",
                condition, ageMonths, weightKg, allergies != null ? allergies : "No known allergies"
            );

            String pediatricTreatment = callTreatmentModel(pediatricPrompt);
            
            // Add pediatric disclaimer
            pediatricTreatment += "\n\n⚠️ PEDIATRIC DISCLAIMER: Pediatric treatment requires specialized expertise. Always consult with a pediatrician or pediatric specialist for treatment decisions in children.";
            
            return pediatricTreatment;
            
        } catch (Exception e) {
            logger.error("Error generating pediatric treatment: {}", e.getMessage());
            return "Unable to generate pediatric treatment recommendations. Please consult with a pediatrician for appropriate pediatric care.";
        }
    }

    /**
     * Generate treatment cost-effectiveness analysis
     */
    public Map<String, Object> analyzeTreatmentCostEffectiveness(List<String> treatmentOptions, String patientProfile) {
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            String costPrompt = String.format(
                "Analyze cost-effectiveness of treatment options:\n\n" +
                "TREATMENT OPTIONS: %s\n\n" +
                "PATIENT PROFILE: %s\n\n" +
                "Provide comparative analysis including:\n" +
                "1. Cost comparison\n" +
                "2. Efficacy comparison\n" +
                "3. Quality of life impact\n" +
                "4. Long-term outcomes\n" +
                "5. Patient preference factors",
                String.join(", ", treatmentOptions), patientProfile
            );

            String costAnalysis = callTreatmentModel(costPrompt);
            
            analysis.put("treatmentOptions", treatmentOptions);
            analysis.put("patientProfile", patientProfile);
            analysis.put("costEffectivenessAnalysis", costAnalysis);
            analysis.put("timestamp", System.currentTimeMillis());
            analysis.put("status", "completed");
            
        } catch (Exception e) {
            logger.error("Error analyzing treatment cost-effectiveness: {}", e.getMessage());
            analysis.put("error", e.getMessage());
            analysis.put("status", "failed");
        }
        
        return analysis;
    }

    // Private helper methods

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(huggingFaceApiKey);
        headers.set("User-Agent", "MediConnect-Treatment/1.0");
        return headers;
    }

    private String buildTreatmentRecommendationPrompt(String diagnosis, String patientInfo) {
        return String.format(
            "You are a medical treatment specialist with expertise in evidence-based medicine. Provide comprehensive treatment recommendations for the following case.\n\n" +
            "DIAGNOSIS: %s\n\n" +
            "PATIENT INFORMATION: %s\n\n" +
            "Please provide detailed treatment recommendations including:\n\n" +
            "1. FIRST-LINE TREATMENT OPTIONS\n" +
            "   - Medications with specific dosages\n" +
            "   - Non-pharmacological interventions\n" +
            "   - Expected timeline for improvement\n\n" +
            "2. ALTERNATIVE TREATMENT APPROACHES\n" +
            "   - Second-line options\n" +
            "   - Adjunctive therapies\n" +
            "   - Combination treatments\n\n" +
            "3. LIFESTYLE MODIFICATIONS\n" +
            "   - Dietary recommendations\n" +
            "   - Exercise prescriptions\n" +
            "   - Behavioral changes\n\n" +
            "4. MONITORING AND FOLLOW-UP\n" +
            "   - Response assessment criteria\n" +
            "   - Laboratory monitoring\n" +
            "   - Follow-up schedule\n\n" +
            "5. PATIENT EDUCATION\n" +
            "   - Condition explanation\n" +
            "   - Treatment adherence strategies\n" +
            "   - Warning signs to report\n\n" +
            "6. PROGNOSIS AND OUTCOMES\n" +
            "   - Expected treatment response\n" +
            "   - Long-term outlook\n" +
            "   - Quality of life considerations\n\n" +
            "Base recommendations on current clinical guidelines and evidence-based practices. Consider patient-specific factors and contraindications.",
            diagnosis, patientInfo != null ? patientInfo : "No additional patient information provided"
        );
    }

    private String buildComprehensiveTreatmentPrompt(String diagnosis, String history, String medications, String allergies) {
        return String.format(
            "Develop a comprehensive, personalized treatment plan:\n\n" +
            "PRIMARY DIAGNOSIS: %s\n\n" +
            "MEDICAL HISTORY: %s\n\n" +
            "CURRENT MEDICATIONS: %s\n\n" +
            "KNOWN ALLERGIES: %s\n\n" +
            "Create a detailed treatment plan addressing:\n\n" +
            "1. IMMEDIATE TREATMENT GOALS\n" +
            "2. LONG-TERM MANAGEMENT STRATEGY\n" +
            "3. MEDICATION OPTIMIZATION\n" +
            "4. DRUG INTERACTION CONSIDERATIONS\n" +
            "5. ALLERGY ACCOMMODATIONS\n" +
            "6. COMORBIDITY MANAGEMENT\n" +
            "7. QUALITY OF LIFE IMPROVEMENTS\n" +
            "8. PREVENTIVE MEASURES\n\n" +
            "Ensure all recommendations are coordinated and patient-centered.",
            diagnosis,
            history != null ? history : "No significant medical history",
            medications != null ? medications : "No current medications",
            allergies != null ? allergies : "No known allergies"
        );
    }

    private String processTreatmentRecommendationResponse(ResponseEntity<String> response, String originalPrompt) throws Exception {
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            
            if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                JsonNode firstResult = jsonResponse.get(0);
                String generatedText = firstResult.get("generated_text").asText();
                
                String recommendation = generatedText.replace(originalPrompt, "").trim();
                
                if (recommendation.isEmpty()) {
                    recommendation = "Unable to generate treatment recommendations for the provided diagnosis. Please consult with a healthcare specialist for appropriate treatment options.";
                }
                
                return addTreatmentRecommendationDisclaimer(recommendation);
            } else {
                return "Treatment recommendations could not be generated.";
            }
        } else {
            throw new RuntimeException("Treatment recommendation API call failed with status: " + response.getStatusCode());
        }
    }

    private String callTreatmentModel(String prompt) throws Exception {
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
        ResponseEntity<String> response = restTemplate.postForEntity(treatmentRecommendationUrl, entity, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                String result = jsonResponse.get(0).get("generated_text").asText().replace(prompt, "").trim();
                return addTreatmentRecommendationDisclaimer(result);
            }
        }
        
        throw new RuntimeException("Treatment recommendation API call failed");
    }

    private String addTreatmentRecommendationDisclaimer(String recommendation) {
        if (!recommendation.toLowerCase().contains("disclaimer") && 
            !recommendation.toLowerCase().contains("consult") &&
            !recommendation.toLowerCase().contains("healthcare")) {
            
            recommendation += "\n\n⚠️ TREATMENT DISCLAIMER: These AI-generated treatment recommendations are for informational purposes only and should not replace professional medical advice. Always consult with qualified healthcare providers for personalized treatment decisions. Treatment plans should be individualized based on comprehensive clinical assessment.";
        }
        return recommendation;
    }
}