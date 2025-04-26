package com.mediconnect.dto;

import lombok.Data;

@Data
public class TranslationResponseDTO {
    
    private String originalText;
    
    private String translatedText;
    
    private String sourceLanguage;
    
    public String getOriginalText() {
		return originalText;
	}

	public void setOriginalText(String originalText) {
		this.originalText = originalText;
	}

	public String getTranslatedText() {
		return translatedText;
	}

	public void setTranslatedText(String translatedText) {
		this.translatedText = translatedText;
	}

	public String getSourceLanguage() {
		return sourceLanguage;
	}

	public void setSourceLanguage(String sourceLanguage) {
		this.sourceLanguage = sourceLanguage;
	}

	public String getTargetLanguage() {
		return targetLanguage;
	}

	public void setTargetLanguage(String targetLanguage) {
		this.targetLanguage = targetLanguage;
	}

	public Double getConfidenceScore() {
		return confidenceScore;
	}

	public void setConfidenceScore(Double confidenceScore) {
		this.confidenceScore = confidenceScore;
	}

	public boolean isHasCulturalAdaptations() {
		return hasCulturalAdaptations;
	}

	public void setHasCulturalAdaptations(boolean hasCulturalAdaptations) {
		this.hasCulturalAdaptations = hasCulturalAdaptations;
	}

	public String getCulturalNotes() {
		return culturalNotes;
	}

	public void setCulturalNotes(String culturalNotes) {
		this.culturalNotes = culturalNotes;
	}

	private String targetLanguage;
    
    private Double confidenceScore;
    
    private boolean hasCulturalAdaptations;
    
    private String culturalNotes;
}