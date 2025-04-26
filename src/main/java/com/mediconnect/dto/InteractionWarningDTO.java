package com.mediconnect.dto;

import lombok.Data;

@Data
public class InteractionWarningDTO {
    
    private String medication1;
    
    private String medication2;
    
    public String getMedication1() {
		return medication1;
	}

	public void setMedication1(String medication1) {
		this.medication1 = medication1;
	}

	public String getMedication2() {
		return medication2;
	}

	public void setMedication2(String medication2) {
		this.medication2 = medication2;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRecommendation() {
		return recommendation;
	}

	public void setRecommendation(String recommendation) {
		this.recommendation = recommendation;
	}

	public String getSourceReference() {
		return sourceReference;
	}

	public void setSourceReference(String sourceReference) {
		this.sourceReference = sourceReference;
	}

	public String getEvidence() {
		return evidence;
	}

	public void setEvidence(String evidence) {
		this.evidence = evidence;
	}

	private String severity;
    
    private String description;
    
    private String recommendation;
    
    private String sourceReference;
    
    private String evidence;
}