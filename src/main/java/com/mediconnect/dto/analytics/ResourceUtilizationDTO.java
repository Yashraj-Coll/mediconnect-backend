package com.mediconnect.dto.analytics;

import java.util.Map;

import lombok.Data;

@Data
public class ResourceUtilizationDTO {
    
    private double totalAppointmentHours;
    
    public double getTotalAppointmentHours() {
		return totalAppointmentHours;
	}

	public void setTotalAppointmentHours(double totalAppointmentHours) {
		this.totalAppointmentHours = totalAppointmentHours;
	}

	public Map<Integer, Long> getAppointmentsByHourOfDay() {
		return appointmentsByHourOfDay;
	}

	public void setAppointmentsByHourOfDay(Map<Integer, Long> appointmentsByHourOfDay) {
		this.appointmentsByHourOfDay = appointmentsByHourOfDay;
	}

	public Map<Integer, Long> getAppointmentsByDayOfWeek() {
		return appointmentsByDayOfWeek;
	}

	public void setAppointmentsByDayOfWeek(Map<Integer, Long> appointmentsByDayOfWeek) {
		this.appointmentsByDayOfWeek = appointmentsByDayOfWeek;
	}

	private Map<Integer, Long> appointmentsByHourOfDay;
    
    private Map<Integer, Long> appointmentsByDayOfWeek;
}