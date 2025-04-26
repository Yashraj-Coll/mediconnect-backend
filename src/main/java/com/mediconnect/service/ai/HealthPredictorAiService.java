package com.mediconnect.service.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HealthPredictorAiService {
    @Autowired
    private OpenAiService openAiService;
    
    public String predictHealthRisks(String patientData) {
        String systemPrompt = "You are a health risk assessment expert. Based on the patient data provided, identify potential health risks and provide preventive recommendations.";
        return openAiService.generateResponse(systemPrompt, patientData);
    }
}