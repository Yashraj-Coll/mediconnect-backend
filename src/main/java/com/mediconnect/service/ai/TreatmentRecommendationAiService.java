package com.mediconnect.service.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TreatmentRecommendationAiService {
    @Autowired
    private OpenAiService openAiService;
    
    public String recommendTreatment(String diagnosis, String patientInfo) {
        String systemPrompt = "You are a medical treatment specialist. Based on the diagnosis and patient information, suggest appropriate treatment options, considering best practices and recent medical guidelines.";
        String userPrompt = "Diagnosis: " + diagnosis + "\nPatient Info: " + patientInfo + "\nRecommend appropriate treatment options.";
        return openAiService.generateResponse(systemPrompt, userPrompt);
    }
}