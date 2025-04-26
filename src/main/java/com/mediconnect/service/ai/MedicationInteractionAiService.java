package com.mediconnect.service.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicationInteractionAiService {
    @Autowired
    private OpenAiService openAiService;
    
    public String checkMedicationInteractions(List<String> medications) {
        String systemPrompt = "You are a pharmacology expert. Analyze the list of medications and identify potential drug interactions, side effects, and precautions.";
        return openAiService.generateResponse(systemPrompt, "Analyze potential interactions between these medications: " + String.join(", ", medications));
    }
}