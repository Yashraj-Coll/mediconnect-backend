package com.mediconnect.model;

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
@Table(name = "genomic_risk_factors")
@Data
public class GenomicRiskFactor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public GenomicData getGenomicData() {
		return genomicData;
	}

	public void setGenomicData(GenomicData genomicData) {
		this.genomicData = genomicData;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getRiskLevel() {
		return riskLevel;
	}

	public void setRiskLevel(String riskLevel) {
		this.riskLevel = riskLevel;
	}

	public Double getRiskScore() {
		return riskScore;
	}

	public void setRiskScore(Double riskScore) {
		this.riskScore = riskScore;
	}

	public String getMarkers() {
		return markers;
	}

	public void setMarkers(String markers) {
		this.markers = markers;
	}

	public String getRecommendations() {
		return recommendations;
	}

	public void setRecommendations(String recommendations) {
		this.recommendations = recommendations;
	}

	@ManyToOne
    @JoinColumn(name = "genomic_data_id", nullable = false)
    private GenomicData genomicData;
    
	@Column(name = "`condition`")
	private String condition;
    
    private String riskLevel;
    
    private Double riskScore;
    
    @Column(nullable = true)
    private String markers;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String recommendations;
}