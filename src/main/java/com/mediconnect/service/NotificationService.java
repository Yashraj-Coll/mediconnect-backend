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

@Service
public class NotificationService {
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Send appointment confirmation to patient
     */
    public void sendAppointmentConfirmationToPatient(Appointment appointment) {
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        
        try {
            // Prepare email content
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientName", patientUser.getFirstName());
            templateModel.put("doctorName", "Dr. " + doctorUser.getLastName());
            templateModel.put("appointmentDate", appointment.getAppointmentDateTime().toLocalDate().toString());
            templateModel.put("appointmentTime", appointment.getAppointmentDateTime().toLocalTime().toString());
            templateModel.put("appointmentType", appointment.getAppointmentType().toString());
            templateModel.put("appointmentDuration", appointment.getDurationMinutes() + " minutes");
            templateModel.put("appointmentNotes", appointment.getPatientNotes() != null ? appointment.getPatientNotes() : "None");
            
            // Send email using template
            emailService.sendTemplateMessage(
                patientUser.getEmail(),
                "Your Appointment is Confirmed",
                "appointment-confirmation",
                templateModel
            );
            
            // Log for debugging
            System.out.println("Sent appointment confirmation email to: " + patientUser.getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send appointment confirmation email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send appointment alert to doctor
     */
    public void sendAppointmentAlertToDoctor(Appointment appointment) {
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        
        try {
            // Prepare email content
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("doctorName", doctorUser.getFirstName());
            templateModel.put("patientName", patientUser.getFirstName() + " " + patientUser.getLastName());
            templateModel.put("appointmentDate", appointment.getAppointmentDateTime().toLocalDate().toString());
            templateModel.put("appointmentTime", appointment.getAppointmentDateTime().toLocalTime().toString());
            templateModel.put("appointmentType", appointment.getAppointmentType().toString());
            templateModel.put("patientNotes", appointment.getPatientNotes() != null ? appointment.getPatientNotes() : "None");
            
            // Send email using template
            emailService.sendTemplateMessage(
                doctorUser.getEmail(),
                "New Appointment Scheduled",
                "appointment-alert-doctor",
                templateModel
            );
            
            // Log for debugging
            System.out.println("Sent appointment alert email to doctor: " + doctorUser.getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send appointment alert email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send appointment cancellation to patient
     */
    public void sendAppointmentCancellationToPatient(Appointment appointment) {
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        
        try {
            // Prepare email content
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientName", patientUser.getFirstName());
            templateModel.put("doctorName", "Dr. " + doctorUser.getLastName());
            templateModel.put("appointmentDate", appointment.getAppointmentDateTime().toLocalDate().toString());
            templateModel.put("appointmentTime", appointment.getAppointmentDateTime().toLocalTime().toString());
            
            // Send email using template
            emailService.sendTemplateMessage(
                patientUser.getEmail(),
                "Your Appointment has been Cancelled",
                "appointment-cancellation-patient",
                templateModel
            );
            
            // Log for debugging
            System.out.println("Sent cancellation email to patient: " + patientUser.getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send cancellation email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send appointment cancellation to doctor
     */
    public void sendAppointmentCancellationToDoctor(Appointment appointment) {
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        
        try {
            // Prepare email content
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("doctorName", doctorUser.getFirstName());
            templateModel.put("patientName", patientUser.getFirstName() + " " + patientUser.getLastName());
            templateModel.put("appointmentDate", appointment.getAppointmentDateTime().toLocalDate().toString());
            templateModel.put("appointmentTime", appointment.getAppointmentDateTime().toLocalTime().toString());
            
            // Send email using template
            emailService.sendTemplateMessage(
                doctorUser.getEmail(),
                "Appointment Cancellation",
                "appointment-cancellation-doctor",
                templateModel
            );
            
            // Log for debugging
            System.out.println("Sent cancellation email to doctor: " + doctorUser.getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send cancellation email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send video session details to doctor
     */
    public void sendVideoSessionDetailsToDoctor(VideoSession videoSession) {
        Appointment appointment = videoSession.getAppointment();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        
        try {
            // Extract Google Meet link if available
            String meetLink = extractMeetLink(videoSession.getSessionNotes());
            
            // Prepare email content
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("doctorName", doctorUser.getFirstName());
            templateModel.put("patientName", patientUser.getFirstName() + " " + patientUser.getLastName());
            templateModel.put("appointmentDate", videoSession.getScheduledStartTime().toLocalDate().toString());
            templateModel.put("appointmentTime", videoSession.getScheduledStartTime().toLocalTime().toString());
            templateModel.put("sessionId", videoSession.getSessionId());
            templateModel.put("doctorToken", videoSession.getDoctorToken());
            
            // Add Google Meet link if available
            if (meetLink != null) {
                templateModel.put("meetLink", meetLink);
                templateModel.put("isGoogleMeet", true);
            } else {
                templateModel.put("isGoogleMeet", false);
                templateModel.put("sessionUrl", "https://mediconnect.com/video/" + videoSession.getSessionId());
            }
            
            // Send email using template
            emailService.sendTemplateMessage(
                doctorUser.getEmail(),
                "Video Consultation Details",
                "video-session-doctor",
                templateModel
            );
            
            // Log for debugging
            System.out.println("Sent video session details email to doctor: " + doctorUser.getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send video session details email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send video session details to patient
     */
    public void sendVideoSessionDetailsToPatient(VideoSession videoSession) {
        Appointment appointment = videoSession.getAppointment();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        
        try {
            // Extract Google Meet link if available
            String meetLink = extractMeetLink(videoSession.getSessionNotes());
            
            // Prepare email content
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientName", patientUser.getFirstName());
            templateModel.put("doctorName", "Dr. " + doctorUser.getLastName());
            templateModel.put("appointmentDate", videoSession.getScheduledStartTime().toLocalDate().toString());
            templateModel.put("appointmentTime", videoSession.getScheduledStartTime().toLocalTime().toString());
            templateModel.put("sessionId", videoSession.getSessionId());
            templateModel.put("patientToken", videoSession.getPatientToken());
            
            // Add Google Meet link if available
            if (meetLink != null) {
                templateModel.put("meetLink", meetLink);
                templateModel.put("isGoogleMeet", true);
            } else {
                templateModel.put("isGoogleMeet", false);
                templateModel.put("sessionUrl", "https://mediconnect.com/video/" + videoSession.getSessionId());
            }
            
            // Send email using template
            emailService.sendTemplateMessage(
                patientUser.getEmail(),
                "Your Video Consultation Link",
                "video-session-patient",
                templateModel
            );
            
            // Log for debugging
            System.out.println("Sent video session details email to patient: " + patientUser.getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send video session details email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send video session cancellation to doctor
     */
    public void sendVideoSessionCancellationToDoctor(VideoSession videoSession, String reason) {
        Appointment appointment = videoSession.getAppointment();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        
        try {
            // Prepare email content
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("doctorName", doctorUser.getFirstName());
            templateModel.put("patientName", patientUser.getFirstName() + " " + patientUser.getLastName());
            templateModel.put("appointmentDate", videoSession.getScheduledStartTime().toLocalDate().toString());
            templateModel.put("appointmentTime", videoSession.getScheduledStartTime().toLocalTime().toString());
            templateModel.put("reason", reason);
            
            // Send email using template
            emailService.sendTemplateMessage(
                doctorUser.getEmail(),
                "Video Consultation Cancelled",
                "video-session-cancellation-doctor",
                templateModel
            );
            
            // Log for debugging
            System.out.println("Sent video session cancellation email to doctor: " + doctorUser.getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send video session cancellation email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send video session cancellation to patient
     */
    public void sendVideoSessionCancellationToPatient(VideoSession videoSession, String reason) {
        Appointment appointment = videoSession.getAppointment();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        
        try {
            // Prepare email content
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientName", patientUser.getFirstName());
            templateModel.put("doctorName", "Dr. " + doctorUser.getLastName());
            templateModel.put("appointmentDate", videoSession.getScheduledStartTime().toLocalDate().toString());
            templateModel.put("appointmentTime", videoSession.getScheduledStartTime().toLocalTime().toString());
            templateModel.put("reason", reason);
            
            // Send email using template
            emailService.sendTemplateMessage(
                patientUser.getEmail(),
                "Your Video Consultation has been Cancelled",
                "video-session-cancellation-patient",
                templateModel
            );
            
            // Log for debugging
            System.out.println("Sent video session cancellation email to patient: " + patientUser.getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send video session cancellation email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send reminder for upcoming appointment to patient
     */
    public void sendAppointmentReminderToPatient(Appointment appointment, int hoursBeforeAppointment) {
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        
        try {
            // Prepare email content
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientName", patientUser.getFirstName());
            templateModel.put("doctorName", "Dr. " + doctorUser.getLastName());
            templateModel.put("appointmentDate", appointment.getAppointmentDateTime().toLocalDate().toString());
            templateModel.put("appointmentTime", appointment.getAppointmentDateTime().toLocalTime().toString());
            templateModel.put("hoursRemaining", hoursBeforeAppointment);
            templateModel.put("appointmentType", appointment.getAppointmentType().toString());
            
            // Send email using template
            emailService.sendTemplateMessage(
                patientUser.getEmail(),
                "Upcoming Appointment Reminder",
                "appointment-reminder-patient",
                templateModel
            );
            
            // Log for debugging
            System.out.println("Sent appointment reminder email to patient: " + patientUser.getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send appointment reminder email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send reminder for upcoming appointment to doctor
     */
    public void sendAppointmentReminderToDoctor(Appointment appointment, int hoursBeforeAppointment) {
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        
        try {
            // Prepare email content
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("doctorName", doctorUser.getFirstName());
            templateModel.put("patientName", patientUser.getFirstName() + " " + patientUser.getLastName());
            templateModel.put("appointmentDate", appointment.getAppointmentDateTime().toLocalDate().toString());
            templateModel.put("appointmentTime", appointment.getAppointmentDateTime().toLocalTime().toString());
            templateModel.put("hoursRemaining", hoursBeforeAppointment);
            templateModel.put("appointmentType", appointment.getAppointmentType().toString());
            
            // Send email using template
            emailService.sendTemplateMessage(
                doctorUser.getEmail(),
                "Upcoming Appointment Reminder",
                "appointment-reminder-doctor",
                templateModel
            );
            
            // Log for debugging
            System.out.println("Sent appointment reminder email to doctor: " + doctorUser.getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send appointment reminder email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send prescription notification to patient
     */
    public void sendPrescriptionNotificationToPatient(Patient patient, String prescriptionUrl) {
        User patientUser = patient.getUser();
        
        try {
            // Prepare email content
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientName", patientUser.getFirstName());
            templateModel.put("prescriptionUrl", prescriptionUrl);
            
            // Send email using template
            emailService.sendTemplateMessage(
                patientUser.getEmail(),
                "Your New Prescription",
                "prescription-notification",
                templateModel
            );
            
            // Log for debugging
            System.out.println("Sent prescription notification email to patient: " + patientUser.getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send prescription notification email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send payment reminder to patient
     */
    public void sendPaymentReminderToPatient(Appointment appointment) {
        Patient patient = appointment.getPatient();
        User patientUser = patient.getUser();
        Doctor doctor = appointment.getDoctor();
        User doctorUser = doctor.getUser();
        
        try {
            // Prepare email content
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientName", patientUser.getFirstName());
            templateModel.put("doctorName", "Dr. " + doctorUser.getLastName());
            templateModel.put("appointmentDate", appointment.getAppointmentDateTime().toLocalDate().toString());
            templateModel.put("appointmentTime", appointment.getAppointmentDateTime().toLocalTime().toString());
            templateModel.put("amount", appointment.getFee());
            templateModel.put("paymentUrl", "https://mediconnect.com/payments/" + appointment.getId());
            
            // Send email using template
            emailService.sendTemplateMessage(
                patientUser.getEmail(),
                "Payment Reminder for Your Appointment",
                "payment-reminder",
                templateModel
            );
            
            // Log for debugging
            System.out.println("Sent payment reminder email to patient: " + patientUser.getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send payment reminder email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send notification for urgent triage cases
     */
    public void sendUrgentTriageNotification(TriageRecord triageRecord) {
        Patient patient = triageRecord.getPatient();
        User patientUser = patient.getUser();
        
        try {
            // Prepare urgent email content
            String subject = "URGENT: Patient Triage Alert";
            String content = String.format(
                "<h2>URGENT TRIAGE NOTIFICATION</h2>" +
                "<p><strong>Patient:</strong> %s %s</p>" +
                "<p><strong>Urgency Level:</strong> %s</p>" +
                "<p><strong>Symptoms:</strong> %s</p>" +
                "<p><strong>Recommended Action:</strong> %s</p>",
                patientUser.getFirstName(), patientUser.getLastName(),
                triageRecord.getUrgencyLevel(),
                triageRecord.getSymptoms(),
                triageRecord.getRecommendedAction()
            );
            
            // Send to emergency staff email
            emailService.sendSimpleMessage("emergency@mediconnect.com", subject, content);
            
            // Log for debugging
            System.out.println("Sent urgent triage notification email");
        } catch (Exception e) {
            System.err.println("Failed to send urgent triage notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send notification for urgent image analysis results
     */
    public void sendUrgentImageAnalysisNotification(ImageAnalysisResult result) {
        // Get patient information
        Patient patient = result.getMedicalRecord().getPatient();
        User patientUser = patient.getUser();
        
        try {
            // Prepare urgent email content
            String subject = "URGENT: Image Analysis Alert";
            String content = String.format(
                "<h2>URGENT IMAGE ANALYSIS NOTIFICATION</h2>" +
                "<p><strong>Patient:</strong> %s %s</p>" +
                "<p><strong>Image Type:</strong> %s</p>" +
                "<p><strong>Findings:</strong> %s</p>" +
                "<p><strong>Confidence Score:</strong> %.2f</p>",
                patientUser.getFirstName(), patientUser.getLastName(),
                result.getImageType(),
                result.getFindings(),
                result.getConfidenceScore()
            );
            
            // Send to relevant doctor and medical staff
            emailService.sendSimpleMessage("medicalstaff@mediconnect.com", subject, content);
            
            // Log for debugging
            System.out.println("Sent urgent image analysis notification email");
        } catch (Exception e) {
            System.err.println("Failed to send urgent image analysis notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send alert for monitoring device readings that trigger alert rules
     */
    public void sendMonitoringAlert(Map<String, Object> alertData) {
        try {
            // Prepare monitoring alert email
            String subject = "Patient Monitoring Alert";
            String content = String.format(
                "<h2>MONITORING ALERT</h2>" +
                "<p><strong>Patient ID:</strong> %s</p>" +
                "<p><strong>Reading Type:</strong> %s</p>" +
                "<p><strong>Value:</strong> %s %s</p>" +
                "<p><strong>Alert Level:</strong> %s</p>" +
                "<p><strong>Rule Name:</strong> %s</p>" +
                "<p><strong>Timestamp:</strong> %s</p>",
                alertData.get("patientId"),
                alertData.get("readingType"),
                alertData.get("value"), alertData.get("unit"),
                alertData.get("alertLevel"),
                alertData.get("ruleName"),
                alertData.get("timestamp")
            );
            
            // Send to monitoring team
            emailService.sendSimpleMessage("monitoring@mediconnect.com", subject, content);
            
            // Log for debugging
            System.out.println("Sent monitoring alert email");
        } catch (Exception e) {
            System.err.println("Failed to send monitoring alert: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Schedule medication reminder for a patient
     */
    public void schedulePatientMedicationReminder(Long patientId, String medicationName, String reminderTime) {
        // Log the scheduled reminder (actual implementation would involve a scheduler)
        System.out.println("MEDICATION REMINDER SCHEDULED");
        System.out.println("Patient ID: " + patientId);
        System.out.println("Medication: " + medicationName);
        System.out.println("Reminder Time: " + reminderTime);
        
        // In a real application, this would schedule a task to send the reminder email at the specified time
    }
    
    /**
     * Helper method to extract Google Meet link from session notes
     */
    private String extractMeetLink(String sessionNotes) {
        if (sessionNotes == null) {
            return null;
        }
        
        // Look for Google Meet link in session notes
        if (sessionNotes.contains("Google Meet Link: ")) {
            int startIndex = sessionNotes.indexOf("Google Meet Link: ") + "Google Meet Link: ".length();
            int endIndex = sessionNotes.indexOf('\n', startIndex);
            
            if (endIndex == -1) {
                // No newline found, use the entire remaining string
                return sessionNotes.substring(startIndex);
            } else {
                return sessionNotes.substring(startIndex, endIndex);
            }
        }
        
        return null;
    }
}