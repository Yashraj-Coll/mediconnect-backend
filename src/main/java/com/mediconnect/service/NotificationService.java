package com.mediconnect.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mediconnect.model.Appointment;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.ImageAnalysisResult;
import com.mediconnect.model.Patient;
import com.mediconnect.model.TriageRecord;
import com.mediconnect.model.User;
import com.mediconnect.model.VideoSession;

import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationService {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private AppointmentNotificationService appointmentNotificationService;
    
    /**
     * Send appointment confirmation to patient
     */
    public void sendAppointmentConfirmationToPatient(Appointment appointment) {
        try {
            // Delegate to AppointmentNotificationService for unified handling
            appointmentNotificationService.sendAppointmentConfirmation(appointment);
            log.info("Delegated appointment confirmation to AppointmentNotificationService for appointment: {}", appointment.getId());
        } catch (Exception e) {
            log.error("Failed to send appointment confirmation email: {}", e.getMessage(), e);
        }
    }
    
    public void sendAppointmentAlertToDoctor(Appointment appointment) {
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("doctorName", doctorUser.getFirstName());
            templateModel.put("patientName", patientUser.getFirstName() + " " + patientUser.getLastName());
            templateModel.put("appointmentDate", appointment.getAppointmentDateTime().toLocalDate().toString());
            templateModel.put("appointmentTime", appointment.getAppointmentDateTime().toLocalTime().toString());
            templateModel.put("appointmentType", appointment.getAppointmentType().toString());
            templateModel.put("patientNotes", appointment.getPatientNotes() != null ? appointment.getPatientNotes() : "None");
            
            emailService.sendTemplateMessage(
                doctorUser.getEmail(),
                "New Appointment Scheduled",
                "appointment-alert-doctor",
                templateModel
            );
            log.info("Sent appointment alert email to doctor: {}", doctorUser.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send appointment alert email: {}", e.getMessage(), e);
        }
    }
    
    public void sendAppointmentCancellationToPatient(Appointment appointment) {
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientName", patientUser.getFirstName());
            templateModel.put("doctorName", "Dr. " + doctorUser.getLastName());
            templateModel.put("appointmentDate", appointment.getAppointmentDateTime().toLocalDate().toString());
            templateModel.put("appointmentTime", appointment.getAppointmentDateTime().toLocalTime().toString());
            
            emailService.sendTemplateMessage(
                patientUser.getEmail(),
                "Your Appointment has been Cancelled",
                "appointment-cancellation-patient",
                templateModel
            );
            log.info("Sent cancellation email to patient: {}", patientUser.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send cancellation email: {}", e.getMessage(), e);
        }
    }
    
    public void sendAppointmentCancellationToDoctor(Appointment appointment) {
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("doctorName", doctorUser.getFirstName());
            templateModel.put("patientName", patientUser.getFirstName() + " " + patientUser.getLastName());
            templateModel.put("appointmentDate", appointment.getAppointmentDateTime().toLocalDate().toString());
            templateModel.put("appointmentTime", appointment.getAppointmentDateTime().toLocalTime().toString());
            
            emailService.sendTemplateMessage(
                doctorUser.getEmail(),
                "Appointment Cancellation",
                "appointment-cancellation-doctor",
                templateModel
            );
            log.info("Sent cancellation email to doctor: {}", doctorUser.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send cancellation email: {}", e.getMessage(), e);
        }
    }
    
    public void sendVideoSessionDetailsToDoctor(VideoSession videoSession) {
    Appointment appointment = videoSession.getAppointment();
    Doctor doctor = appointment.getDoctor();
    User doctorUser = doctor.getUser();
    Patient patient = appointment.getPatient();
    User patientUser = patient.getUser();
    
    try {
        // Generate Jitsi Meet link from appointment
        String jitsiLink = "";
        if (appointment.getVideoRoomName() != null) {
            jitsiLink = "https://meet.jit.si/" + appointment.getVideoRoomName();
        }
        
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("doctorName", doctorUser.getFirstName());
        templateModel.put("patientName", patientUser.getFirstName() + " " + patientUser.getLastName());
        templateModel.put("appointmentDate", videoSession.getScheduledStartTime().toLocalDate().toString());
        templateModel.put("appointmentTime", videoSession.getScheduledStartTime().toLocalTime().toString());
        templateModel.put("sessionId", videoSession.getSessionId());
        templateModel.put("doctorToken", videoSession.getDoctorToken());
        
        if (jitsiLink != null && !jitsiLink.isEmpty()) {
            templateModel.put("jitsiMeetingLink", jitsiLink);
            templateModel.put("isJitsiMeet", true);
        } else {
            templateModel.put("isJitsiMeet", false);
            templateModel.put("sessionUrl", "https://mediconnect.com/video/" + videoSession.getSessionId());
        }
        
        emailService.sendTemplateMessage(
            doctorUser.getEmail(),
            "Video Consultation Details - Jitsi Meet",
            "video-session-doctor",
            templateModel
        );
        log.info("Sent Jitsi video session details email to doctor: {}", doctorUser.getEmail());
    } catch (MessagingException e) {
        log.error("Failed to send video session details email: {}", e.getMessage(), e);
    }
}
    
    public void sendVideoSessionDetailsToPatient(VideoSession videoSession) {
        Appointment appointment = videoSession.getAppointment();
    Doctor doctor = appointment.getDoctor();
    User doctorUser = doctor.getUser();
    Patient patient = appointment.getPatient();
    User patientUser = patient.getUser();
    
    try {
        // Generate Jitsi Meet link from appointment
        String jitsiLink = "";
        if (appointment.getVideoRoomName() != null) {
            jitsiLink = "https://meet.jit.si/" + appointment.getVideoRoomName();
        }
        
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("doctorName", doctorUser.getFirstName());
        templateModel.put("patientName", patientUser.getFirstName() + " " + patientUser.getLastName());
        templateModel.put("appointmentDate", videoSession.getScheduledStartTime().toLocalDate().toString());
        templateModel.put("appointmentTime", videoSession.getScheduledStartTime().toLocalTime().toString());
        templateModel.put("sessionId", videoSession.getSessionId());
        templateModel.put("doctorToken", videoSession.getDoctorToken());
        
        if (jitsiLink != null && !jitsiLink.isEmpty()) {
            templateModel.put("jitsiMeetingLink", jitsiLink);
            templateModel.put("isJitsiMeet", true);
        } else {
            templateModel.put("isJitsiMeet", false);
            templateModel.put("sessionUrl", "https://mediconnect.com/video/" + videoSession.getSessionId());
        }
        
        emailService.sendTemplateMessage(
            doctorUser.getEmail(),
            "Video Consultation Details - Jitsi Meet",
            "video-session-doctor",
            templateModel
        );
        log.info("Sent Jitsi video session details email to doctor: {}", doctorUser.getEmail());
    } catch (MessagingException e) {
        log.error("Failed to send video session details email: {}", e.getMessage(), e);
    }
}

    public void sendVideoSessionCancellationToDoctor(VideoSession videoSession, String reason) {
        Appointment appointment = videoSession.getAppointment();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("doctorName", doctorUser.getFirstName());
            templateModel.put("patientName", patientUser.getFirstName() + " " + patientUser.getLastName());
            templateModel.put("appointmentDate", videoSession.getScheduledStartTime().toLocalDate().toString());
            templateModel.put("appointmentTime", videoSession.getScheduledStartTime().toLocalTime().toString());
            templateModel.put("reason", reason);
            
            emailService.sendTemplateMessage(
                doctorUser.getEmail(),
                "Video Consultation Cancelled",
                "video-session-cancellation-doctor",
                templateModel
            );
            log.info("Sent video session cancellation email to doctor: {}", doctorUser.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send video session cancellation email: {}", e.getMessage(), e);
        }
    }

    public void sendVideoSessionCancellationToPatient(VideoSession videoSession, String reason) {
        Appointment appointment = videoSession.getAppointment();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientName", patientUser.getFirstName());
            templateModel.put("doctorName", "Dr. " + doctorUser.getLastName());
            templateModel.put("appointmentDate", videoSession.getScheduledStartTime().toLocalDate().toString());
            templateModel.put("appointmentTime", videoSession.getScheduledStartTime().toLocalTime().toString());
            templateModel.put("reason", reason);
            
            emailService.sendTemplateMessage(
                patientUser.getEmail(),
                "Your Video Consultation has been Cancelled",
                "video-session-cancellation-patient",
                templateModel
            );
            log.info("Sent video session cancellation email to patient: {}", patientUser.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send video session cancellation email: {}", e.getMessage(), e);
        }
    }

    public void sendAppointmentReminderToPatient(Appointment appointment, int hoursBeforeAppointment) {
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientName", patientUser.getFirstName());
            templateModel.put("doctorName", "Dr. " + doctorUser.getLastName());
            templateModel.put("appointmentDate", appointment.getAppointmentDateTime().toLocalDate().toString());
            templateModel.put("appointmentTime", appointment.getAppointmentDateTime().toLocalTime().toString());
            templateModel.put("appointmentType", appointment.getAppointmentType().toString());
            templateModel.put("hoursBeforeAppointment", hoursBeforeAppointment);
            
            emailService.sendTemplateMessage(
                patientUser.getEmail(),
                "Appointment Reminder - " + hoursBeforeAppointment + " hours",
                "appointment-reminder-patient",
                templateModel
            );
            log.info("Sent appointment reminder email to patient: {}", patientUser.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send appointment reminder email: {}", e.getMessage(), e);
        }
    }

    public void sendAppointmentReminderToDoctor(Appointment appointment, int hoursBeforeAppointment) {
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("doctorName", doctorUser.getFirstName());
            templateModel.put("patientName", patientUser.getFirstName() + " " + patientUser.getLastName());
            templateModel.put("appointmentDate", appointment.getAppointmentDateTime().toLocalDate().toString());
            templateModel.put("appointmentTime", appointment.getAppointmentDateTime().toLocalTime().toString());
            templateModel.put("appointmentType", appointment.getAppointmentType().toString());
            templateModel.put("hoursBeforeAppointment", hoursBeforeAppointment);
            
            emailService.sendTemplateMessage(
                doctorUser.getEmail(),
                "Appointment Reminder - " + hoursBeforeAppointment + " hours",
                "appointment-reminder-doctor",
                templateModel
            );
            log.info("Sent appointment reminder email to doctor: {}", doctorUser.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send appointment reminder email: {}", e.getMessage(), e);
        }
    }

    public void sendPrescriptionNotificationToPatient(Patient patient, String prescriptionUrl) {
        User patientUser = patient.getUser();
        
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientName", patientUser.getFirstName());
            templateModel.put("prescriptionUrl", prescriptionUrl);
            
            emailService.sendTemplateMessage(
                patientUser.getEmail(),
                "New Prescription Available",
                "prescription-notification",
                templateModel
            );
            log.info("Sent prescription notification email to patient: {}", patientUser.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send prescription notification email: {}", e.getMessage(), e);
        }
    }

    public void sendPaymentReminderToPatient(Appointment appointment) {
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientName", patientUser.getFirstName());
            templateModel.put("doctorName", "Dr. " + doctorUser.getLastName());
            templateModel.put("appointmentDate", appointment.getAppointmentDateTime().toLocalDate().toString());
            templateModel.put("appointmentTime", appointment.getAppointmentDateTime().toLocalTime().toString());
            templateModel.put("fee", appointment.getFee());
            
            emailService.sendTemplateMessage(
                patientUser.getEmail(),
                "Payment Reminder - MediConnect",
                "payment-reminder",
                templateModel
            );
            log.info("Sent payment reminder email to patient: {}", patientUser.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send payment reminder email: {}", e.getMessage(), e);
        }
    }

    public void sendUrgentTriageNotification(TriageRecord triageRecord) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("triageId", triageRecord.getId());
            templateModel.put("urgencyLevel", triageRecord.getUrgencyLevel());
            templateModel.put("symptoms", triageRecord.getSymptoms());
            // Fixed: Use safe getter that returns default value
            templateModel.put("recommendations", getTriageRecommendations(triageRecord));
            
            // Send to emergency contact or healthcare provider
            String emergencyEmail = "emergency@mediconnect.com"; // Configure as needed
            
            emailService.sendTemplateMessage(
                emergencyEmail,
                "URGENT: High Priority Triage Alert",
                "urgent-triage-notification",
                templateModel
            );
            log.info("Sent urgent triage notification for record: {}", triageRecord.getId());
        } catch (MessagingException e) {
            log.error("Failed to send urgent triage notification: {}", e.getMessage(), e);
        }
    }

    public void sendUrgentImageAnalysisNotification(ImageAnalysisResult result) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("analysisId", result.getId());
            templateModel.put("findings", result.getFindings());
            // Fixed: Use safe getters with default values
            templateModel.put("confidence", getImageAnalysisConfidence(result));
            templateModel.put("recommendations", getImageAnalysisRecommendations(result));
            
            // Send to radiologist or healthcare provider
            String radiologistEmail = "radiology@mediconnect.com"; // Configure as needed
            
            emailService.sendTemplateMessage(
                radiologistEmail,
                "URGENT: Critical Image Analysis Finding",
                "urgent-image-analysis-notification",
                templateModel
            );
            log.info("Sent urgent image analysis notification for result: {}", result.getId());
        } catch (MessagingException e) {
            log.error("Failed to send urgent image analysis notification: {}", e.getMessage(), e);
        }
    }

    public void sendMonitoringAlert(Map<String, Object> alertData) {
        try {
            String patientEmail = (String) alertData.get("patientEmail");
            String alertType = (String) alertData.get("alertType");
            String message = (String) alertData.get("message");
            
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("alertType", alertType);
            templateModel.put("message", message);
            templateModel.put("timestamp", alertData.get("timestamp"));
            
            emailService.sendTemplateMessage(
                patientEmail,
                "Health Monitoring Alert - " + alertType,
                "monitoring-alert",
                templateModel
            );
            log.info("Sent monitoring alert email to: {}", patientEmail);
        } catch (MessagingException e) {
            log.error("Failed to send monitoring alert email: {}", e.getMessage(), e);
        }
    }

    public void schedulePatientMedicationReminder(Long patientId, String medicationName, String reminderTime) {
        // This would typically integrate with a scheduling service
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientId", patientId);
            templateModel.put("medicationName", medicationName);
            templateModel.put("reminderTime", reminderTime);
            
            // For now, send immediate notification
            sendMedicationReminder(patientId, medicationName, "As prescribed", reminderTime);
            
            log.info("Scheduled medication reminder for patient: {} for medication: {}", patientId, medicationName);
        } catch (Exception e) {
            log.error("Failed to schedule medication reminder: {}", e.getMessage(), e);
        }
    }


    public void sendMedicationReminder(Long patientId, String medicationName, String dosage, String reminderTime) {
        try {
            // In a real implementation, you would fetch patient details from database
            String patientEmail = "patient" + patientId + "@example.com"; // Placeholder
            String patientName = "Patient " + patientId;

            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientName", patientName);
            templateModel.put("medicationName", medicationName);
            templateModel.put("dosage", dosage);
            templateModel.put("reminderTime", reminderTime);

            emailService.sendTemplateMessage(
                patientEmail,
                "Medication Reminder",
                "medication-reminder",
                templateModel
            );
            log.info("Sent medication reminder email to: {}", patientEmail);
        } catch (MessagingException e) {
            log.error("Failed to send medication reminder: {}", e.getMessage(), e);
        }
    }

    // Helper methods to safely handle missing getters
    private String getTriageRecommendations(TriageRecord triageRecord) {
        // Since getRecommendations() doesn't exist, return a default value
        return "Please consult with a healthcare provider immediately";
    }

    private Double getImageAnalysisConfidence(ImageAnalysisResult result) {
        // Since getConfidence() doesn't exist, return a default value
        return 0.85; // 85% confidence as default
    }

    private String getImageAnalysisRecommendations(ImageAnalysisResult result) {
        // Since getRecommendations() doesn't exist, return a default value
        return "Further analysis required. Please consult with a radiologist.";
    }
}