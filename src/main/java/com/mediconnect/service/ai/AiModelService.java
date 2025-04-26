package com.mediconnect.service.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiModelService {
    @Autowired
    private OpenAiService openAiService;
    
    public String diagnoseFromDescription(String symptoms) {
        String systemPrompt = "You are a diagnostic expert. Based on the symptoms described, suggest possible diagnoses, recommended tests, and next steps. Always include a disclaimer about consulting a healthcare professional.";
        return openAiService.generateResponse(systemPrompt, symptoms);
    }
    
    public String analyzeImage(byte[] imageBytes) {
        return openAiService.generateImageResponse(imageBytes, "Analyze this medical image. Identify any visible conditions, abnormalities, or notable features. Provide possible diagnoses based on the image alone.");
    }
}