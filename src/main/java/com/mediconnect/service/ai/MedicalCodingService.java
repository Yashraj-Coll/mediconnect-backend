package com.mediconnect.service.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MedicalCodingService {
    @Autowired
    private OpenAiService openAiService;
    
    public String generateMedicalCoding(String clinicalText) {
        String systemPrompt = "You are a certified medical coding expert. Analyze the clinical text and provide appropriate ICD-10-CM codes with descriptions.";
        return openAiService.generateResponse(systemPrompt, clinicalText);
    }
}