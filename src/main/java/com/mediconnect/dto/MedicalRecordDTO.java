package com.mediconnect.dto;

import java.time.LocalDate;
import com.mediconnect.model.MedicalRecord;

public class MedicalRecordDTO {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private String doctorName;
    private String title;
    private String type;
    private String documentPath;
    private LocalDate recordDate;
    private String hospital;
    private String notes;

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }
    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getDocumentPath() {
        return documentPath;
    }
    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }
    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public String getHospital() {
        return hospital;
    }
    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    // --- Entity to DTO Conversion ---
    public static MedicalRecordDTO fromEntity(MedicalRecord entity) {
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setId(entity.getId());
        dto.setPatientId(entity.getPatient() != null ? entity.getPatient().getId() : null);
        dto.setDoctorId(entity.getDoctor() != null ? entity.getDoctor().getId() : null);
        if (entity.getDoctor() != null && entity.getDoctor().getUser() != null) {
            dto.setDoctorName(entity.getDoctor().getUser().getFirstName() + " " + entity.getDoctor().getUser().getLastName());
        }
        dto.setTitle(entity.getTitle());
        dto.setType(entity.getType());
        dto.setDocumentPath(entity.getDocumentPath());
        dto.setRecordDate(entity.getRecordDate());
        dto.setHospital(entity.getHospital());
        dto.setNotes(entity.getNotes());
        return dto;
    }
}
