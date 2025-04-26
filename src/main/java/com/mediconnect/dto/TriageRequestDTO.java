package com.mediconnect.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class TriageRequestDTO {
    
    private Long patientId;
    
    private String symptoms;
    
    private Map<String, Object> vitalSigns;
    
    public Long getPatientId() {
		return patientId;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
	}

	public String getSymptoms() {
		return symptoms;
	}

	public void setSymptoms(String symptoms) {
		this.symptoms = symptoms;
	}

	public Map<String, Object> getVitalSigns() {
		return vitalSigns;
	}

	public void setVitalSigns(Map<String, Object> vitalSigns) {
		this.vitalSigns = vitalSigns;
	}

	public List<String> getCurrentMedications() {
		return currentMedications;
	}

	public void setCurrentMedications(List<String> currentMedications) {
		this.currentMedications = currentMedications;
	}

	public Integer getPainLevel() {
		return painLevel;
	}

	public void setPainLevel(Integer painLevel) {
		this.painLevel = painLevel;
	}

	public String getSymptomDuration() {
		return symptomDuration;
	}

	public void setSymptomDuration(String symptomDuration) {
		this.symptomDuration = symptomDuration;
	}

	private List<String> currentMedications;
    
    private Integer painLevel;
    
    private String symptomDuration;
}