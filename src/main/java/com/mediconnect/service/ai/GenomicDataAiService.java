package com.mediconnect.service.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenomicDataAiService {
    @Autowired
    private OpenAiService openAiService;
    
    public String analyzeGenomicData(String genomicData) {
        String systemPrompt = "You are a genomics expert. Analyze the provided genomic data and identify potential genetic markers, health risks, or conditions. Provide a comprehensive yet understandable report.";
        return openAiService.generateResponse(systemPrompt, genomicData);
    }
}