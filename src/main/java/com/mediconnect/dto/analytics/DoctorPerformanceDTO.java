package com.mediconnect.dto.analytics;

import java.util.Map;

import com.mediconnect.model.Appointment;

import lombok.Data;

@Data
public class DoctorPerformanceDTO {
    
    private Long doctorId;
    
    private int totalAppointments;
    
    public Long getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(Long doctorId) {
		this.doctorId = doctorId;
	}

	public int getTotalAppointments() {
		return totalAppointments;
	}

	public void setTotalAppointments(int totalAppointments) {
		this.totalAppointments = totalAppointments;
	}

	public Long getCompletedAppointments() {
		return completedAppointments;
	}

	public void setCompletedAppointments(Long completedAppointments) {
		this.completedAppointments = completedAppointments;
	}

	public Long getCancelledAppointments() {
		return cancelledAppointments;
	}

	public void setCancelledAppointments(Long cancelledAppointments) {
		this.cancelledAppointments = cancelledAppointments;
	}

	public Long getNoShowAppointments() {
		return noShowAppointments;
	}

	public void setNoShowAppointments(Long noShowAppointments) {
		this.noShowAppointments = noShowAppointments;
	}

	public Map<Appointment.AppointmentType, Long> getAppointmentTypeDistribution() {
		return appointmentTypeDistribution;
	}

	public void setAppointmentTypeDistribution(Map<Appointment.AppointmentType, Long> appointmentTypeDistribution) {
		this.appointmentTypeDistribution = appointmentTypeDistribution;
	}

	public double getAverageAppointmentDuration() {
		return averageAppointmentDuration;
	}

	public void setAverageAppointmentDuration(double averageAppointmentDuration) {
		this.averageAppointmentDuration = averageAppointmentDuration;
	}

	private Long completedAppointments;
    
    private Long cancelledAppointments;
    
    private Long noShowAppointments;
    
    private Map<Appointment.AppointmentType, Long> appointmentTypeDistribution;
    
    private double averageAppointmentDuration;
}