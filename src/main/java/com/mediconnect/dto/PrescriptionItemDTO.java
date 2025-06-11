package com.mediconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PrescriptionItemDTO {

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public String getInstructions() {
		return instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public Boolean getBeforeMeal() {
		return beforeMeal;
	}

	public void setBeforeMeal(Boolean beforeMeal) {
		this.beforeMeal = beforeMeal;
	}

	private Long id;
    
    @NotBlank(message = "Medication name is required")
    private String medicationName;
    
    private String dosage;
    
    private String frequency;
    
    private String duration;
    
    private String quantity;
    
    private String route;
    
    private String instructions;
    
    private Boolean beforeMeal;
}