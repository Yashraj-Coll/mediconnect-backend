package com.mediconnect.controller;

import com.mediconnect.service.ai.OpenAIChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatbotController {
    @Autowired
    private OpenAIChatbotService chatbotService;
    
    @PostMapping
    public ResponseEntity<?> getChatResponse(@RequestBody Map<String, String> request) {
        try {
            String userQuery = request.get("query");
            String response = chatbotService.generateChatResponse(userQuery);
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}