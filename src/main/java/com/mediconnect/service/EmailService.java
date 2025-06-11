package com.mediconnect.service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.mediconnect.model.Appointment;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.Patient;
import com.mediconnect.model.Prescription;
import com.mediconnect.model.RazorpayPayment;
import com.mediconnect.model.User;
import com.mediconnect.model.VideoSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender emailSender;
    
    @Autowired
    private SpringTemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.from-name:MediConnect Health}")
    private String fromName;
    
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
        try {
            Context context = new Context();
            context.setVariables(templateModel);
            String htmlContent = templateEngine.process(templateName, context);
            
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, 
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, 
                    StandardCharsets.UTF_8.name());
            
            // FIXED: Handle UnsupportedEncodingException
            try {
                helper.setFrom(fromEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                log.warn("Failed to set from name, using email only: {}", e.getMessage());
                helper.setFrom(fromEmail);
            }
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            emailSender.send(message);
            log.info("Template email sent successfully to: {} with template: {}", to, templateName);
        } catch (Exception e) {
            log.error("Failed to send template email to: {} with template: {} - Error: ", to, templateName, e);
            throw new MessagingException("Email sending failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Send HTML email with attachment
     */
    public void sendMessageWithAttachment(String to, String subject, String text, String pathToAttachment) 
            throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        
        // FIXED: Handle UnsupportedEncodingException
        try {
            helper.setFrom(fromEmail, fromName);
        } catch (UnsupportedEncodingException e) {
            log.warn("Failed to set from name, using email only: {}", e.getMessage());
            helper.setFrom(fromEmail);
        }
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);
        
        if (StringUtils.hasText(pathToAttachment)) {
            FileSystemResource file = new FileSystemResource(new File(pathToAttachment));
            if (file.exists()) {
                helper.addAttachment(file.getFilename(), file);
            } else {
                log.warn("Attachment file not found: {}", pathToAttachment);
            }
        }
        
        emailSender.send(message);
    }

    /**
     * Send HTML email with template and attachment
     */
    public void sendTemplateMessageWithAttachment(String to, String subject, String templateName, 
                                                Map<String, Object> templateModel, String attachmentPath, 
                                                String attachmentName) throws MessagingException {
        Context context = new Context();
        context.setVariables(templateModel);
        String htmlContent = templateEngine.process(templateName, context);
        
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
        
        // FIXED: Handle UnsupportedEncodingException
        try {
            helper.setFrom(fromEmail, fromName);
        } catch (UnsupportedEncodingException e) {
            log.warn("Failed to set from name, using email only: {}", e.getMessage());
            helper.setFrom(fromEmail);
        }
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        // Add attachment if provided
        if (StringUtils.hasText(attachmentPath)) {
            FileSystemResource file = new FileSystemResource(new File(attachmentPath));
            if (file.exists()) {
                String fileName = StringUtils.hasText(attachmentName) ? attachmentName : file.getFilename();
                helper.addAttachment(fileName, file);
            } else {
                log.warn("Attachment file not found: {}", attachmentPath);
            }
        }
        
        emailSender.send(message);
    }

    /**
     * Send email asynchronously
     */
    public CompletableFuture<Boolean> sendEmailAsync(String to, String subject, String templateName, 
                                                   Map<String, Object> templateModel, String attachmentPath, 
                                                   String attachmentName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                sendTemplateMessageWithAttachment(to, subject, templateName, templateModel, attachmentPath, attachmentName);
                log.info("Email sent successfully to: {} with subject: {}", to, subject);
                return true;
            } catch (Exception e) {
                log.error("Failed to send email to: {} - Error: ", to, e);
                return false;
            }
        });
    }

    /**
     * Send payment confirmation email with PDF attachment
     */
    public boolean sendPaymentConfirmationEmail(String toEmail, String patientName, String transactionId, 
            String orderId, String amount, String doctorName, String specialization, String appointmentDateTime, String pdfPath) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientName", patientName);
            templateModel.put("transactionId", transactionId);
            templateModel.put("orderId", orderId);
            templateModel.put("amount", amount);
            templateModel.put("doctorName", doctorName);
            templateModel.put("specialization", specialization != null ? specialization : "General Medicine"); 
            templateModel.put("paymentMethod", "Online Payment");
            templateModel.put("paymentDate", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // Parse appointment date and time - FIX THE PARSING
            if (appointmentDateTime != null && appointmentDateTime.contains(" at ")) {
                String[] parts = appointmentDateTime.split(" at ");
                templateModel.put("appointmentDate", parts[0].trim());
                templateModel.put("appointmentTime", parts[1].trim());
            } else {
                templateModel.put("appointmentDate", appointmentDateTime);
                templateModel.put("appointmentTime", "");
            }
            
            // DEFAULT TO VIDEO - will be overridden by actual appointment type
            templateModel.put("appointmentType", "Video Consultation"); // Default to Video, not In-Person
            
            sendTemplateMessageWithAttachment(
                toEmail,
                "Payment Confirmation - MediConnect",
                "payment-confirmation",
                templateModel,
                pdfPath,
                "Payment_Receipt.pdf"
            );
            
            log.info("Payment confirmation email sent to: {} with datetime: {}", toEmail, appointmentDateTime);
            return true;
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email to: {} - Error: ", toEmail, e);
            return false;
        }
    }
    
    /**
     * Send payment confirmation email with correct appointment type
     */
    public boolean sendPaymentConfirmationEmailWithType(String toEmail, String patientName, String transactionId, 
            String orderId, String amount, String doctorName, String specialization, String appointmentDateTime, 
            String appointmentType, String pdfPath) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientName", patientName);
            templateModel.put("transactionId", transactionId);
            templateModel.put("orderId", orderId);
            templateModel.put("amount", amount);
            templateModel.put("doctorName", doctorName);
            templateModel.put("specialization", specialization != null ? specialization : "General Medicine"); 
            templateModel.put("paymentMethod", "Online Payment");
            templateModel.put("paymentDate", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // Parse appointment date and time correctly
            if (appointmentDateTime != null && appointmentDateTime.contains(" at ")) {
                String[] parts = appointmentDateTime.split(" at ");
                templateModel.put("appointmentDate", parts[0].trim());
                templateModel.put("appointmentTime", parts[1].trim());
            } else {
                templateModel.put("appointmentDate", appointmentDateTime);
                templateModel.put("appointmentTime", "");
            }
            
            // Use the CORRECT appointment type passed from the service
            templateModel.put("appointmentType", appointmentType);
            
            log.info("Payment confirmation template data: Date={}, Time={}, Type={}", 
                    templateModel.get("appointmentDate"), templateModel.get("appointmentTime"), appointmentType);
            
            sendTemplateMessageWithAttachment(
                toEmail,
                "Payment Confirmation - MediConnect",
                "payment-confirmation",
                templateModel,
                pdfPath,
                "Payment_Receipt.pdf"
            );
            
            log.info("Payment confirmation email sent to: {} with correct type: {}", toEmail, appointmentType);
            return true;
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email to: {} - Error: ", toEmail, e);
            return false;
        }
    }

    /**
     * Send appointment confirmation email
     */
    public boolean sendAppointmentConfirmationEmail(String toEmail, String patientName, String doctorName, 
        String specialization,
        String appointmentDateTime, String appointmentType, 
        String meetingLink, String hospitalAddress, String pdfPath) {
    try {
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("patientName", patientName);
        templateModel.put("doctorName", doctorName);
        templateModel.put("specialization", specialization != null ? specialization : "General Medicine");
        
        // Parse appointment date and time
        if (appointmentDateTime.contains(" at ")) {
            templateModel.put("appointmentDate", appointmentDateTime.substring(0, appointmentDateTime.indexOf(" at ")));
            templateModel.put("appointmentTime", appointmentDateTime.substring(appointmentDateTime.indexOf(" at ") + 4));
        } else {
            templateModel.put("appointmentDate", appointmentDateTime);
            templateModel.put("appointmentTime", "");
        }
        
        // Standardize appointment type
        if ("VIDEO".equalsIgnoreCase(appointmentType) || "video".equalsIgnoreCase(appointmentType)) {
            appointmentType = "VIDEO_CONSULTATION";
        } else if ("PHYSICAL".equalsIgnoreCase(appointmentType) || "physical".equalsIgnoreCase(appointmentType)) {
            appointmentType = "IN_PERSON";
        }
        templateModel.put("appointmentType", appointmentType);
        templateModel.put("appointmentDuration", "30"); // Default duration
        templateModel.put("appointmentNotes", ""); // Default empty notes
        
        // Video consultation specific - JITSI MEET
        if ("VIDEO_CONSULTATION".equalsIgnoreCase(appointmentType)) {
            templateModel.put("jitsiMeetingLink", meetingLink); // Changed from meetingLink to jitsiMeetingLink
            templateModel.put("isVideoConsultation", true);
            templateModel.put("isJitsiMeet", true); // Add flag for Jitsi Meet
            log.info("Adding Jitsi Meet link to email template: {}", meetingLink);
        } else {
            templateModel.put("hospitalAddress", hospitalAddress != null ? hospitalAddress : "MediConnect Healthcare Center, Tech City, India");
            templateModel.put("isVideoConsultation", false);
            templateModel.put("isJitsiMeet", false);
        }
        
        sendTemplateMessageWithAttachment(
            toEmail,
            "Appointment Confirmed - MediConnect",
            "appointment-confirmation",
            templateModel,
            pdfPath,
            pdfPath != null ? "Appointment_Confirmation.pdf" : null
        );
        
        log.info("Appointment confirmation email sent to: {} with meeting link: {}", toEmail, meetingLink);
        return true;
    } catch (Exception e) {
        log.error("Failed to send appointment confirmation email to: {} - Error: ", toEmail, e);
        return false;
    }
}
    
    /**
     * Send appointment confirmation email (DEPRECATED - Use the new method above)
     */
    public void sendAppointmentConfirmation(Appointment appointment) throws MessagingException {
        log.warn("DEPRECATED method sendAppointmentConfirmation called - use sendAppointmentConfirmationEmail instead");
        // Intentionally not implementing to avoid duplicate emails
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
     * Send payment confirmation email (DEPRECATED - Use the new method above)
     */
    public void sendPaymentConfirmationEmail(RazorpayPayment payment) throws MessagingException {
        log.warn("DEPRECATED method sendPaymentConfirmationEmail called - use the new method instead");
        // Intentionally not implementing to avoid duplicate emails
    }
    /**
     * Send lab test payment confirmation email with PDF attachment
     */
    public boolean sendLabTestPaymentConfirmationEmail(String toEmail, String patientName, String transactionId, 
            String orderId, String amount, String testName, String sampleType, String bookingDateTime, 
            String collectionType, String pdfPath) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientName", patientName);
            templateModel.put("transactionId", transactionId);
            templateModel.put("orderId", orderId);
            templateModel.put("amount", amount);
            templateModel.put("testName", testName);
            templateModel.put("sampleType", sampleType != null ? sampleType : "Blood"); 
            templateModel.put("paymentMethod", "Online Payment");
            templateModel.put("paymentDate", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // Parse booking date and time
            if (bookingDateTime != null && bookingDateTime.contains(" at ")) {
                String[] parts = bookingDateTime.split(" at ");
                templateModel.put("bookingDate", parts[0].trim());
                templateModel.put("bookingTime", parts[1].trim());
            } else {
                templateModel.put("bookingDate", bookingDateTime);
                templateModel.put("bookingTime", "");
            }
            
            templateModel.put("collectionType", collectionType);
            templateModel.put("isLabTest", true); // Flag to identify lab test emails
            
            sendTemplateMessageWithAttachment(
                toEmail,
                "Lab Test Payment Confirmation - MediConnect",
                "lab-test-payment-confirmation", // You'll need to create this template
                templateModel,
                pdfPath,
                "Lab_Test_Receipt.pdf"
            );
            
            log.info("Lab test payment confirmation email sent to: {} for test: {}", toEmail, testName);
            return true;
        } catch (Exception e) {
            log.error("Failed to send lab test payment confirmation email to: {} - Error: ", toEmail, e);
            return false;
        }
    }
    /**
     * Send lab test booking confirmation email
     */
    public boolean sendLabTestBookingConfirmationEmail(String toEmail, String patientName, String testName, 
        String sampleType, String bookingDateTime, String collectionType, 
        String patientAddress, String pdfPath) {
    try {
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("patientName", patientName);
        templateModel.put("testName", testName);
        templateModel.put("sampleType", sampleType != null ? sampleType : "Blood");
        
        // Parse booking date and time
        if (bookingDateTime != null && bookingDateTime.contains(" at ")) {
            templateModel.put("bookingDate", bookingDateTime.substring(0, bookingDateTime.indexOf(" at ")));
            templateModel.put("bookingTime", bookingDateTime.substring(bookingDateTime.indexOf(" at ") + 4));
        } else {
            templateModel.put("bookingDate", bookingDateTime);
            templateModel.put("bookingTime", "");
        }
        
        templateModel.put("collectionType", collectionType);
        templateModel.put("patientAddress", patientAddress != null ? patientAddress : "Address on file");
        templateModel.put("isLabTest", true);
        templateModel.put("isHomeCollection", collectionType != null && collectionType.contains("Home"));
        
        sendTemplateMessageWithAttachment(
            toEmail,
            "Lab Test Booking Confirmed - MediConnect",
            "lab-test-booking-confirmation", // You'll need to create this template
            templateModel,
            pdfPath,
            pdfPath != null ? "Lab_Test_Booking.pdf" : null
        );
        
        log.info("Lab test booking confirmation email sent to: {} for test: {}", toEmail, testName);
        return true;
    } catch (Exception e) {
        log.error("Failed to send lab test booking confirmation email to: {} - Error: ", toEmail, e);
        return false;
    }
    }

    /**
     * Send lab test status update email
     */
    public boolean sendLabTestStatusUpdateEmail(String toEmail, String patientName, String testName, 
            String status, String statusMessage, String bookingId) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientName", patientName);
            templateModel.put("testName", testName);
            templateModel.put("status", status);
            templateModel.put("statusMessage", statusMessage);
            templateModel.put("bookingId", bookingId);
            templateModel.put("updateDate", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            sendTemplateMessage(
                toEmail,
                "Lab Test Status Update - MediConnect",
                "lab-test-status-update", // You'll need to create this template
                templateModel
            );
            
            log.info("Lab test status update email sent to: {} for booking: {}", toEmail, bookingId);
            return true;
        } catch (Exception e) {
            log.error("Failed to send lab test status update email to: {} - Error: ", toEmail, e);
            return false;
        }
    }

    /**
     * Send lab test cancellation email
     */
    public boolean sendLabTestCancellationEmail(String toEmail, String patientName, String testName, 
            String cancellationReason, String bookingId) {
        try {
            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("patientName", patientName);
            templateModel.put("testName", testName);
            templateModel.put("cancellationReason", cancellationReason != null ? cancellationReason : "No reason provided");
            templateModel.put("bookingId", bookingId);
            templateModel.put("cancellationDate", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            sendTemplateMessage(
                toEmail,
                "Lab Test Booking Cancelled - MediConnect",
                "lab-test-cancellation", // You'll need to create this template
                templateModel
            );
            
            log.info("Lab test cancellation email sent to: {} for booking: {}", toEmail, bookingId);
            return true;
        } catch (Exception e) {
            log.error("Failed to send lab test cancellation email to: {} - Error: ", toEmail, e);
            return false;
        }
    }
    /**
     * Send password reset OTP - MISSING METHOD
     */
    public boolean sendPasswordResetOtp(String toEmail, String otp, String firstName) {
        try {
            String subject = "Password Reset OTP - MediConnect";
            String content = buildPasswordResetOtpContent(otp, firstName);
            
            return sendHtmlEmailSimple(toEmail, subject, content);
            
        } catch (Exception e) {
            log.error("Failed to send password reset OTP to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Send password change confirmation - MISSING METHOD
     */
    public boolean sendPasswordChangeConfirmation(String toEmail, String firstName) {
        try {
            String subject = "Password Changed Successfully - MediConnect";
            String content = buildPasswordChangeConfirmationContent(firstName);
            
            return sendHtmlEmailSimple(toEmail, subject, content);
            
        } catch (Exception e) {
            log.error("Failed to send password change confirmation to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Simple HTML email sender
     */
    private boolean sendHtmlEmailSimple(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            emailSender.send(message);
            log.info("HTML email sent successfully to: {}", toEmail);
            return true;
            
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    // ============ EMAIL CONTENT BUILDERS ============

    private String buildPasswordResetOtpContent(String otp, String firstName) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Password Reset OTP</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; margin: 0; padding: 20px; background-color: #f4f4f4;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px;">
                    <h1 style="color: #8B5CF6; text-align: center;">MediConnect</h1>
                    <h2 style="color: #333;">Password Reset Request</h2>
                    <p>Hello %s,</p>
                    <p>We received a request to reset your password. Use the OTP below:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <div style="background-color: #8B5CF6; color: white; font-size: 32px; font-weight: bold; padding: 20px; border-radius: 8px; letter-spacing: 5px; display: inline-block;">
                            %s
                        </div>
                    </div>
                    <p><strong>Important:</strong> This OTP is valid for 15 minutes only.</p>
                    <p>If you didn't request this, please ignore this email.</p>
                    <div style="margin-top: 30px; text-align: center; color: #888; font-size: 12px;">
                        <p>This is an automated email from MediConnect.</p>
                    </div>
                </div>
            </body>
            </html>
            """, firstName, otp);
    }

    private String buildPasswordChangeConfirmationContent(String firstName) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Password Changed</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; margin: 0; padding: 20px; background-color: #f4f4f4;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px;">
                    <h1 style="color: #8B5CF6; text-align: center;">MediConnect</h1>
                    <h2 style="color: #333; text-align: center;">Password Changed Successfully</h2>
                    <p>Hello %s,</p>
                    <p>Your password has been successfully changed. You can now use your new password to log in.</p>
                    <p>If you didn't make this change, please contact our support team immediately.</p>
                    <div style="margin-top: 30px; text-align: center; color: #888; font-size: 12px;">
                        <p>This is an automated email from MediConnect.</p>
                    </div>
                </div>
            </body>
            </html>
            """, firstName);
    }
}