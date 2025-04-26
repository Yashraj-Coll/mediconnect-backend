package com.mediconnect.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class GenomicAnalysisDTO {
    
    private Long genomicDataId;
    
    private Long patientId;
    
    private String dataType;
    
    public Long getGenomicDataId() {
		return genomicDataId;
	}

	public void setGenomicDataId(Long genomicDataId) {
		this.genomicDataId = genomicDataId;
	}

	public Long getPatientId() {
		return patientId;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public List<Map<String, Object>> getRiskFactors() {
		return riskFactors;
	}

	public void setRiskFactors(List<Map<String, Object>> riskFactors) {
		this.riskFactors = riskFactors;
	}

	public LocalDateTime getProcessedAt() {
		return processedAt;
	}

	public void setProcessedAt(LocalDateTime processedAt) {
		this.processedAt = processedAt;
	}

	private List<Map<String, Object>> riskFactors;
    
    private LocalDateTime processedAt;
}