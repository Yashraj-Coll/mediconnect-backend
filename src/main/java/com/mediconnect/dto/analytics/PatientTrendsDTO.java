package com.mediconnect.dto.analytics;

import java.time.LocalDate;
import java.util.Map;

import lombok.Data;

@Data
public class PatientTrendsDTO {
    
    private Long patientId;
    
    private Map<LocalDate, Double> temperatureData;
    
    private Map<LocalDate, Integer> heartRateData;
    
    public Long getPatientId() {
		return patientId;
	}

	public void setPatientId(Long patientId) {
		this.patientId = patientId;
	}

	public Map<LocalDate, Double> getTemperatureData() {
		return temperatureData;
	}

	public void setTemperatureData(Map<LocalDate, Double> temperatureData) {
		this.temperatureData = temperatureData;
	}

	public Map<LocalDate, Integer> getHeartRateData() {
		return heartRateData;
	}

	public void setHeartRateData(Map<LocalDate, Integer> heartRateData) {
		this.heartRateData = heartRateData;
	}

	public Map<LocalDate, String> getBloodPressureData() {
		return bloodPressureData;
	}

	public void setBloodPressureData(Map<LocalDate, String> bloodPressureData) {
		this.bloodPressureData = bloodPressureData;
	}

	public Map<LocalDate, Double> getOxygenSaturationData() {
		return oxygenSaturationData;
	}

	public void setOxygenSaturationData(Map<LocalDate, Double> oxygenSaturationData) {
		this.oxygenSaturationData = oxygenSaturationData;
	}

	public Double getCurrentBMI() {
		return currentBMI;
	}

	public void setCurrentBMI(Double currentBMI) {
		this.currentBMI = currentBMI;
	}

	private Map<LocalDate, String> bloodPressureData;
    
    private Map<LocalDate, Double> oxygenSaturationData;
    
    private Double currentBMI;
}