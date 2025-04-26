package com.mediconnect.service.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpenAIChatbotService {
    @Autowired
    private OpenAiService openAiService;
    
    public String generateChatResponse(String userQuery) {
        String systemPrompt = "You are a helpful medical assistant providing accurate healthcare information. Answer patient questions clearly and suggest consulting a doctor when appropriate.";
        return openAiService.generateResponse(systemPrompt, userQuery);
    }
}