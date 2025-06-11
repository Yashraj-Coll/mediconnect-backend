package com.mediconnect.dto.response;

public class TranslationResponseDTO {
    private String translatedText;
    private String translation;
    private String sourceLanguage;
    private String targetLanguage;
    private boolean success;
    private String error;
    private long timestamp;

    public TranslationResponseDTO() {}

    public TranslationResponseDTO(String translatedText, String sourceLanguage, String targetLanguage) {
        this.translatedText = translatedText;
        this.translation = translatedText;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.success = true;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getTranslatedText() { return translatedText; }
    public void setTranslatedText(String translatedText) { 
        this.translatedText = translatedText;
        this.translation = translatedText;
    }
    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { 
        this.translation = translation;
        this.translatedText = translation;
    }
    public String getSourceLanguage() { return sourceLanguage; }
    public void setSourceLanguage(String sourceLanguage) { this.sourceLanguage = sourceLanguage; }
    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}