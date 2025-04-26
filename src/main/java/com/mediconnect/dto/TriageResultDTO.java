package com.mediconnect.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class TriageResultDTO {
    
    private Long triageId;
    
    private String urgencyLevel;
    
    public Long getTriageId() {
		return triageId;
	}

	public void setTriageId(Long triageId) {
		this.triageId = triageId;
	}

	public String getUrgencyLevel() {
		return urgencyLevel;
	}

	public void setUrgencyLevel(String urgencyLevel) {
		this.urgencyLevel = urgencyLevel;
	}

	public String getRecommendedAction() {
		return recommendedAction;
	}

	public void setRecommendedAction(String recommendedAction) {
		this.recommendedAction = recommendedAction;
	}

	public String getAssessmentDetails() {
		return assessmentDetails;
	}

	public void setAssessmentDetails(String assessmentDetails) {
		this.assessmentDetails = assessmentDetails;
	}

	public Double getConfidenceScore() {
		return confidenceScore;
	}

	public void setConfidenceScore(Double confidenceScore) {
		this.confidenceScore = confidenceScore;
	}

	public Map<String, Double> getPossibleConditions() {
		return possibleConditions;
	}

	public void setPossibleConditions(Map<String, Double> possibleConditions) {
		this.possibleConditions = possibleConditions;
	}

	public List<String> getWarningSignsToMonitor() {
		return warningSignsToMonitor;
	}

	public void setWarningSignsToMonitor(List<String> warningSignsToMonitor) {
		this.warningSignsToMonitor = warningSignsToMonitor;
	}

	private String recommendedAction;
    
    private String assessmentDetails;
    
    private Double confidenceScore;
    
    private Map<String, Double> possibleConditions;
    
    private List<String> warningSignsToMonitor;
}