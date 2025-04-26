package com.mediconnect.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class HealthRiskAssessmentDTO {
    
    private Long assessmentId;
    
    private String overallRiskLevel;
    
    public Long getAssessmentId() {
		return assessmentId;
	}

	public void setAssessmentId(Long assessmentId) {
		this.assessmentId = assessmentId;
	}

	public String getOverallRiskLevel() {
		return overallRiskLevel;
	}

	public void setOverallRiskLevel(String overallRiskLevel) {
		this.overallRiskLevel = overallRiskLevel;
	}

	public Map<String, Double> getHealthRisks() {
		return healthRisks;
	}

	public void setHealthRisks(Map<String, Double> healthRisks) {
		this.healthRisks = healthRisks;
	}

	public Map<String, List<String>> getRecommendations() {
		return recommendations;
	}

	public void setRecommendations(Map<String, List<String>> recommendations) {
		this.recommendations = recommendations;
	}

	public Map<String, String> getLifestyleSuggestions() {
		return lifestyleSuggestions;
	}

	public void setLifestyleSuggestions(Map<String, String> lifestyleSuggestions) {
		this.lifestyleSuggestions = lifestyleSuggestions;
	}

	public String getNextCheckupRecommendation() {
		return nextCheckupRecommendation;
	}

	public void setNextCheckupRecommendation(String nextCheckupRecommendation) {
		this.nextCheckupRecommendation = nextCheckupRecommendation;
	}

	private Map<String, Double> healthRisks;
    
    private Map<String, List<String>> recommendations;
    
    private Map<String, String> lifestyleSuggestions;
    
    private String nextCheckupRecommendation;
}