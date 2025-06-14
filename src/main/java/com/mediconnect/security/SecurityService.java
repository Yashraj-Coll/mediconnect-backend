package com.mediconnect.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.mediconnect.model.Appointment;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.MedicalRecord;
import com.mediconnect.model.Patient;
import com.mediconnect.model.VideoSession;
import com.mediconnect.model.AiDiagnosisResult;
import com.mediconnect.repository.AiDiagnosisResultRepository;
import com.mediconnect.repository.AppointmentRepository;
import com.mediconnect.repository.DoctorRepository;
import com.mediconnect.repository.MedicalRecordRepository;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.repository.VideoSessionRepository;

@Service
public class SecurityService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private VideoSessionRepository videoSessionRepository;
    
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    
    @Autowired
    private AiDiagnosisResultRepository aiDiagnosisResultRepository;
    
    /**
     * Check if the current user is the doctor associated with the given ID
     */
    public boolean isDoctorWithId(Long doctorId) {
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (userDetails == null) return false;

        Optional<Doctor> doctor = doctorRepository.findById(doctorId);
        return doctor.isPresent()
            && doctor.get().getUser() != null
            && doctor.get().getUser().getId() != null
            && doctor.get().getUser().getId().equals(userDetails.getId());
    }
    
    /**
     * Check if the current user is the patient associated with the given ID
     */
    public boolean isPatientWithId(Long patientId) {
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (userDetails == null) return false;

        Optional<Patient> patient = patientRepository.findById(patientId);
        return patient.isPresent()
            && patient.get().getUser() != null
            && patient.get().getUser().getId() != null
            && patient.get().getUser().getId().equals(userDetails.getId());
    }
    
    /**
     * Check if the current user is the owner of a specific entity
     */
    public boolean isOwner(Long entityId) {
        // This is a placeholder for a more generic ownership check
        // In a real application, this would check if the user owns the referenced entity
        return true;
    }
    
    /**
     * Check if the current user is the same as the given user ID
     */
    public boolean isCurrentUser(Long userId) {
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (userDetails == null) return false;

        return userDetails.getId() != null && userDetails.getId().equals(userId);
    }
    
    /**
     * Check if the current user can access the specified appointment
     */
    public boolean canAccessAppointment(Long appointmentId) {
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (userDetails == null) return false;

        Optional<Appointment> appointment = appointmentRepository.findById(appointmentId);
        if (!appointment.isPresent()) return false;

        Long userId = userDetails.getId();
        Appointment appt = appointment.get();

        // Null safe checks
        boolean doctorCheck = appt.getDoctor() != null
            && appt.getDoctor().getUser() != null
            && appt.getDoctor().getUser().getId() != null
            && appt.getDoctor().getUser().getId().equals(userId);

        boolean patientCheck = appt.getPatient() != null
            && appt.getPatient().getUser() != null
            && appt.getPatient().getUser().getId() != null
            && appt.getPatient().getUser().getId().equals(userId);

        return doctorCheck || patientCheck;
    }
    
    /**
     * Check if the current user can access the specified video session
     */
    public boolean canAccessVideoSession(Long videoSessionId) {
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (userDetails == null) return false;

        Optional<VideoSession> session = videoSessionRepository.findById(videoSessionId);
        if (!session.isPresent() || session.get().getAppointment() == null) return false;

        return canAccessAppointment(session.get().getAppointment().getId());
    }
    
    /**
     * Check if the current user can access the specified video session by session ID
     */
    public boolean canAccessVideoSessionBySessionId(String sessionId) {
        Optional<VideoSession> session = videoSessionRepository.findBySessionId(sessionId);
        if (!session.isPresent()) return false;

        return canAccessVideoSession(session.get().getId());
    }
    
    /**
     * Check if the current user is the doctor for the specified video session
     */
    public boolean isDoctorForVideoSession(Long videoSessionId) {
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (userDetails == null) return false;

        Optional<VideoSession> session = videoSessionRepository.findById(videoSessionId);
        if (!session.isPresent() || session.get().getAppointment() == null) return false;

        Long userId = userDetails.getId();
        Appointment appt = session.get().getAppointment();

        return appt.getDoctor() != null
            && appt.getDoctor().getUser() != null
            && appt.getDoctor().getUser().getId() != null
            && appt.getDoctor().getUser().getId().equals(userId);
    }
    
    /**
     * Check if the current user can access the specified medical record
     */
    public boolean canAccessMedicalRecord(Long medicalRecordId) {
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (userDetails == null) return false;

        Optional<MedicalRecord> record = medicalRecordRepository.findById(medicalRecordId);
        if (!record.isPresent()) return false;

        Long userId = userDetails.getId();
        MedicalRecord medicalRecord = record.get();

        boolean doctorCheck = medicalRecord.getDoctor() != null
            && medicalRecord.getDoctor().getUser() != null
            && medicalRecord.getDoctor().getUser().getId() != null
            && medicalRecord.getDoctor().getUser().getId().equals(userId);

        boolean patientCheck = medicalRecord.getPatient() != null
            && medicalRecord.getPatient().getUser() != null
            && medicalRecord.getPatient().getUser().getId() != null
            && medicalRecord.getPatient().getUser().getId().equals(userId);

        return doctorCheck || patientCheck;
    }
    
    /**
     * Check if the current user can access the specified AI diagnosis
     */
    public boolean canAccessAiDiagnosis(Long aiDiagnosisId) {
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (userDetails == null) return false;

        Optional<AiDiagnosisResult> diagnosis = aiDiagnosisResultRepository.findById(aiDiagnosisId);
        if (!diagnosis.isPresent() || diagnosis.get().getMedicalRecord() == null) return false;

        return canAccessMedicalRecord(diagnosis.get().getMedicalRecord().getId());
    }
    
    /**
     * Check if the current user is the doctor for the specified AI diagnosis
     */
    public boolean isDoctorForAiDiagnosis(Long aiDiagnosisId) {
        UserDetailsImpl userDetails = getCurrentUserDetails();
        if (userDetails == null) return false;

        Optional<AiDiagnosisResult> diagnosis = aiDiagnosisResultRepository.findById(aiDiagnosisId);
        if (!diagnosis.isPresent() || diagnosis.get().getMedicalRecord() == null || diagnosis.get().getMedicalRecord().getDoctor() == null) return false;

        Long userId = userDetails.getId();
        return diagnosis.get().getMedicalRecord().getDoctor().getUser() != null
            && diagnosis.get().getMedicalRecord().getDoctor().getUser().getId() != null
            && diagnosis.get().getMedicalRecord().getDoctor().getUser().getId().equals(userId);
    }
    
    /**
     * Get the current authenticated user details
     */
    private UserDetailsImpl getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return (UserDetailsImpl) principal;
        }
        return null;
    }
}
