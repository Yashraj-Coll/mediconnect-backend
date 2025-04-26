package com.mediconnect.service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import com.mediconnect.model.Appointment;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.Patient;
import com.mediconnect.model.Prescription;
import com.mediconnect.model.RazorpayPayment;
import com.mediconnect.model.User;
import com.mediconnect.model.VideoSession;


@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;
    
    @Autowired
    private SpringTemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    /**
     * Send a simple text email
     */
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
    
    /**
     * Send HTML email using Thymeleaf template
     */
    public void sendTemplateMessage(String to, String subject, String templateName, Map<String, Object> templateModel) 
            throws MessagingException {
        Context context = new Context();
        context.setVariables(templateModel);
        String htmlContent = templateEngine.process(templateName, context);
        
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, 
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, 
                StandardCharsets.UTF_8.name());
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        emailSender.send(message);
    }
    
    /**
     * Send HTML email with attachment
     */
    public void sendMessageWithAttachment(String to, String subject, String text, String pathToAttachment) 
            throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);
        
        FileSystemResource file = new FileSystemResource(new File(pathToAttachment));
        helper.addAttachment(file.getFilename(), file);
        
        emailSender.send(message);
    }
    
    /**
     * Send appointment confirmation email
     */
    public void sendAppointmentConfirmation(Appointment appointment) throws MessagingException {
        Patient patient = appointment.getPatient();
        Doctor doctor = appointment.getDoctor();
        User patientUser = patient.getUser();
        User doctorUser = doctor.getUser();
        
        Map<String, Object> templateModel = Map.of(
            "patientName", patientUser.getFirstName() + " " + patientUser.getLastName(),
            "doctorName", "Dr. " + doctorUser.getFirstName() + " " + doctorUser.getLastName(),
            "specialization", doctor.getSpecialization(),
            "appointmentDate", appointment.getAppointmentDateTime().toLocalDate().toString(),
            "appointmentTime", appointment.getAppointmentDateTime().toLocalTime().toString(),
            "appointmentType", appointment.getAppointmentType().toString(),
            "appointmentDuration", appointment.getDurationMinutes() + " minutes",
            "appointmentNotes", appointment.getPatientNotes() != null ? appointment.getPatientNotes() : "None"
        );
        
        sendTemplateMessage(
            patientUser.getEmail(),
            "Your Appointment is Confirmed",
            "appointment-confirmation",
            templateModel
        );
    }
    
    /**
     * Send appointment reminder email (24 hours before)
     */
    public void sendAppointmentReminder(Appointment appointment) throws MessagingException {
        Patient patient = appointment.getPatient();
        Doctor doctor = appointment.getDoctor();
        User patientUser = patient.getUser();
        User doctorUser = doctor.getUser();
        
        Map<String, Object> templateModel = Map.of(
            "patientName", patientUser.getFirstName() + " " + patientUser.getLastName(),
            "doctorName", "Dr. " + doctorUser.getFirstName() + " " + doctorUser.getLastName(),
            "appointmentDate", appointment.getAppointmentDateTime().toLocalDate().toString(),
            "appointmentTime", appointment.getAppointmentDateTime().toLocalTime().toString(),
            "appointmentType", appointment.getAppointmentType().toString()
        );
        
        sendTemplateMessage(
            patientUser.getEmail(),
            "Reminder: Your Appointment Tomorrow",
            "appointment-reminder",
            templateModel
        );
    }
    
    /**
     * Send prescription notification with PDF attachment
     */
    public void sendPrescriptionNotification(Prescription prescription, String pdfPath) throws MessagingException {
        Patient patient = prescription.getPatient();
        Doctor doctor = prescription.getDoctor();
        User patientUser = patient.getUser();
        User doctorUser = doctor.getUser();
        
        String emailContent = String.format(
            "<h2>Your Prescription Is Ready</h2>" +
            "<p>Hello %s,</p>" +
            "<p>Dr. %s has issued a new prescription for you. The prescription details are attached to this email.</p>" +
            "<p>Please take the medications as prescribed. If you have any questions, feel free to contact your doctor.</p>" +
            "<p>Regards,<br>MediConnect Team</p>",
            patientUser.getFirstName(),
            doctorUser.getLastName()
        );
        
        sendMessageWithAttachment(
            patientUser.getEmail(),
            "Your New Prescription",
            emailContent,
            pdfPath
        );
    }
    
    /**
     * Send video session link email
     */
    public void sendVideoSessionInvitation(VideoSession videoSession) throws MessagingException {
        Appointment appointment = videoSession.getAppointment();
        Patient patient = appointment.getPatient();
        Doctor doctor = appointment.getDoctor();
        User patientUser = patient.getUser();
        
        String sessionUrl = "https://mediconnect.com/video/" + videoSession.getSessionId();
        
        Map<String, Object> templateModel = Map.of(
            "patientName", patientUser.getFirstName() + " " + patientUser.getLastName(),
            "doctorName", "Dr. " + doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName(),
            "appointmentDate", appointment.getAppointmentDateTime().toLocalDate().toString(),
            "appointmentTime", appointment.getAppointmentDateTime().toLocalTime().toString(),
            "sessionUrl", sessionUrl,
            "token", videoSession.getPatientToken()
        );
        
        sendTemplateMessage(
            patientUser.getEmail(),
            "Your Video Consultation Link",
            "video-session-invitation",
            templateModel
        );
    }
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(User user, String resetToken) throws MessagingException {
        String resetUrl = "https://mediconnect.com/reset-password?token=" + resetToken;
        
        Map<String, Object> templateModel = Map.of(
            "name", user.getFirstName(),
            "resetUrl", resetUrl,
            "expiryHours", 24 // Token validity in hours
        );
        
        sendTemplateMessage(
            user.getEmail(),
            "Reset Your MediConnect Password",
            "password-reset",
            templateModel
        );
    }
    
    /**
     * Send payment confirmation email
     */
    public void sendPaymentConfirmationEmail(RazorpayPayment payment) throws MessagingException {
        Appointment appointment = payment.getAppointment();
        Patient patient = appointment.getPatient();
        Doctor doctor = appointment.getDoctor();
        User patientUser = patient.getUser();
        User doctorUser = doctor.getUser();
        
        Map<String, Object> templateModel = Map.of(
            "patientName", patientUser.getFirstName() + " " + patientUser.getLastName(),
            "doctorName", "Dr. " + doctorUser.getFirstName() + " " + doctorUser.getLastName(),
            "appointmentDate", appointment.getAppointmentDateTime().toLocalDate().toString(),
            "appointmentTime", appointment.getAppointmentDateTime().toLocalTime().toString(),
            "transactionId", payment.getRazorpayPaymentId(),
            "orderId", payment.getRazorpayOrderId(),
            "amount", payment.getAmount().toString(),
            "currency", payment.getCurrency(),
            "paymentMethod", payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "Online",
            "paymentDate", payment.getCompletedAt().toString()
        );
        
        sendTemplateMessage(
            patientUser.getEmail(),
            "Payment Confirmation - MediConnect",
            "payment-confirmation",
            templateModel
        );
    }
}