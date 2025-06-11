package com.mediconnect.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatbotMessageDTO {
    @NotBlank(message = "Message cannot be empty")
    @Size(max = 2000, message = "Message too long")
    private String message;
    
    private String context;
    private String language;
    private Long userId;

    public ChatbotMessageDTO() {}

    public ChatbotMessageDTO(String message) {
        this.message = message;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}