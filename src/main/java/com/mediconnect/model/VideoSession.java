package com.mediconnect.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "video_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoSession {
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Appointment getAppointment() {
		return appointment;
	}

	public void setAppointment(Appointment appointment) {
		this.appointment = appointment;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getDoctorToken() {
		return doctorToken;
	}

	public void setDoctorToken(String doctorToken) {
		this.doctorToken = doctorToken;
	}

	public String getPatientToken() {
		return patientToken;
	}

	public void setPatientToken(String patientToken) {
		this.patientToken = patientToken;
	}

	public SessionStatus getStatus() {
		return status;
	}

	public void setStatus(SessionStatus status) {
		this.status = status;
	}

	public LocalDateTime getScheduledStartTime() {
		return scheduledStartTime;
	}

	public void setScheduledStartTime(LocalDateTime scheduledStartTime) {
		this.scheduledStartTime = scheduledStartTime;
	}

	public LocalDateTime getActualStartTime() {
		return actualStartTime;
	}

	public void setActualStartTime(LocalDateTime actualStartTime) {
		this.actualStartTime = actualStartTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public Integer getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(Integer durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	public String getSessionNotes() {
		return sessionNotes;
	}

	public void setSessionNotes(String sessionNotes) {
		this.sessionNotes = sessionNotes;
	}

	public Boolean getRecordingEnabled() {
		return recordingEnabled;
	}

	public void setRecordingEnabled(Boolean recordingEnabled) {
		this.recordingEnabled = recordingEnabled;
	}

	public String getRecordingUrl() {
		return recordingUrl;
	}

	public void setRecordingUrl(String recordingUrl) {
		this.recordingUrl = recordingUrl;
	}

	public String getTechnicalIssues() {
		return technicalIssues;
	}

	public void setTechnicalIssues(String technicalIssues) {
		this.technicalIssues = technicalIssues;
	}

	public Integer getConnectionQuality() {
		return connectionQuality;
	}

	public void setConnectionQuality(Integer connectionQuality) {
		this.connectionQuality = connectionQuality;
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
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;
    
    @NotNull
    private String sessionId;
    
    private String doctorToken;
    
    private String patientToken;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SessionStatus status;
    
    private LocalDateTime scheduledStartTime;
    
    private LocalDateTime actualStartTime;
    
    private LocalDateTime endTime;
    
    private Integer durationMinutes;
    
    @Column(columnDefinition = "TEXT")
    private String sessionNotes;
    
    private Boolean recordingEnabled;
    
    private String recordingUrl;
    
    @Column(columnDefinition = "TEXT")
    private String technicalIssues;
    
    private Integer connectionQuality;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = SessionStatus.SCHEDULED;
        }
        if (recordingEnabled == null) {
            recordingEnabled = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum SessionStatus {
        SCHEDULED, ONGOING, COMPLETED, CANCELLED, FAILED
    }
}