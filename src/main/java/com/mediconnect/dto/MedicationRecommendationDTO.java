package com.mediconnect.dto;

import java.util.List;

import lombok.Data;

@Data
public class MedicationRecommendationDTO {
    
    private String medicationName;
    
    public String getMedicationName() {
		return medicationName;
	}

	public void setMedicationName(String medicationName) {
		this.medicationName = medicationName;
	}

	public String getDosage() {
		return dosage;
	}

	public void setDosage(String dosage) {
		this.dosage = dosage;
	}

	public Double getEffectiveness() {
		return effectiveness;
	}

	public void setEffectiveness(Double effectiveness) {
		this.effectiveness = effectiveness;
	}

	public Double getGeneticCompatibility() {
		return geneticCompatibility;
	}

	public void setGeneticCompatibility(Double geneticCompatibility) {
		this.geneticCompatibility = geneticCompatibility;
	}

	public String getRationale() {
		return rationale;
	}

	public void setRationale(String rationale) {
		this.rationale = rationale;
	}

	public String getSideEffectRisk() {
		return sideEffectRisk;
	}

	public void setSideEffectRisk(String sideEffectRisk) {
		this.sideEffectRisk = sideEffectRisk;
	}

	public List<String> getAlternatives() {
		return alternatives;
	}

	public void setAlternatives(List<String> alternatives) {
		this.alternatives = alternatives;
	}

	private String dosage;
    
    private Double effectiveness;
    
    private Double geneticCompatibility;
    
    private String rationale;
    
    private String sideEffectRisk;
    
    private List<String> alternatives;
}