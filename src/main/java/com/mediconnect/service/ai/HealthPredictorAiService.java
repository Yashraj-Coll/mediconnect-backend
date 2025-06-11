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
public class HealthPredictorAiService {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthPredictorAiService.class);
    
    @Value("${huggingface.api.key}")
    private String huggingFaceApiKey;
    
    @Value("${huggingface.endpoints.health-prediction}")
    private String healthPredictionUrl;
    
    @Value("${huggingface.medical.max-tokens:400}")
    private int maxTokens;
    
    @Value("${huggingface.medical.temperature:0.4}")
    private double temperature;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Predict health risks based on patient data
     * Uses DialoGPT-medium for health risk assessment
     */
    public String predictHealthRisks(String patientData) {
        try {
            if (patientData == null || patientData.trim().isEmpty()) {
                return "No patient data provided for health risk assessment.";
            }

            HttpHeaders headers = createHeaders();
            String predictionPrompt = buildHealthRiskPrompt(patientData);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", predictionPrompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", maxTokens,
                "temperature", temperature,
                "do_sample", true,
                "return_full_text", false,
                "top_p", 0.9,
                "repetition_penalty", 1.1
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(healthPredictionUrl, entity, String.class);
            
            return processHealthPredictionResponse(response, predictionPrompt);
            
        } catch (Exception e) {
            logger.error("Error in health prediction service: {}", e.getMessage(), e);
            return "Unable to predict health risks. Please consult with a healthcare professional for proper risk assessment.";
        }
    }

    /**
     * Predict disease progression based on current condition
     */
    public String predictDiseaseProgression(String currentCondition, String patientHistory) {
        try {
            HttpHeaders headers = createHeaders();
            String progressionPrompt = buildProgressionPrompt(currentCondition, patientHistory);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", progressionPrompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", 450,
                "temperature", 0.3,
                "do_sample", true,
                "return_full_text", false,
                "top_p", 0.85
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(healthPredictionUrl, entity, String.class);
            
            return processHealthPredictionResponse(response, progressionPrompt);
            
        } catch (Exception e) {
            logger.error("Error in disease progression prediction: {}", e.getMessage());
            return "Unable to predict disease progression. Please consult with a healthcare specialist for detailed prognosis.";
        }
    }

    /**
     * Generate personalized health recommendations
     */
    public String generateHealthRecommendations(String patientProfile, String healthGoals) {
        try {
            HttpHeaders headers = createHeaders();
            String recommendationPrompt = buildRecommendationPrompt(patientProfile, healthGoals);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", recommendationPrompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", 500,
                "temperature", 0.5,
                "do_sample", true,
                "return_full_text", false,
                "top_p", 0.9
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(healthPredictionUrl, entity, String.class);
            
            return processHealthPredictionResponse(response, recommendationPrompt);
            
        } catch (Exception e) {
            logger.error("Error generating health recommendations: {}", e.getMessage());
            return "Unable to generate personalized health recommendations. Please consult with a healthcare professional.";
        }
    }

    /**
     * Assess cardiovascular risk factors
     */
    public Map<String, Object> assessCardiovascularRisk(Map<String, Object> riskFactors) {
        Map<String, Object> assessment = new HashMap<>();
        
        try {
            String riskData = formatRiskFactors(riskFactors);
            String cvRiskPrompt = buildCardiovascularRiskPrompt(riskData);
            
            String prediction = callHealthPredictionModel(cvRiskPrompt);
            
            assessment.put("riskAssessment", prediction);
            assessment.put("riskFactors", riskFactors);
            assessment.put("assessmentType", "cardiovascular");
            assessment.put("timestamp", System.currentTimeMillis());
            assessment.put("status", "completed");
            
        } catch (Exception e) {
            logger.error("Error in cardiovascular risk assessment: {}", e.getMessage());
            assessment.put("error", e.getMessage());
            assessment.put("status", "failed");
        }
        
        return assessment;
    }

    /**
     * Predict medication adherence based on patient factors
     */
    public String predictMedicationAdherence(String patientFactors, String medicationRegimen) {
        try {
            String adherencePrompt = String.format(
                "Analyze medication adherence risk based on the following factors:\n\n" +
                "Patient Factors: %s\n\n" +
                "Medication Regimen: %s\n\n" +
                "Provide:\n" +
                "1. Adherence Risk Level (Low/Medium/High)\n" +
                "2. Key Risk Factors\n" +
                "3. Strategies to Improve Adherence\n" +
                "4. Monitoring Recommendations",
                patientFactors, medicationRegimen
            );
            
            return callHealthPredictionModel(adherencePrompt);
            
        } catch (Exception e) {
            logger.error("Error predicting medication adherence: {}", e.getMessage());
            return "Unable to predict medication adherence. Please consult with a pharmacist or healthcare provider.";
        }
    }

    /**
     * Generate lifestyle modification recommendations
     */
    public String generateLifestyleRecommendations(String healthConditions, String currentLifestyle) {
        try {
            String lifestylePrompt = String.format(
                "Generate personalized lifestyle modification recommendations:\n\n" +
                "Current Health Conditions: %s\n\n" +
                "Current Lifestyle: %s\n\n" +
                "Provide specific, actionable recommendations for:\n" +
                "1. Diet and Nutrition\n" +
                "2. Physical Activity\n" +
                "3. Sleep Hygiene\n" +
                "4. Stress Management\n" +
                "5. Preventive Measures\n\n" +
                "Include timeline and measurable goals.",
                healthConditions, currentLifestyle
            );
            
            return callHealthPredictionModel(lifestylePrompt);
            
        } catch (Exception e) {
            logger.error("Error generating lifestyle recommendations: {}", e.getMessage());
            return "Unable to generate lifestyle recommendations. Please consult with a healthcare professional.";
        }
    }

    /**
     * Assess mental health risk factors
     */
    public String assessMentalHealthRisk(String psychosocialFactors, String symptoms) {
        try {
            String mentalHealthPrompt = String.format(
                "Assess mental health risk based on the following information:\n\n" +
                "Psychosocial Factors: %s\n\n" +
                "Current Symptoms: %s\n\n" +
                "Provide:\n" +
                "1. Risk Assessment (Low/Medium/High)\n" +
                "2. Areas of Concern\n" +
                "3. Protective Factors\n" +
                "4. Recommended Interventions\n" +
                "5. When to Seek Professional Help\n\n" +
                "Include mental health resources and crisis information if appropriate.",
                psychosocialFactors, symptoms
            );
            
            String assessment = callHealthPredictionModel(mentalHealthPrompt);
            
            // Add mental health crisis information
            assessment += "\n\n🚨 CRISIS RESOURCES:\n" +
                         "• National Suicide Prevention Lifeline: 988\n" +
                         "• Crisis Text Line: Text HOME to 741741\n" +
                         "• Emergency Services: 911\n" +
                         "If you're having thoughts of self-harm, please seek immediate professional help.";
            
            return assessment;
            
        } catch (Exception e) {
            logger.error("Error assessing mental health risk: {}", e.getMessage());
            return "Unable to assess mental health risk. Please consult with a mental health professional for proper evaluation.";
        }
    }

    /**
     * Generate preventive care recommendations
     */
    public List<String> generatePreventiveCareRecommendations(int age, String gender, String riskFactors) {
        List<String> recommendations = new ArrayList<>();
        
        try {
            String preventivePrompt = String.format(
                "Generate age-appropriate preventive care recommendations:\n\n" +
                "Age: %d\nGender: %s\nRisk Factors: %s\n\n" +
                "Provide specific screening recommendations, vaccination schedules, and preventive measures. " +
                "Format as a numbered list of actionable items.",
                age, gender, riskFactors
            );
            
            String response = callHealthPredictionModel(preventivePrompt);
            
            // Parse response into list
            if (response != null && !response.trim().isEmpty()) {
                String[] lines = response.split("\n");
                for (String line : lines) {
                    line = line.trim();
                    if (!line.isEmpty() && (line.matches("\\d+\\..*") || line.startsWith("•") || line.startsWith("-"))) {
                        recommendations.add(line);
                    }
                }
            }
            
            if (recommendations.isEmpty()) {
                recommendations.add("Consult with a healthcare provider for personalized preventive care recommendations.");
            }
            
        } catch (Exception e) {
            logger.error("Error generating preventive care recommendations: {}", e.getMessage());
            recommendations.add("Unable to generate recommendations. Please consult with a healthcare provider.");
        }
        
        return recommendations;
    }

    // Private helper methods

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(huggingFaceApiKey);
        headers.set("User-Agent", "MediConnect-HealthPredictor/1.0");
        return headers;
    }

    private String buildHealthRiskPrompt(String patientData) {
        return String.format(
            "You are a health risk assessment expert. Analyze the following patient data and identify potential health risks.\n\n" +
            "PATIENT DATA:\n%s\n\n" +
            "Please provide a comprehensive health risk assessment including:\n\n" +
            "1. IDENTIFIED RISK FACTORS\n" +
            "   - List specific risk factors found in the data\n" +
            "   - Categorize by risk level (High/Medium/Low)\n\n" +
            "2. POTENTIAL HEALTH CONDITIONS\n" +
            "   - Conditions the patient may be at risk for\n" +
            "   - Timeline for potential development\n\n" +
            "3. PREVENTIVE RECOMMENDATIONS\n" +
            "   - Specific actions to reduce identified risks\n" +
            "   - Lifestyle modifications\n" +
            "   - Screening recommendations\n\n" +
            "4. MONITORING SCHEDULE\n" +
            "   - Regular check-ups and tests needed\n" +
            "   - Warning signs to watch for\n\n" +
            "Provide evidence-based recommendations and emphasize the importance of professional medical consultation.",
            patientData
        );
    }

    private String buildProgressionPrompt(String condition, String history) {
        return String.format(
            "Analyze disease progression for the following medical case:\n\n" +
            "CURRENT CONDITION: %s\n\n" +
            "PATIENT HISTORY: %s\n\n" +
            "Provide a detailed progression analysis including:\n\n" +
            "1. LIKELY PROGRESSION PATTERNS\n" +
            "2. TIMELINE EXPECTATIONS\n" +
            "3. FACTORS AFFECTING PROGRESSION\n" +
            "4. INTERVENTION OPPORTUNITIES\n" +
            "5. PROGNOSIS INDICATORS\n\n" +
            "Include both best-case and worst-case scenarios with probability estimates.",
            condition, history != null ? history : "No significant history provided"
        );
    }

    private String buildRecommendationPrompt(String profile, String goals) {
        return String.format(
            "Generate personalized health recommendations based on:\n\n" +
            "PATIENT PROFILE: %s\n\n" +
            "HEALTH GOALS: %s\n\n" +
            "Provide comprehensive recommendations for:\n\n" +
            "1. IMMEDIATE ACTIONS (0-30 days)\n" +
            "2. SHORT-TERM GOALS (1-6 months)\n" +
            "3. LONG-TERM OBJECTIVES (6+ months)\n" +
            "4. LIFESTYLE MODIFICATIONS\n" +
            "5. MONITORING PARAMETERS\n\n" +
            "Make recommendations specific, measurable, and achievable.",
            profile, goals != null ? goals : "General health improvement"
        );
    }

    private String buildCardiovascularRiskPrompt(String riskData) {
        return String.format(
            "Assess cardiovascular disease risk based on the following factors:\n\n" +
            "%s\n\n" +
            "Provide:\n" +
            "1. OVERALL CARDIOVASCULAR RISK LEVEL\n" +
            "2. KEY CONTRIBUTING FACTORS\n" +
            "3. MODIFIABLE RISK FACTORS\n" +
            "4. RECOMMENDED INTERVENTIONS\n" +
            "5. MONITORING SCHEDULE\n" +
            "6. EMERGENCY WARNING SIGNS\n\n" +
            "Include both pharmaceutical and lifestyle interventions.",
            riskData
        );
    }

    private String processHealthPredictionResponse(ResponseEntity<String> response, String originalPrompt) throws Exception {
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            
            if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                JsonNode firstResult = jsonResponse.get(0);
                String generatedText = firstResult.get("generated_text").asText();
                
                String prediction = generatedText.replace(originalPrompt, "").trim();
                
                if (prediction.isEmpty()) {
                    prediction = "Unable to generate health risk prediction based on the provided data. Please consult with a healthcare professional for comprehensive risk assessment.";
                }
                
                return addHealthPredictionDisclaimer(prediction);
            } else {
                return "Health risk prediction could not be generated from the provided patient data.";
            }
        } else {
            throw new RuntimeException("Health prediction API call failed with status: " + response.getStatusCode());
        }
    }

    private String callHealthPredictionModel(String prompt) throws Exception {
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
        ResponseEntity<String> response = restTemplate.postForEntity(healthPredictionUrl, entity, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                return jsonResponse.get(0).get("generated_text").asText().replace(prompt, "").trim();
            }
        }
        
        throw new RuntimeException("Health prediction API call failed");
    }

    private String formatRiskFactors(Map<String, Object> riskFactors) {
        StringBuilder formatted = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : riskFactors.entrySet()) {
            formatted.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        return formatted.toString();
    }

    private String addHealthPredictionDisclaimer(String prediction) {
        if (!prediction.toLowerCase().contains("disclaimer") && 
            !prediction.toLowerCase().contains("consult") &&
            !prediction.toLowerCase().contains("healthcare professional")) {
            
            prediction += "\n\n⚠️ HEALTH PREDICTION DISCLAIMER: This AI-generated health risk assessment is for informational purposes only and should not replace professional medical advice, diagnosis, or treatment. Please consult with a qualified healthcare provider for personalized medical guidance and risk assessment.";
        }
        return prediction;
    }
}