package com.mediconnect.dto;

import java.util.Date;

import com.mediconnect.model.VitalSign;

public class VitalSignDTO {
    private Long id;
    private Long patientId;
    private String vitalType;
    private String value;
    private Date readingDate;
    private String status;
    private String notes;
	private Long doctorId;
private String doctorName;

    // ======= Getters & Setters =======
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getVitalType() { return vitalType; }
    public void setVitalType(String vitalType) { this.vitalType = vitalType; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public Date getReadingDate() { return readingDate; }
    public void setReadingDate(Date readingDate) { this.readingDate = readingDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
	
	public Long getDoctorId() { return doctorId; }
public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

public String getDoctorName() { return doctorName; }
public void setDoctorName(String doctorName) { this.doctorName = doctorName; }


public static VitalSignDTO fromEntity(VitalSign entity) {
    VitalSignDTO dto = new VitalSignDTO();
    dto.setId(entity.getId());
    dto.setPatientId(entity.getPatient() != null ? entity.getPatient().getId() : null);
    dto.setVitalType(entity.getVitalType());
    dto.setValue(entity.getValue());
    dto.setReadingDate(entity.getReadingDate());
    dto.setStatus(entity.getStatus() != null ? entity.getStatus().toString() : null);
    dto.setNotes(entity.getNotes());
    
    if (entity.getDoctor() != null) {
        dto.setDoctorId(entity.getDoctor().getId());
        if (entity.getDoctor().getUser() != null) {
            dto.setDoctorName("Dr. " + entity.getDoctor().getUser().getFirstName() + " " + 
                           entity.getDoctor().getUser().getLastName());
        }
    }
    
    return dto;
}
}
