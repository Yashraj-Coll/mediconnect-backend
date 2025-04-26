package com.mediconnect.service.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;

import java.util.*;

@Service
public class OpenAiService {
    @Value("${mediconnect.ai.model.api-key}")
    private String apiKey;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public String generateResponse(String systemPrompt, String userPrompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4");
        requestBody.put("messages", Arrays.asList(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user", "content", userPrompt)
        ));
        requestBody.put("temperature", 0.2);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://api.openai.com/v1/chat/completions",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {},
                Collections.emptyMap()
            );
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Empty response from OpenAI API");
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("No choices in OpenAI API response");
            }
            
            @SuppressWarnings("unchecked")
			Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null) {
                throw new RuntimeException("No message in OpenAI API response");
            }
            
            return (String) message.get("content");
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate AI response: " + e.getMessage(), e);
        }
    }
    
    public String generateImageResponse(byte[] imageBytes, String prompt) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> messageObj = new HashMap<>();
        messageObj.put("role", "user");
        
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", prompt);
        
        Map<String, Object> imageContent = new HashMap<>();
        imageContent.put("type", "image_url");
        Map<String, Object> imageUrl = new HashMap<>();
        imageUrl.put("url", "data:image/jpeg;base64," + base64Image);
        imageContent.put("image_url", imageUrl);
        
        contents.add(textContent);
        contents.add(imageContent);
        
        messageObj.put("content", contents);
        messages.add(messageObj);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4-vision-preview");
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 300);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://api.openai.com/v1/chat/completions",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {},
                Collections.emptyMap()
            );
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Empty response from OpenAI API");
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("No choices in OpenAI API response");
            }
            
            @SuppressWarnings("unchecked")
			Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null) {
                throw new RuntimeException("No message in OpenAI API response");
            }
            
            return (String) message.get("content");
        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze image: " + e.getMessage(), e);
        }
    }
    
    public String transcribeSpeech(byte[] audioData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("model", "whisper-1");
        body.add("file", new ByteArrayResource(audioData) {
            @Override
            public String getFilename() {
                return "audio.mp3";
            }
        });
        
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "https://api.openai.com/v1/audio/transcriptions",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {},
                Collections.emptyMap()
            );
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Empty response from OpenAI API");
            }
            
            return (String) responseBody.get("text");
        } catch (Exception e) {
            throw new RuntimeException("Failed to transcribe speech: " + e.getMessage(), e);
        }
    }
}