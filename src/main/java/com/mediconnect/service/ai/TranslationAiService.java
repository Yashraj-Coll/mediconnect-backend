package com.mediconnect.service.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TranslationAiService {
    @Autowired
    private OpenAiService openAiService;
    
    public String translateMedicalContent(String text, String sourceLanguage, String targetLanguage) {
        String systemPrompt = "You are a medical translator expert. Translate the given medical text from " + sourceLanguage + " to " + targetLanguage + " accurately, preserving all medical terminology.";
        return openAiService.generateResponse(systemPrompt, text);
    }
}