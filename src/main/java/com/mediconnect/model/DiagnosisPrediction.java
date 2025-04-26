package com.mediconnect.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diagnosis_predictions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisPrediction {
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AiDiagnosisResult getAiDiagnosisResult() {
		return aiDiagnosisResult;
	}

	public void setAiDiagnosisResult(AiDiagnosisResult aiDiagnosisResult) {
		this.aiDiagnosisResult = aiDiagnosisResult;
	}

	public String getConditionName() {
		return conditionName;
	}

	public void setConditionName(String conditionName) {
		this.conditionName = conditionName;
	}

	public Double getProbability() {
		return probability;
	}

	public void setProbability(Double probability) {
		this.probability = probability;
	}

	public String getReasonForDiagnosis() {
		return reasonForDiagnosis;
	}

	public void setReasonForDiagnosis(String reasonForDiagnosis) {
		this.reasonForDiagnosis = reasonForDiagnosis;
	}

	public String getSupportingEvidence() {
		return supportingEvidence;
	}

	public void setSupportingEvidence(String supportingEvidence) {
		this.supportingEvidence = supportingEvidence;
	}

	public Integer getSeverityLevel() {
		return severityLevel;
	}

	public void setSeverityLevel(Integer severityLevel) {
		this.severityLevel = severityLevel;
	}

	public String getRelatedConditions() {
		return relatedConditions;
	}

	public void setRelatedConditions(String relatedConditions) {
		this.relatedConditions = relatedConditions;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "ai_diagnosis_result_id")
    private AiDiagnosisResult aiDiagnosisResult;
    
    @NotNull
    private String conditionName;
    
    private Double probability;
    
    @Column(columnDefinition = "TEXT")
    private String reasonForDiagnosis;
    
    @Column(columnDefinition = "TEXT")
    private String supportingEvidence;
    
    private Integer severityLevel;
    
    @Column(columnDefinition = "TEXT")
    private String relatedConditions;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}