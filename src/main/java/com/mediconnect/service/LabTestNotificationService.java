package com.mediconnect.service;

import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mediconnect.model.LabTestBooking;
import com.mediconnect.model.Patient;
import com.mediconnect.model.RazorpayPayment;
import com.mediconnect.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LabTestNotificationService {

    private static final Logger log = LoggerFactory.getLogger(LabTestNotificationService.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private PdfGenerationService pdfGenerationService;

    /**
     * Send comprehensive notifications for lab test payment success
     * (Payment confirmation + Lab test booking confirmation + PDF receipt)
     */
    public void sendAllNotifications(RazorpayPayment payment, LabTestBooking labTestBooking) {
        try {
            Patient patient = labTestBooking.getPatient();
            User patientUser = patient.getUser();
            
            String patientName = patientUser.getFirstName() + " " + patientUser.getLastName();
            String patientEmail = patientUser.getEmail();
            
            // Generate PDF receipt
            String pdfPath = null;
            try {
                pdfPath = pdfGenerationService.generateLabTestReceipt(payment, labTestBooking);
                log.info("Lab test PDF receipt generated: {}", pdfPath);
            } catch (Exception e) {
                log.error("Failed to generate lab test PDF receipt: ", e);
                // Continue without PDF - don't fail the entire notification process
            }
            
            // Format booking details
            String bookingDateTime = "Scheduled for sample collection";
            if (labTestBooking.getScheduledDate() != null) {
                bookingDateTime = labTestBooking.getScheduledDate().format(
                    DateTimeFormatter.ofPattern("dd-MM-yyyy")) + " at " +
                    labTestBooking.getScheduledDate().format(
                    DateTimeFormatter.ofPattern("hh:mm a"));
            }
            
            // Collection type
            String collectionType = labTestBooking.getHomeCollection() ? 
                "Home Sample Collection" : "Lab Visit Required";
            
            // Send payment confirmation email with PDF attachment
            boolean emailSent = emailService.sendLabTestPaymentConfirmationEmail(
                patientEmail,
                patientName,
                payment.getRazorpayPaymentId(),
                payment.getRazorpayOrderId(),
                payment.getAmount().toString(),
                labTestBooking.getTestName(),
                labTestBooking.getSampleType(),
                bookingDateTime,
                collectionType,
                pdfPath
            );
            
            if (emailSent) {
                log.info("Lab test payment confirmation email sent successfully to: {}", patientEmail);
            } else {
                log.error("Failed to send lab test payment confirmation email to: {}", patientEmail);
            }
            
        } catch (Exception e) {
            log.error("Error in lab test notification process: ", e);
            throw new RuntimeException("Notification process failed: " + e.getMessage());
        }
    }

    /**
     * Send lab test booking confirmation email
     */
    public boolean sendLabTestBookingConfirmationEmail(LabTestBooking labTestBooking) {
        try {
            Patient patient = labTestBooking.getPatient();
            User patientUser = patient.getUser();
            
            String patientName = patientUser.getFirstName() + " " + patientUser.getLastName();
            String patientEmail = patientUser.getEmail();
            
            String bookingDateTime = "Scheduled for sample collection";
            if (labTestBooking.getScheduledDate() != null) {
                bookingDateTime = labTestBooking.getScheduledDate().format(
                    DateTimeFormatter.ofPattern("dd-MM-yyyy")) + " at " +
                    labTestBooking.getScheduledDate().format(
                    DateTimeFormatter.ofPattern("hh:mm a"));
            }
            
            String collectionType = labTestBooking.getHomeCollection() ? 
                "Home Sample Collection" : "Lab Visit Required";
            
            return emailService.sendLabTestBookingConfirmationEmail(
                patientEmail,
                patientName,
                labTestBooking.getTestName(),
                labTestBooking.getSampleType(),
                bookingDateTime,
                collectionType,
                labTestBooking.getPatientAddress(),
                null // No PDF for booking confirmation
            );
            
        } catch (Exception e) {
            log.error("Failed to send lab test booking confirmation email: ", e);
            return false;
        }
    }

    /**
     * Send lab test status update notification
     */
    public void sendStatusUpdateNotification(LabTestBooking labTestBooking) {
        try {
            Patient patient = labTestBooking.getPatient();
            User patientUser = patient.getUser();
            
            String patientName = patientUser.getFirstName() + " " + patientUser.getLastName();
            String patientEmail = patientUser.getEmail();
            
            String status = labTestBooking.getStatus().toString();
            String statusMessage = getStatusMessage(labTestBooking.getStatus());
            
            emailService.sendLabTestStatusUpdateEmail(
                patientEmail,
                patientName,
                labTestBooking.getTestName(),
                status,
                statusMessage,
                "LAB-" + labTestBooking.getId()
            );
            
            log.info("Lab test status update notification sent for booking: {}", labTestBooking.getId());
            
        } catch (Exception e) {
            log.error("Failed to send lab test status update notification: ", e);
        }
    }

    /**
     * Send lab test cancellation notification
     */
    public void sendCancellationNotification(LabTestBooking labTestBooking) {
        try {
            Patient patient = labTestBooking.getPatient();
            User patientUser = patient.getUser();
            
            String patientName = patientUser.getFirstName() + " " + patientUser.getLastName();
            String patientEmail = patientUser.getEmail();
            
            emailService.sendLabTestCancellationEmail(
                patientEmail,
                patientName,
                labTestBooking.getTestName(),
                labTestBooking.getCancellationReason(),
                "LAB-" + labTestBooking.getId()
            );
            
            log.info("Lab test cancellation notification sent for booking: {}", labTestBooking.getId());
            
        } catch (Exception e) {
            log.error("Failed to send lab test cancellation notification: ", e);
        }
    }

    /**
     * Get user-friendly status message
     */
    private String getStatusMessage(LabTestBooking.BookingStatus status) {
        switch (status) {
            case PENDING:
                return "Your lab test booking is pending confirmation.";
            case CONFIRMED:
                return "Your lab test booking has been confirmed. Our team will contact you soon.";
            case SAMPLE_COLLECTED:
                return "Your sample has been collected and is being processed.";
            case IN_PROGRESS:
                return "Your lab test is currently in progress.";
            case COMPLETED:
                return "Your lab test has been completed. Reports are ready for download.";
            case CANCELLED:
                return "Your lab test booking has been cancelled.";
            default:
                return "Status updated for your lab test booking.";
        }
    }
}