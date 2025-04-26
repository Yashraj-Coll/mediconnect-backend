package com.mediconnect.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "health_risk_assessments")
@Data
public class HealthRiskAssessment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public String getOverallRiskLevel() {
		return overallRiskLevel;
	}

	public void setOverallRiskLevel(String overallRiskLevel) {
		this.overallRiskLevel = overallRiskLevel;
	}

	public String getHealthRisks() {
		return healthRisks;
	}

	public void setHealthRisks(String healthRisks) {
		this.healthRisks = healthRisks;
	}

	public String getRecommendations() {
		return recommendations;
	}

	public void setRecommendations(String recommendations) {
		this.recommendations = recommendations;
	}

	public String getLifestyleSuggestions() {
		return lifestyleSuggestions;
	}

	public void setLifestyleSuggestions(String lifestyleSuggestions) {
		this.lifestyleSuggestions = lifestyleSuggestions;
	}

	public LocalDateTime getAssessedAt() {
		return assessedAt;
	}

	public void setAssessedAt(LocalDateTime assessedAt) {
		this.assessedAt = assessedAt;
	}

	@ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    
    private String overallRiskLevel;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String healthRisks;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String recommendations;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String lifestyleSuggestions;
    
    private LocalDateTime assessedAt;
}