package com.mediconnect.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mediconnect.model.Appointment;
import com.mediconnect.model.Doctor;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppointmentDTO {

    private Long id;
    private String appointmentType;
    private String appointmentDateTime;
    private String status;
    private Double fee;
    private String patientNotes;
    private String doctorNotes;
    private Integer durationMinutes;
    private Boolean isPaid;
    private Long patientId;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private String doctorProfileImage;
    private String doctorGender;
    private String doctorHospitalAffiliation;
    private String videoRoomName;

    // === DEFAULT NO-ARGS CONSTRUCTOR ===
    public AppointmentDTO() {
    }

    // === CONSTRUCTOR FROM ENTITY ===
    public AppointmentDTO(Appointment appointment) {
        this.id = appointment.getId();
        this.appointmentType = appointment.getAppointmentType() != null
                ? appointment.getAppointmentType().name() : null;
        this.appointmentDateTime = appointment.getAppointmentDateTime() != null
                ? appointment.getAppointmentDateTime().toString() : null;
        this.status = appointment.getStatus() != null
                ? appointment.getStatus().name() : null;
        this.fee = appointment.getFee();
        this.patientNotes = appointment.getPatientNotes();
        this.doctorNotes = appointment.getDoctorNotes();
        this.durationMinutes = appointment.getDurationMinutes();
        this.isPaid = appointment.getIsPaid() != null ? appointment.getIsPaid() : false;
        this.patientId = appointment.getPatient() != null ? appointment.getPatient().getId() : null;
        this.doctorId = appointment.getDoctor() != null ? appointment.getDoctor().getId() : null;
        Doctor doctor = appointment.getDoctor();
        if (doctor != null) {
            this.doctorName = doctor.getUser() != null ?
                    doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName() : null;
            this.doctorSpecialization = doctor.getSpecialization();
            this.doctorProfileImage = doctor.getProfileImage();
            this.doctorGender = doctor.getGender();
            this.doctorHospitalAffiliation = doctor.getHospitalAffiliation();
            this.videoRoomName = appointment.getVideoRoomName();
        }
        
    }

    // === GETTERS AND SETTERS ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAppointmentType() { return appointmentType; }
    public void setAppointmentType(String appointmentType) { this.appointmentType = appointmentType; }

    public String getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(String appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getFee() { return fee; }
    public void setFee(Double fee) { this.fee = fee; }

    public String getPatientNotes() { return patientNotes; }
    public void setPatientNotes(String patientNotes) { this.patientNotes = patientNotes; }

    public String getDoctorNotes() { return doctorNotes; }
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Boolean getIsPaid() { return isPaid; }
    public void setIsPaid(Boolean isPaid) { this.isPaid = isPaid; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDoctorSpecialization() { return doctorSpecialization; }
    public void setDoctorSpecialization(String doctorSpecialization) { this.doctorSpecialization = doctorSpecialization; }

    public String getDoctorProfileImage() { return doctorProfileImage; }
    public void setDoctorProfileImage(String doctorProfileImage) { this.doctorProfileImage = doctorProfileImage; }

    public String getDoctorGender() { return doctorGender; }
    public void setDoctorGender(String doctorGender) { this.doctorGender = doctorGender; }

    public String getDoctorHospitalAffiliation() { return doctorHospitalAffiliation; }
    public void setDoctorHospitalAffiliation(String doctorHospitalAffiliation) { this.doctorHospitalAffiliation = doctorHospitalAffiliation; }

    public String getVideoRoomName() { 
        return videoRoomName; 
    }

    public void setVideoRoomName(String videoRoomName) { 
        this.videoRoomName = videoRoomName; 
    }

}