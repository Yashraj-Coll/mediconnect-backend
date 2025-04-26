package com.mediconnect.dto;

import java.time.LocalDateTime;

import com.mediconnect.model.Appointment.AppointmentStatus;
import com.mediconnect.model.Appointment.AppointmentType;

public class AppointmentSearchDTO {
    
    private Long doctorId;
    private Long patientId;
    private AppointmentStatus status;
    private AppointmentType type;
    private Boolean isPaid;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String doctorName;
    private String patientName;
    private String specialization;
    private Integer minDuration;
    private Integer maxDuration;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "appointmentDateTime";
    private String sortDirection = "ASC";
    
    // Getters and setters
    public Long getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
    
    public Long getPatientId() {
        return patientId;
    }
    
    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }
    
    public AppointmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
    
    public AppointmentType getType() {
        return type;
    }
    
    public void setType(AppointmentType type) {
        this.type = type;
    }
    
    public Boolean getIsPaid() {
        return isPaid;
    }
    
    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public String getDoctorName() {
        return doctorName;
    }
    
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
    
    public String getPatientName() {
        return patientName;
    }
    
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    
    public String getSpecialization() {
        return specialization;
    }
    
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
    
    public Integer getMinDuration() {
        return minDuration;
    }
    
    public void setMinDuration(Integer minDuration) {
        this.minDuration = minDuration;
    }
    
    public Integer getMaxDuration() {
        return maxDuration;
    }
    
    public void setMaxDuration(Integer maxDuration) {
        this.maxDuration = maxDuration;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortDirection() {
        return sortDirection;
    }
    
    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}