package com.mediconnect.dto;

import java.util.List;

import lombok.Data;

@Data
public class TreatmentOptionDTO {
    
    private String name;
    
    private String description;
    
    private String type;
    
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double getEffectiveness() {
		return effectiveness;
	}

	public void setEffectiveness(Double effectiveness) {
		this.effectiveness = effectiveness;
	}

	public List<String> getRisks() {
		return risks;
	}

	public void setRisks(List<String> risks) {
		this.risks = risks;
	}

	public List<String> getBenefits() {
		return benefits;
	}

	public void setBenefits(List<String> benefits) {
		this.benefits = benefits;
	}

	public List<String> getContraindications() {
		return contraindications;
	}

	public void setContraindications(List<String> contraindications) {
		this.contraindications = contraindications;
	}

	public List<String> getSideEffects() {
		return sideEffects;
	}

	public void setSideEffects(List<String> sideEffects) {
		this.sideEffects = sideEffects;
	}

	public String getTimeToEffect() {
		return timeToEffect;
	}

	public void setTimeToEffect(String timeToEffect) {
		this.timeToEffect = timeToEffect;
	}

	public Integer getCost() {
		return cost;
	}

	public void setCost(Integer cost) {
		this.cost = cost;
	}

	private Double effectiveness;
    
    private List<String> risks;
    
    private List<String> benefits;
    
    private List<String> contraindications;
    
    private List<String> sideEffects;
    
    private String timeToEffect;
    
    private Integer cost;
}