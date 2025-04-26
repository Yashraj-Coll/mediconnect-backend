package com.mediconnect.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_diagnosis_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiDiagnosisResult {
    
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

	public String getModelVersion() {
		return modelVersion;
	}

	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}

	public String getInputParameters() {
		return inputParameters;
	}

	public void setInputParameters(String inputParameters) {
		this.inputParameters = inputParameters;
	}

	public String getSymptoms() {
		return symptoms;
	}

	public void setSymptoms(String symptoms) {
		this.symptoms = symptoms;
	}

	public String getAnalysisReport() {
		return analysisReport;
	}

	public void setAnalysisReport(String analysisReport) {
		this.analysisReport = analysisReport;
	}

	public List<DiagnosisPrediction> getPredictions() {
		return predictions;
	}

	public void setPredictions(List<DiagnosisPrediction> predictions) {
		this.predictions = predictions;
	}

	public String getRecommendedTests() {
		return recommendedTests;
	}

	public void setRecommendedTests(String recommendedTests) {
		this.recommendedTests = recommendedTests;
	}

	public String getTreatmentSuggestions() {
		return treatmentSuggestions;
	}

	public void setTreatmentSuggestions(String treatmentSuggestions) {
		this.treatmentSuggestions = treatmentSuggestions;
	}

	public String getSpecialNotes() {
		return specialNotes;
	}

	public void setSpecialNotes(String specialNotes) {
		this.specialNotes = specialNotes;
	}

	public Double getConfidenceScore() {
		return confidenceScore;
	}

	public void setConfidenceScore(Double confidenceScore) {
		this.confidenceScore = confidenceScore;
	}

	public AiDiagnosisStatus getStatus() {
		return status;
	}

	public void setStatus(AiDiagnosisStatus status) {
		this.status = status;
	}

	public Boolean getReviewedByDoctor() {
		return reviewedByDoctor;
	}

	public void setReviewedByDoctor(Boolean reviewedByDoctor) {
		this.reviewedByDoctor = reviewedByDoctor;
	}

	public String getDoctorFeedback() {
		return doctorFeedback;
	}

	public void setDoctorFeedback(String doctorFeedback) {
		this.doctorFeedback = doctorFeedback;
	}

	public LocalDateTime getAnalyzedAt() {
		return analyzedAt;
	}

	public void setAnalyzedAt(LocalDateTime analyzedAt) {
		this.analyzedAt = analyzedAt;
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
    
    @OneToOne
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;
    
    @NotNull
    private String modelVersion;
    
    @Column(columnDefinition = "TEXT")
    private String inputParameters;
    
    @Column(columnDefinition = "TEXT")
    private String symptoms;
    
    @Column(columnDefinition = "TEXT")
    private String analysisReport;
    
    @OneToMany(mappedBy = "aiDiagnosisResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiagnosisPrediction> predictions = new ArrayList<>();
    
    @Column(columnDefinition = "TEXT")
    private String recommendedTests;
    
    @Column(columnDefinition = "TEXT")
    private String treatmentSuggestions;
    
    @Column(length = 2000)
    private String specialNotes;
    
    private Double confidenceScore;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AiDiagnosisStatus status;
    
    private Boolean reviewedByDoctor;
    
    @Column(columnDefinition = "TEXT")
    private String doctorFeedback;
    
    private LocalDateTime analyzedAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        analyzedAt = LocalDateTime.now();
        if (reviewedByDoctor == null) {
            reviewedByDoctor = false;
        }
        if (status == null) {
            status = AiDiagnosisStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum AiDiagnosisStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}