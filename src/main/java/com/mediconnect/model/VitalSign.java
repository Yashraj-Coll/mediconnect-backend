package com.mediconnect.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import java.util.Date;

@Entity
@Table(name = "vital_signs")
public class VitalSign {

    public enum Status {
        normal, high, low
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(name = "vital_type")
    private String vitalType;

    @Column(name = "value")
    private String value;

    @Column(name = "reading_date")
    @Temporal(TemporalType.DATE)
    private Date readingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ====== Getters and Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public String getVitalType() { return vitalType; }
    public void setVitalType(String vitalType) { this.vitalType = vitalType; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public Date getReadingDate() { return readingDate; }
    public void setReadingDate(Date readingDate) { this.readingDate = readingDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // Helper method to get patient ID (for backward compatibility)
    public Long getPatientId() {
        return patient != null ? patient.getId() : null;
    }

    // Helper method to get doctor ID (for backward compatibility)
    public Long getDoctorId() {
        return doctor != null ? doctor.getId() : null;
    }
}