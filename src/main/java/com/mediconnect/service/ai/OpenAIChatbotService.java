package com.mediconnect.service.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class OpenAIChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIChatbotService.class);

    @Value("${huggingface.api.key}")
    private String huggingFaceApiKey;
    
    @Value("${huggingface.endpoints.chat}")
    private String chatModelUrl;
    
    @Value("${huggingface.chat.max-tokens:500}")
    private int maxTokens;
    
    @Value("${huggingface.chat.temperature:0.7}")
    private double temperature;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generate chat responses using Hugging Face Falcon-7B model
     * Replaces OpenAI chat completion functionality
     */
    public String generateChatResponse(String prompt) {
        return generateChatResponse(prompt, null);
    }

    /**
     * Generate chat responses with optional context
     */
    public String generateChatResponse(String prompt, String context) {
        try {
            HttpHeaders headers = createHeaders();
            String enhancedPrompt = buildMedicalPrompt(prompt, context);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", enhancedPrompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", maxTokens,
                "temperature", temperature,
                "top_p", 0.9,
                "do_sample", true,
                "return_full_text", false,
                "pad_token_id", 50256
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(chatModelUrl, entity, String.class);
            
            return processHuggingFaceResponse(response, enhancedPrompt);
            
        } catch (Exception e) {
            logger.error("Error calling Hugging Face chat API: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    /**
     * Enhanced method for medical context-aware responses
     */
    public String generateMedicalResponse(String prompt, String patientContext) {
        String medicalContext = String.format(
            "Patient Information: %s\n\n" +
            "Medical Question: %s\n\n" +
            "Please provide a comprehensive medical response considering the patient's context. " +
            "Include relevant medical considerations, potential risks, and recommendations.",
            patientContext != null ? patientContext : "No specific patient context provided",
            prompt
        );
        
        return generateChatResponse(prompt, medicalContext);
    }

    /**
     * Generate responses for symptom analysis
     */
    public String analyzeSymptoms(String symptoms) {
        String symptomPrompt = String.format(
            "As a medical AI assistant, analyze these symptoms: %s\n\n" +
            "Provide:\n" +
            "1. Possible conditions (with likelihood)\n" +
            "2. Recommended immediate actions\n" +
            "3. When to seek medical attention\n" +
            "4. Self-care measures\n\n" +
            "Always emphasize consulting healthcare professionals for proper diagnosis.",
            symptoms
        );
        
        return generateChatResponse(symptomPrompt);
    }

    /**
     * Generate health education content
     */
    public String generateHealthEducation(String topic) {
        String educationPrompt = String.format(
            "Provide comprehensive health education about: %s\n\n" +
            "Include:\n" +
            "- Basic explanation in simple terms\n" +
            "- Prevention strategies\n" +
            "- Risk factors\n" +
            "- When to consult a doctor\n" +
            "- Reliable sources for more information",
            topic
        );
        
        return generateChatResponse(educationPrompt);
    }

    /**
     * Test Hugging Face API connectivity and configuration
     */
    public Map<String, Object> testConfiguration() {
        Map<String, Object> testResult = new HashMap<>();
        
        try {
            testResult.put("apiKeyConfigured", isApiKeyConfigured());
            testResult.put("apiKeyMasked", maskApiKey(huggingFaceApiKey));
            testResult.put("apiUrl", chatModelUrl);
            testResult.put("modelName", "tiiuae/falcon-7b-instruct");
            testResult.put("maxTokens", maxTokens);
            testResult.put("temperature", temperature);
            
            if (isApiKeyConfigured()) {
                String testResponse = generateChatResponse("Hello, please confirm the AI service is working by responding with 'Service Active'");
                testResult.put("testResponse", testResponse);
                testResult.put("testSuccessful", isTestSuccessful(testResponse));
            } else {
                testResult.put("testSuccessful", false);
                testResult.put("error", "API key not configured");
            }
            
        } catch (Exception e) {
            testResult.put("testSuccessful", false);
            testResult.put("error", e.getMessage());
            logger.error("Configuration test failed: {}", e.getMessage());
        }
        
        return testResult;
    }

    /**
     * Get service health status
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", "HuggingFace ChatBot Service");
        health.put("status", isApiKeyConfigured() ? "UP" : "DOWN");
        health.put("model", "tiiuae/falcon-7b-instruct");
        health.put("endpoint", chatModelUrl);
        health.put("timestamp", System.currentTimeMillis());
        return health;
    }

    // Private helper methods

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(huggingFaceApiKey);
        headers.set("User-Agent", "MediConnect/1.0");
        return headers;
    }

    private String buildMedicalPrompt(String prompt, String context) {
        StringBuilder promptBuilder = new StringBuilder();
        
        promptBuilder.append("You are a helpful medical AI assistant for MediConnect Health Platform. ");
        promptBuilder.append("Provide accurate, helpful medical information while always noting that this is not a substitute for professional medical advice. ");
        promptBuilder.append("Keep responses professional, compassionate, and encouraging users to consult healthcare professionals for serious concerns.\n\n");
        
        if (context != null && !context.trim().isEmpty()) {
            promptBuilder.append("Context: ").append(context).append("\n\n");
        }
        
        promptBuilder.append("User Question: ").append(prompt);
        
        return promptBuilder.toString();
    }

    private String processHuggingFaceResponse(ResponseEntity<String> response, String originalPrompt) throws Exception {
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            
            if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                JsonNode firstResult = jsonResponse.get(0);
                String generatedText = firstResult.get("generated_text").asText();
                
                // Clean response by removing the original prompt
                String cleanResponse = generatedText.replace(originalPrompt, "").trim();
                
                if (cleanResponse.isEmpty()) {
                    cleanResponse = "I'm here to help with your health questions. Please feel free to ask me anything about health and wellness.";
                }
                
                // Add medical disclaimer
                cleanResponse = addMedicalDisclaimer(cleanResponse);
                
                return cleanResponse;
            } else {
                return "I'm experiencing some technical difficulties. Please try again or consult with a healthcare professional.";
            }
        } else {
            throw new RuntimeException("Hugging Face API call failed with status: " + response.getStatusCode());
        }
    }

    private String addMedicalDisclaimer(String response) {
        if (!response.toLowerCase().contains("disclaimer") && 
            !response.toLowerCase().contains("consult") &&
            !response.toLowerCase().contains("healthcare professional")) {
            
            response += "\n\n⚠️ Medical Disclaimer: This information is for educational purposes only and should not replace professional medical advice. Please consult with a qualified healthcare provider for personalized medical guidance.";
        }
        return response;
    }

    private boolean isApiKeyConfigured() {
        return huggingFaceApiKey != null && !huggingFaceApiKey.trim().isEmpty() && !huggingFaceApiKey.equals("your-api-key-here");
    }

    private String maskApiKey(String key) {
        if (key == null || key.isEmpty()) {
            return "NOT_SET";
        }
        if (key.length() < 12) {
            return "***";
        }
        return key.substring(0, 8) + "***" + key.substring(key.length() - 4);
    }

    private boolean isTestSuccessful(String response) {
        return response != null && 
               !response.contains("technical difficulties") && 
               !response.contains("configuration issues") &&
               !response.contains("API call failed") &&
               response.length() > 10;
    }

    private String handleException(Exception e) {
        String errorMessage = e.getMessage();
        
        if (e instanceof HttpServerErrorException) {
            HttpServerErrorException serverError = (HttpServerErrorException) e;
            if (serverError.getStatusCode().value() == 503) {
                return "The AI model is currently loading. Please wait a moment and try again.";
            }
        } else if (e instanceof HttpClientErrorException) {
            HttpClientErrorException clientError = (HttpClientErrorException) e;
            if (clientError.getStatusCode().value() == 429) {
                return "Too many requests. Please wait a moment and try again.";
            } else if (clientError.getStatusCode().value() == 401) {
                return "Authentication failed. Please check the API configuration.";
            }
        } else if (e instanceof ResourceAccessException) {
            return "Connection timeout. Please check your internet connection and try again.";
        }
        
        if (errorMessage != null) {
            if (errorMessage.toLowerCase().contains("model is loading")) {
                return "The AI model is currently loading. Please wait a moment and try again.";
            } else if (errorMessage.toLowerCase().contains("rate limit")) {
                return "Service is busy. Please wait a moment and try again.";
            }
        }
        
        return "I'm experiencing technical difficulties. Please try again later or consult with a healthcare professional directly.";
    }
}