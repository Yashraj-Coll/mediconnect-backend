package com.mediconnect.dto.response;

public class ChatbotResponseDTO {
    private String response;
    private boolean success;
    private String error;
    private long timestamp;
    private String messageId;
    private String language;

    public ChatbotResponseDTO() {}

    public ChatbotResponseDTO(String response, boolean success) {
        this.response = response;
        this.success = success;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}