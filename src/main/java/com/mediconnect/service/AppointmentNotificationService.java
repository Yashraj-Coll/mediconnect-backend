package com.mediconnect.service;

import com.mediconnect.model.Appointment;
import com.mediconnect.model.RazorpayPayment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
public class AppointmentNotificationService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentNotificationService.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private PdfGenerationService pdfGenerationService;


    @Value("${mediconnect.notifications.enabled:true}")
    private boolean notificationsEnabled;

    @Value("${mediconnect.notifications.payment-confirmation:true}")
    private boolean paymentConfirmationEnabled;

    @Value("${mediconnect.notifications.appointment-confirmation:true}")
    private boolean appointmentConfirmationEnabled;

    /**
     * Main method to trigger all notifications after successful payment
     */
    public void sendAllNotifications(RazorpayPayment payment, Appointment appointment) {
        if (!notificationsEnabled) {
            log.info("Notifications are disabled");
            return;
        }

        try {
            log.info("Starting notification process for payment: {} and appointment: {}", 
                    payment.getId(), appointment.getId());

            // Prepare common data
            String patientEmail = getPatientEmail(appointment);
            String patientName = getPatientName(appointment);
            String doctorName = getDoctorName(appointment);
            String appointmentDateTime = formatAppointmentDateTime(appointment);

            // Generate PDF receipt
            String pdfPath = pdfGenerationService.generatePaymentReceipt(payment, appointment);
            log.info("PDF receipt generated: {}", pdfPath);

            // Send notifications asynchronously
            CompletableFuture<Void> allNotifications = CompletableFuture.allOf(
                sendPaymentConfirmationAsync(payment, appointment, patientEmail, patientName, doctorName, appointmentDateTime, pdfPath),
                sendAppointmentConfirmationAsync(payment, appointment, patientEmail, patientName, doctorName, appointmentDateTime)
            );

            // Log completion (non-blocking)
            allNotifications.thenRun(() -> {
                log.info("All notifications sent successfully for appointment: {}", appointment.getId());
            }).exceptionally(throwable -> {
                log.error("Error in notification process: ", throwable);
                return null;
            });

        } catch (Exception e) {
            log.error("Failed to initiate notification process for payment: {} - Error: ", payment.getId(), e);
        }
    }

    private CompletableFuture<Void> sendPaymentConfirmationAsync(RazorpayPayment payment, Appointment appointment, 
            String patientEmail, String patientName, 
            String doctorName, String appointmentDateTime,
            String pdfPath) {
return CompletableFuture.runAsync(() -> {
if (paymentConfirmationEnabled) {
try {
log.info("Sending payment confirmation email to: {}", patientEmail);

// Get the CORRECT appointment type
String appointmentType = "In-Person"; // Default
if (appointment.getAppointmentType() == Appointment.AppointmentType.video) {
appointmentType = "Video Consultation";
}

log.info("Payment confirmation for appointment {}: Type={}, DateTime={}", 
appointment.getId(), appointmentType, appointmentDateTime);

emailService.sendPaymentConfirmationEmailWithType(
patientEmail,
patientName,
payment.getRazorpayPaymentId(),
payment.getRazorpayOrderId(),
payment.getAmount().toString(),
doctorName,
getDoctorSpecialization(appointment),
appointmentDateTime,
appointmentType, // Pass the correct type
pdfPath
);

log.info("Payment confirmation email sent successfully to: {}", patientEmail);
} catch (Exception e) {
log.error("Error sending payment confirmation email to {}: ", patientEmail, e);
}
} else {
log.info("Payment confirmation email disabled");
}
});
}

    private CompletableFuture<Void> sendAppointmentConfirmationAsync(RazorpayPayment payment, Appointment appointment, 
                                                               String patientEmail, String patientName, 
                                                               String doctorName, String appointmentDateTime) {
    return CompletableFuture.runAsync(() -> {
        if (appointmentConfirmationEnabled) {
            try {
                log.info("Sending appointment confirmation email to: {}", patientEmail);
                
                // Generate Jitsi Meet link for video consultations
                String jitsiMeetingLink = "";
                String hospitalAddress = "";
                
                if ("video".equals(appointment.getAppointmentType().toString().toLowerCase())) {
                    // Generate Jitsi room name consistently
                    String roomName = appointment.getVideoRoomName();
                    if (roomName == null || roomName.trim().isEmpty()) {
                        roomName = "mediconnect-doctor-" + appointment.getDoctor().getId() + 
                                  "-patient-" + appointment.getPatient().getId() + "-" + appointment.getId();
                        // Update appointment with room name
                        appointment.setVideoRoomName(roomName);
                        // Note: In a real scenario, you'd save this to database here
                    }
                    
                    // Construct Jitsi Meet URL
                    jitsiMeetingLink = "https://meet.jit.si/" + roomName;
                    log.info("Generated Jitsi Meet link for appointment {}: {}", appointment.getId(), jitsiMeetingLink);
                } else {
                    hospitalAddress = getHospitalAddress(appointment);
                }

                emailService.sendAppointmentConfirmationEmail(
                    patientEmail,
                    patientName,
                    doctorName,
                    getDoctorSpecialization(appointment),
                    appointmentDateTime,
                    appointment.getAppointmentType().toString(),
                    jitsiMeetingLink, // Pass Jitsi link instead of Google Meet
                    hospitalAddress,
                    null // pdfPath
                );
                log.info("Appointment confirmation email sent successfully to: {}", patientEmail);
            } catch (Exception e) {
                log.error("Error sending appointment confirmation email to {}: ", patientEmail, e);
            }
        } else {
            log.info("Appointment confirmation email disabled");
        }
    });
}

    /**
     * Send only payment confirmation (can be called separately)
     */
    public void sendPaymentConfirmation(RazorpayPayment payment, Appointment appointment) {
        if (!notificationsEnabled || !paymentConfirmationEnabled) {
            log.info("Payment confirmation disabled");
            return;
        }

        try {
            log.info("Sending standalone payment confirmation");
            
            // Generate PDF receipt
            String pdfPath = pdfGenerationService.generatePaymentReceipt(payment, appointment);
            
            emailService.sendPaymentConfirmationEmail(
                getPatientEmail(appointment),
                getPatientName(appointment),
                payment.getRazorpayPaymentId(),
                payment.getRazorpayOrderId(),
                payment.getAmount().toString(),
                getDoctorName(appointment),
                getDoctorSpecialization(appointment),
                formatAppointmentDateTime(appointment),
                pdfPath
            );
            
            log.info("Standalone payment confirmation sent successfully");
        } catch (Exception e) {
            log.error("Error sending standalone payment confirmation: ", e);
        }
    }

    /**
     * Send only appointment confirmation (can be called separately)
     */
    public void sendAppointmentConfirmation(Appointment appointment) {
    if (!notificationsEnabled || !appointmentConfirmationEnabled) {
        log.info("Appointment confirmation disabled");
        return;
    }

    try {
        log.info("Sending standalone appointment confirmation");
        
        String jitsiMeetingLink = "";
        String hospitalAddress = "";
        
        if ("video".equals(appointment.getAppointmentType().toString().toLowerCase())) {
            // Generate Jitsi room name consistently
            String roomName = appointment.getVideoRoomName();
            if (roomName == null || roomName.trim().isEmpty()) {
                roomName = "mediconnect-doctor-" + appointment.getDoctor().getId() + 
                          "-patient-" + appointment.getPatient().getId() + "-" + appointment.getId();
                appointment.setVideoRoomName(roomName);
            }
            
            // Construct Jitsi Meet URL
            jitsiMeetingLink = "https://meet.jit.si/" + roomName;
            log.info("Generated Jitsi Meet link for appointment {}: {}", appointment.getId(), jitsiMeetingLink);
        } else {
            hospitalAddress = getHospitalAddress(appointment);
        }

        emailService.sendAppointmentConfirmationEmail(
            getPatientEmail(appointment),
            getPatientName(appointment),
            getDoctorName(appointment),
            getDoctorSpecialization(appointment),
            formatAppointmentDateTime(appointment),
            appointment.getAppointmentType().toString(),
            jitsiMeetingLink, // Pass Jitsi link instead of Google Meet
            hospitalAddress,
            null
        );
        log.info("Standalone appointment confirmation sent successfully");
    } catch (Exception e) {
        log.error("Error sending standalone appointment confirmation: ", e);
    }
}

    // Helper methods
    private String getPatientEmail(Appointment appointment) {
        if (appointment.getPatient() != null && appointment.getPatient().getUser() != null) {
            return appointment.getPatient().getUser().getEmail();
        }
        return "";
    }

    private String getPatientName(Appointment appointment) {
        if (appointment.getPatient() != null && appointment.getPatient().getUser() != null) {
            return appointment.getPatient().getUser().getFirstName() + " " + 
                   appointment.getPatient().getUser().getLastName();
        }
        return "Patient";
    }

    private String getDoctorName(Appointment appointment) {
        if (appointment.getDoctor() != null && appointment.getDoctor().getUser() != null) {
            return appointment.getDoctor().getUser().getFirstName() + " " + 
                   appointment.getDoctor().getUser().getLastName();
        }
        return "Doctor";
    }

    private String formatAppointmentDateTime(Appointment appointment) {
        try {
            if (appointment.getAppointmentDateTime() == null) {
                return "Date/Time to be confirmed";
            }
            
            // Convert to IST if needed and format consistently
            OffsetDateTime appointmentTime = appointment.getAppointmentDateTime();
            
            // Ensure we're working with IST timezone
            ZonedDateTime istTime = appointmentTime.atZoneSameInstant(ZoneId.of("Asia/Kolkata"));
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' hh:mm a");
            String formattedDateTime = istTime.format(formatter);
            
            log.info("Formatted appointment datetime for ID {}: Original={}, Formatted={}", 
                    appointment.getId(), appointmentTime, formattedDateTime);
            
            return formattedDateTime;
        } catch (Exception e) {
            log.error("Error formatting appointment date for appointment {}: ", appointment.getId(), e);
            return "Date/Time to be confirmed";
        }
    }

    private String getHospitalAddress(Appointment appointment) {
        if (appointment.getDoctor() != null) {
            String hospital = appointment.getDoctor().getHospitalAffiliation();
            if (hospital != null && !hospital.isEmpty()) {
                return hospital;
            }
        }
        return "MediConnect Healthcare Center, Tech City, India";
    }

    private String getDoctorSpecialization(Appointment appointment) {
        if (appointment.getDoctor() != null && appointment.getDoctor().getSpecialization() != null) {
            return appointment.getDoctor().getSpecialization();
        }
        return "General Medicine"; // default fallback
    }
}