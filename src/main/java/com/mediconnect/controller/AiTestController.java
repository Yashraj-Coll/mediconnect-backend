package com.mediconnect.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Test controller to verify AI service connectivity
 * Updated for Text Generation instead of deprecated Conversational
 */
@RestController
@RequestMapping("/api/public/ai/test")
@CrossOrigin(origins = "*")
public class AiTestController {

    @Value("${huggingface.api.key}")
    private String apiKey;

    @Value("${huggingface.endpoints.chat}")
    private String chatEndpoint;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> testAiStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test API key presence
            if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("hf_YOUR_ACTUAL_API_KEY_HERE")) {
                response.put("status", "error");
                response.put("message", "Hugging Face API key not configured");
                response.put("apiKey", "Missing or invalid");
                return ResponseEntity.ok(response);
            }

            response.put("status", "success");
            response.put("message", "API key is configured");
            response.put("apiKey", apiKey.substring(0, 8) + "...");
            response.put("endpoint", chatEndpoint);
            response.put("modelType", "Text Generation (GPT-2)");
            response.put("timestamp", System.currentTimeMillis());

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Configuration test failed: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/simple-chat")
    public ResponseEntity<Map<String, Object>> testSimpleChat(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String userMessage = request.get("message");
            if (userMessage == null || userMessage.trim().isEmpty()) {
                userMessage = "What are the symptoms of diabetes?";
            }

            // Create medical prompt for GPT-2 text generation
            String medicalPrompt = "Medical Question: " + userMessage + "\nMedical Answer:";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // Updated request body for text-generation (not conversational)
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", medicalPrompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", 80,
                "temperature", 0.7,
                "do_sample", true,
                "return_full_text", false,
                "top_p", 0.9
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            long startTime = System.currentTimeMillis();
            ResponseEntity<String> apiResponse = restTemplate.exchange(
                chatEndpoint,
                HttpMethod.POST,
                entity,
                String.class
            );
            long endTime = System.currentTimeMillis();

            response.put("success", true);
            response.put("userMessage", userMessage);
            response.put("prompt", medicalPrompt);
            response.put("aiResponse", apiResponse.getBody());
            response.put("responseTime", (endTime - startTime) + "ms");
            response.put("httpStatus", apiResponse.getStatusCode().value());
            response.put("modelType", "openai-community/gpt2");
            response.put("timestamp", System.currentTimeMillis());

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("message", "Failed to get AI response: " + e.getClass().getSimpleName());
            response.put("endpoint", chatEndpoint);
            response.put("suggestion", "GPT-2 model might be loading. Try again in 30-60 seconds.");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/models")
    public ResponseEntity<Map<String, Object>> listModelsInfo() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("chatModel", "openai-community/gpt2");
        response.put("chatEndpoint", chatEndpoint);
        response.put("apiKeyConfigured", apiKey != null && !apiKey.trim().isEmpty());
        response.put("timeout", "15000ms");
        response.put("maxTokens", 150);
        response.put("temperature", 0.7);
        response.put("modelType", "Text Generation");
        response.put("note", "Using GPT-2 for reliable text generation instead of deprecated conversational models");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-different-models")
    public ResponseEntity<Map<String, Object>> testDifferentModels(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String[] modelsToTest = {
            "openai-community/gpt2",
            "openai-community/gpt2-medium"
        };
        
        String userMessage = request.getOrDefault("message", "Hello, what is diabetes?");
        
        for (String model : modelsToTest) {
            try {
                String endpoint = "https://api-inference.huggingface.co/models/" + model;
                String prompt = "Medical Question: " + userMessage + "\nAnswer:";
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("inputs", prompt);
                requestBody.put("parameters", Map.of(
                    "max_new_tokens", 50,
                    "temperature", 0.7,
                    "return_full_text", false
                ));

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                
                long startTime = System.currentTimeMillis();
                ResponseEntity<String> apiResponse = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    entity,
                    String.class
                );
                long endTime = System.currentTimeMillis();
                
                response.put(model + "_status", "success");
                response.put(model + "_response", apiResponse.getBody());
                response.put(model + "_time", (endTime - startTime) + "ms");
                
            } catch (Exception e) {
                response.put(model + "_status", "failed");
                response.put(model + "_error", e.getMessage());
            }
        }
        
        return ResponseEntity.ok(response);
    }
}