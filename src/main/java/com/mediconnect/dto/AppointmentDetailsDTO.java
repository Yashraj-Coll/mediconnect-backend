package com.mediconnect.dto;

import com.mediconnect.model.Appointment;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.Patient;

public class AppointmentDetailsDTO {
    
    // Appointment info
    private Long id;
    private String appointmentType;
    private String appointmentDateTime;
    private String status;
    private Double fee;
    private String patientNotes;
    private String doctorNotes;
    private Integer durationMinutes;
    private Boolean isPaid;
    
    // Doctor info
    private Long doctorId;
    private Long doctorUserId;        // 🚨 CRITICAL: MISSING FIELD ADDED!
    private String doctorName;
    private String doctorSpecialization;
    private String doctorProfileImage;
    private String doctorGender;
    private String doctorHospitalAffiliation;
    private String doctorEmail;
    private String doctorPhone;
    private Double doctorConsultationFee;
    private String doctorBiography;
    private Integer doctorRating;
    
    // Patient info
    private Long patientId;
    private Long patientUserId;       // 🚨 CRITICAL: MISSING FIELD ADDED!
    private String patientName;
    private String patientEmail;
    private String patientPhone;
    private String patientGender;
    private String patientDateOfBirth;
    private String patientBloodGroup;
    private String patientAllergies;
    private String patientProfileImage;
    private String videoRoomName;
    
    // Default constructor
    public AppointmentDetailsDTO() {}
    
    // Constructor from entities
    public AppointmentDetailsDTO(Appointment appointment) {
        // Appointment details
        this.id = appointment.getId();
        this.appointmentType = appointment.getAppointmentType() != null ? 
            appointment.getAppointmentType().name() : null;
        this.appointmentDateTime = appointment.getAppointmentDateTime() != null ? 
            appointment.getAppointmentDateTime().toString() : null;
        this.status = appointment.getStatus() != null ? 
            appointment.getStatus().name() : null;
        this.fee = appointment.getFee();
        this.patientNotes = appointment.getPatientNotes();
        this.doctorNotes = appointment.getDoctorNotes();
        this.durationMinutes = appointment.getDurationMinutes();
        this.isPaid = appointment.getIsPaid();
        this.videoRoomName = appointment.getVideoRoomName();
        
        // Doctor details
        Doctor doctor = appointment.getDoctor();
        if (doctor != null) {
            this.doctorId = doctor.getId();
            this.doctorSpecialization = doctor.getSpecialization();
            this.doctorProfileImage = doctor.getProfileImage();
            this.doctorGender = doctor.getGender();
            this.doctorHospitalAffiliation = doctor.getHospitalAffiliation();
            this.doctorConsultationFee = doctor.getConsultationFee();
            this.doctorBiography = doctor.getBiography();
            this.doctorRating = doctor.getAverageRating();
            
            if (doctor.getUser() != null) {
                this.doctorUserId = doctor.getUser().getId(); // 🔥 CRITICAL FIX!
                this.doctorName = (doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName()).trim();
                this.doctorEmail = doctor.getUser().getEmail();
                this.doctorPhone = doctor.getUser().getPhoneNumber();
            }
        }
        
        // Patient details
        Patient patient = appointment.getPatient();
        if (patient != null) {
            this.patientId = patient.getId();
            this.patientGender = patient.getGender() != null ? patient.getGender().name() : null;
            this.patientDateOfBirth = patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : null;
            this.patientBloodGroup = patient.getBloodGroup();
            this.patientAllergies = patient.getAllergies();
            this.patientProfileImage = patient.getProfileImage();
            
            if (patient.getUser() != null) {
                this.patientUserId = patient.getUser().getId(); // 🔥 CRITICAL FIX!
                this.patientName = (patient.getUser().getFirstName() + " " + patient.getUser().getLastName()).trim();
                this.patientEmail = patient.getUser().getEmail();
                this.patientPhone = patient.getUser().getPhoneNumber();
            }
        }
    }
    
    // Getters and Setters
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
    
    // Doctor getters/setters
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    
    // 🚨 CRITICAL: Missing getter/setter for doctorUserId
    public Long getDoctorUserId() { return doctorUserId; }
    public void setDoctorUserId(Long doctorUserId) { this.doctorUserId = doctorUserId; }
    
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
    
    public String getDoctorEmail() { return doctorEmail; }
    public void setDoctorEmail(String doctorEmail) { this.doctorEmail = doctorEmail; }
    
    public String getDoctorPhone() { return doctorPhone; }
    public void setDoctorPhone(String doctorPhone) { this.doctorPhone = doctorPhone; }
    
    public Double getDoctorConsultationFee() { return doctorConsultationFee; }
    public void setDoctorConsultationFee(Double doctorConsultationFee) { this.doctorConsultationFee = doctorConsultationFee; }
    
    public String getDoctorBiography() { return doctorBiography; }
    public void setDoctorBiography(String doctorBiography) { this.doctorBiography = doctorBiography; }
    
    public Integer getDoctorRating() { return doctorRating; }
    public void setDoctorRating(Integer doctorRating) { this.doctorRating = doctorRating; }
    
    // Patient getters/setters
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    
    // 🚨 CRITICAL: Missing getter/setter for patientUserId
    public Long getPatientUserId() { return patientUserId; }
    public void setPatientUserId(Long patientUserId) { this.patientUserId = patientUserId; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    
    public String getPatientEmail() { return patientEmail; }
    public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }
    
    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }
    
    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }
    
    public String getPatientDateOfBirth() { return patientDateOfBirth; }
    public void setPatientDateOfBirth(String patientDateOfBirth) { this.patientDateOfBirth = patientDateOfBirth; }
    
    public String getPatientBloodGroup() { return patientBloodGroup; }
    public void setPatientBloodGroup(String patientBloodGroup) { this.patientBloodGroup = patientBloodGroup; }
    
    public String getPatientAllergies() { return patientAllergies; }
    public void setPatientAllergies(String patientAllergies) { this.patientAllergies = patientAllergies; }
    
    public String getPatientProfileImage() { return patientProfileImage; }
    public void setPatientProfileImage(String patientProfileImage) { this.patientProfileImage = patientProfileImage; }

    public String getVideoRoomName() { 
        return videoRoomName; 
    }

    public void setVideoRoomName(String videoRoomName) { 
        this.videoRoomName = videoRoomName; 
    }

    @Override
    public String toString() {
        return "AppointmentDetailsDTO{" +
                "id=" + id +
                ", doctorUserId=" + doctorUserId +
                ", patientUserId=" + patientUserId +
                ", doctorName='" + doctorName + '\'' +
                ", patientName='" + patientName + '\'' +
                ", appointmentType='" + appointmentType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}