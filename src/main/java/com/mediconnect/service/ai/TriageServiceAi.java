package com.mediconnect.service.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TriageServiceAi {
    @Autowired
    private OpenAiService openAiService;
    
    public String performTriage(String symptoms) {
        String systemPrompt = "You are an emergency medicine expert. Assess the urgency level of the given symptoms and classify as: EMERGENCY (immediate care needed), URGENT (see doctor within 24 hours), SEMI-URGENT (see doctor within few days), or ROUTINE (can be scheduled). Provide a brief explanation.";
        return openAiService.generateResponse(systemPrompt, "Patient symptoms: " + symptoms);
    }
}