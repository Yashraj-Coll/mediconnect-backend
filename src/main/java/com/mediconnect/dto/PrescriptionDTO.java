package com.mediconnect.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PrescriptionDTO {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private Long patientId;
    private String patientName;
    private Long appointmentId;
    private LocalDateTime prescriptionDate;
    private LocalDateTime validUntil;
    private Boolean isRefillable;
    private Integer refillCount;
    private String specialInstructions;
    private String notes;
    private Boolean isDigitallySigned;
    private List<PrescriptionItemDTO> prescriptionItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public LocalDateTime getPrescriptionDate() { return prescriptionDate; }
    public void setPrescriptionDate(LocalDateTime prescriptionDate) { this.prescriptionDate = prescriptionDate; }

    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }

    public Boolean getIsRefillable() { return isRefillable; }
    public void setIsRefillable(Boolean isRefillable) { this.isRefillable = isRefillable; }

    public Integer getRefillCount() { return refillCount; }
    public void setRefillCount(Integer refillCount) { this.refillCount = refillCount; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getIsDigitallySigned() { return isDigitallySigned; }
    public void setIsDigitallySigned(Boolean isDigitallySigned) { this.isDigitallySigned = isDigitallySigned; }

    public List<PrescriptionItemDTO> getPrescriptionItems() { return prescriptionItems; }
    public void setPrescriptionItems(List<PrescriptionItemDTO> prescriptionItems) { this.prescriptionItems = prescriptionItems; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
