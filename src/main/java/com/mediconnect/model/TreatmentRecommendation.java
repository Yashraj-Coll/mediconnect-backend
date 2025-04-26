package com.mediconnect.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "treatment_recommendations")
@Data
public class TreatmentRecommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "medical_record_id", nullable = false)
    private MedicalRecord medicalRecord;
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public MedicalRecord getMedicalRecord() {
		return medicalRecord;
	}

	public void setMedicalRecord(MedicalRecord medicalRecord) {
		this.medicalRecord = medicalRecord;
	}

	public String getOverview() {
		return overview;
	}

	public void setOverview(String overview) {
		this.overview = overview;
	}

	public String getPersonalizationFactors() {
		return personalizationFactors;
	}

	public void setPersonalizationFactors(String personalizationFactors) {
		this.personalizationFactors = personalizationFactors;
	}

	public String getLifestyleRecommendations() {
		return lifestyleRecommendations;
	}

	public void setLifestyleRecommendations(String lifestyleRecommendations) {
		this.lifestyleRecommendations = lifestyleRecommendations;
	}

	public String getFollowUpRecommendations() {
		return followUpRecommendations;
	}

	public void setFollowUpRecommendations(String followUpRecommendations) {
		this.followUpRecommendations = followUpRecommendations;
	}

	public String getTreatmentOptions() {
		return treatmentOptions;
	}

	public void setTreatmentOptions(String treatmentOptions) {
		this.treatmentOptions = treatmentOptions;
	}

	public LocalDateTime getGeneratedAt() {
		return generatedAt;
	}

	public void setGeneratedAt(LocalDateTime generatedAt) {
		this.generatedAt = generatedAt;
	}

	@Lob
    @Column(columnDefinition = "TEXT")
    private String overview;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String personalizationFactors;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String lifestyleRecommendations;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String followUpRecommendations;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String treatmentOptions;
    
    private LocalDateTime generatedAt;
}