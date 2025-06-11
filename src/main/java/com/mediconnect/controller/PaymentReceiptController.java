package com.mediconnect.controller;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.mediconnect.model.Patient;
import com.mediconnect.model.RazorpayPayment;
import com.mediconnect.security.UserDetailsImpl;
import com.mediconnect.service.PatientService;
import com.mediconnect.service.PdfGenerationService;
import com.mediconnect.service.RazorpayServiceInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/payments")
public class PaymentReceiptController {

    private static final Logger log = LoggerFactory.getLogger(PaymentReceiptController.class);

    @Autowired
    private RazorpayServiceInterface razorpayService;

    @Autowired
    private PdfGenerationService pdfGenerationService;
    
    @Autowired
    private PatientService patientService;

    /**
     * Get all payments for current authenticated user (patient)
     * FIXED: Added DTO mapping to prevent circular references
     */
    @GetMapping("/user")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<Map<String, Object>>> getCurrentUserPayments(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            log.info("🔍 Fetching payments for user: {}", userDetails.getEmail());
            
            if (userDetails == null || userDetails.getUser() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            // Get patient by user ID
            Patient patient = patientService.getPatientByUserId(userDetails.getUser().getId())
                    .orElse(null);
            
            if (patient == null) {
                log.warn("⚠️ No patient profile found for user: {}", userDetails.getUser().getId());
                return ResponseEntity.ok(List.of()); // Return empty list instead of error
            }

            // Get payments for this patient
            List<RazorpayPayment> payments = razorpayService.getPaymentsByPatientId(patient.getId());
            log.info("✅ Found {} payments for patient: {}", payments.size(), patient.getId());
            
            // FIXED: Convert to DTOs to prevent circular references
            List<Map<String, Object>> paymentDTOs = payments.stream()
                    .map(this::convertToPaymentDTO)
                    .collect(Collectors.toList());
            
            log.info("✅ Converted {} payments to DTOs", paymentDTOs.size());
            return ResponseEntity.ok(paymentDTOs);
            
        } catch (Exception e) {
            log.error("❌ Error fetching user payments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * FIXED: Convert RazorpayPayment entity to DTO to prevent circular references
     */
    private Map<String, Object> convertToPaymentDTO(RazorpayPayment payment) {
        Map<String, Object> dto = new HashMap<>();
        
        // Basic payment information
        dto.put("id", payment.getId());
        dto.put("amount", payment.getAmount());
        dto.put("currency", payment.getCurrency());
        dto.put("status", payment.getStatus() != null ? payment.getStatus().toString() : "UNKNOWN");
        dto.put("razorpayOrderId", payment.getRazorpayOrderId());
        dto.put("razorpayPaymentId", payment.getRazorpayPaymentId());
        dto.put("razorpaySignature", payment.getRazorpaySignature());
        dto.put("paymentMethod", payment.getPaymentMethod());
        dto.put("cardId", payment.getCardId());
        dto.put("cardNetwork", payment.getCardNetwork());
        dto.put("cardLast4", payment.getCardLast4());
        dto.put("email", payment.getEmail());
        dto.put("contact", payment.getContact());
        dto.put("createdAt", payment.getCreatedAt());
        dto.put("completedAt", payment.getCompletedAt());
        dto.put("refundedAt", payment.getRefundedAt());
        dto.put("errorMessage", payment.getErrorMessage());
        dto.put("refundReason", payment.getRefundReason());
        dto.put("bookingType", payment.getBookingType());

        // Add appointment information if present
        if (payment.getAppointment() != null) {
            Map<String, Object> appointmentDTO = new HashMap<>();
            appointmentDTO.put("id", payment.getAppointment().getId());
            appointmentDTO.put("appointmentDateTime", payment.getAppointment().getAppointmentDateTime());
            appointmentDTO.put("durationMinutes", payment.getAppointment().getDurationMinutes());
            appointmentDTO.put("appointmentType", payment.getAppointment().getAppointmentType());
            appointmentDTO.put("status", payment.getAppointment().getStatus());
            appointmentDTO.put("patientNotes", payment.getAppointment().getPatientNotes());
            appointmentDTO.put("doctorNotes", payment.getAppointment().getDoctorNotes());
            appointmentDTO.put("fee", payment.getAppointment().getFee());
            appointmentDTO.put("isPaid", payment.getAppointment().getIsPaid());
            appointmentDTO.put("createdAt", payment.getAppointment().getCreatedAt());
            appointmentDTO.put("updatedAt", payment.getAppointment().getUpdatedAt());

            // Add doctor information safely
            if (payment.getAppointment().getDoctor() != null) {
                Map<String, Object> doctorDTO = new HashMap<>();
                doctorDTO.put("id", payment.getAppointment().getDoctor().getId());
                doctorDTO.put("specialization", payment.getAppointment().getDoctor().getSpecialization());
                doctorDTO.put("consultationFee", payment.getAppointment().getDoctor().getConsultationFee());
                
                // Add user information safely
                if (payment.getAppointment().getDoctor().getUser() != null) {
                    Map<String, Object> doctorUserDTO = new HashMap<>();
                    doctorUserDTO.put("id", payment.getAppointment().getDoctor().getUser().getId());
                    doctorUserDTO.put("firstName", payment.getAppointment().getDoctor().getUser().getFirstName());
                    doctorUserDTO.put("lastName", payment.getAppointment().getDoctor().getUser().getLastName());
                    doctorUserDTO.put("email", payment.getAppointment().getDoctor().getUser().getEmail());
                    doctorDTO.put("user", doctorUserDTO);
                }
                appointmentDTO.put("doctor", doctorDTO);
            }

            dto.put("appointment", appointmentDTO);
        }

        // Add lab test booking information if present
        if (payment.getLabTestBooking() != null) {
            Map<String, Object> labTestDTO = new HashMap<>();
            labTestDTO.put("id", payment.getLabTestBooking().getId());
            // Add other lab test fields as needed - you'll need to check your LabTestBooking entity
            dto.put("labTestBooking", labTestDTO);
        }

        return dto;
    }

    /**
     * Download payment receipt as PDF - Enhanced with security
     */
    @GetMapping("/receipt/{paymentId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN') or hasRole('DOCTOR')")
    @CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
    public ResponseEntity<InputStreamResource> downloadReceipt(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            log.info("🧾 Generating receipt for payment ID: {} by user: {}", paymentId, userDetails.getEmail());
            
            // Get patient by user ID
            Patient patient = patientService.getPatientByUserId(userDetails.getUser().getId())
                    .orElse(null);
            
            if (patient == null) {
                log.error("❌ No patient profile found for user: {}", userDetails.getUser().getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Find payment by ID and verify ownership
            List<RazorpayPayment> userPayments = razorpayService.getPaymentsByPatientId(patient.getId());
            RazorpayPayment payment = userPayments.stream()
                    .filter(p -> p.getId().equals(paymentId))
                    .findFirst()
                    .orElse(null);
            
            if (payment == null) {
                log.error("❌ Payment not found or access denied for payment ID: {}", paymentId);
                return ResponseEntity.notFound().build();
            }

            // Check if payment is completed
            if (!"CAPTURED".equals(payment.getStatus().toString())) {
                log.error("❌ Cannot generate receipt for non-completed payment: {}", paymentId);
                return ResponseEntity.badRequest().build();
            }

            String pdfPath;
            String fileName;

            // Generate appropriate receipt based on payment type
            if ("LAB_TEST".equals(payment.getBookingType()) && payment.getLabTestBooking() != null) {
                log.info("🧪 Generating lab test receipt for payment: {}", paymentId);
                pdfPath = pdfGenerationService.generateLabTestReceipt(payment, payment.getLabTestBooking());
                fileName = "lab_test_receipt_" + paymentId + ".pdf";
            } else if (payment.getAppointment() != null) {
                log.info("🏥 Generating appointment receipt for payment: {}", paymentId);
                pdfPath = pdfGenerationService.generatePaymentReceipt(payment, payment.getAppointment());
                fileName = "appointment_receipt_" + paymentId + ".pdf";
            } else {
                log.error("❌ Invalid payment type for receipt generation: {}", paymentId);
                return ResponseEntity.badRequest().build();
            }

            // Serve the PDF file
            File pdfFile = new File(pdfPath);
            if (!pdfFile.exists()) {
                log.error("❌ Generated PDF file not found: {}", pdfPath);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(pdfFile));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);

            log.info("✅ Receipt generated successfully for payment: {}", paymentId);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(pdfFile.length())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);

        } catch (Exception e) {
            log.error("❌ Error generating receipt for payment {}: {}", paymentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get payment summary for current authenticated user
     * FIXED: Simplified to prevent any circular reference issues
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> getCurrentUserPaymentSummary(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            log.info("📊 Generating payment summary for user: {}", userDetails.getEmail());
            
            if (userDetails == null || userDetails.getUser() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            // Get patient by user ID
            Patient patient = patientService.getPatientByUserId(userDetails.getUser().getId())
                    .orElse(null);
            
            if (patient == null) {
                log.warn("⚠️ No patient profile found for user: {}", userDetails.getUser().getId());
                // Return default summary for users without patient profile
                Map<String, Object> defaultSummary = new HashMap<>();
                defaultSummary.put("totalSpent", 0.0);
                defaultSummary.put("totalTransactions", 0L);
                defaultSummary.put("successfulPayments", 0L);
                defaultSummary.put("pendingPayments", 0L);
                defaultSummary.put("failedPayments", 0L);
                defaultSummary.put("appointmentPayments", 0L);
                defaultSummary.put("labTestPayments", 0L);
                return ResponseEntity.ok(defaultSummary);
            }

            List<RazorpayPayment> payments = razorpayService.getPaymentsByPatientId(patient.getId());
            log.info("📊 Processing {} payments for summary", payments.size());
            
            Map<String, Object> summary = new HashMap<>();
            
            // Calculate totals safely
            double totalSpent = payments.stream()
                    .filter(p -> p.getStatus() != null && "CAPTURED".equals(p.getStatus().toString()))
                    .mapToDouble(p -> p.getAmount() != null ? p.getAmount().doubleValue() : 0.0)
                    .sum();
            
            long totalTransactions = payments.size();
            
            long successfulPayments = payments.stream()
                    .filter(p -> p.getStatus() != null && "CAPTURED".equals(p.getStatus().toString()))
                    .count();
            
            long pendingPayments = payments.stream()
                    .filter(p -> p.getStatus() != null && 
                               ("CREATED".equals(p.getStatus().toString()) || 
                                "AUTHORIZED".equals(p.getStatus().toString())))
                    .count();
            
            long failedPayments = payments.stream()
                    .filter(p -> p.getStatus() != null && "FAILED".equals(p.getStatus().toString()))
                    .count();
            
            long appointmentPayments = payments.stream()
                    .filter(p -> p.getAppointment() != null)
                    .count();
            
            long labTestPayments = payments.stream()
                    .filter(p -> p.getLabTestBooking() != null)
                    .count();
            
            summary.put("totalSpent", totalSpent);
            summary.put("totalTransactions", totalTransactions);
            summary.put("successfulPayments", successfulPayments);
            summary.put("pendingPayments", pendingPayments);
            summary.put("failedPayments", failedPayments);
            summary.put("appointmentPayments", appointmentPayments);
            summary.put("labTestPayments", labTestPayments);
            
            log.info("✅ Payment summary generated: {} payments, ₹{} total", totalTransactions, totalSpent);
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("❌ Error generating payment summary for user {}: {}", 
                    userDetails.getUser().getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Admin/Doctor endpoint to get payments for specific patient
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<Map<String, Object>>> getPatientPayments(@PathVariable Long patientId) {
        try {
            log.info("🔍 Admin/Doctor fetching payments for patient: {}", patientId);
            List<RazorpayPayment> payments = razorpayService.getPaymentsByPatientId(patientId);
            
            // Convert to DTOs to prevent circular references
            List<Map<String, Object>> paymentDTOs = payments.stream()
                    .map(this::convertToPaymentDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(paymentDTOs);
        } catch (Exception e) {
            log.error("❌ Error fetching patient payments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    /**
     * Get payment details by payment ID with proper amount formatting
     * Add this method to your PaymentReceiptController.java
     */
    @GetMapping("/details/{paymentId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> getPaymentDetailsById(@PathVariable String paymentId) {
        try {
            log.info("🔍 Fetching payment details for payment ID: {}", paymentId);
            
            RazorpayPayment payment = razorpayService.getPaymentDetails(paymentId);
            
            if (payment != null) {
                Map<String, Object> paymentDetails = new HashMap<>();
                paymentDetails.put("paymentId", payment.getRazorpayPaymentId());
                paymentDetails.put("orderId", payment.getRazorpayOrderId());
                
                // 🔧 CRITICAL FIX: Ensure amount is properly formatted
                BigDecimal amount = payment.getAmount();
                if (amount != null) {
                    // If amount is stored in paisa (> 1000), convert to rupees
                    if (amount.compareTo(new BigDecimal("1000")) > 0) {
                        amount = amount.divide(new BigDecimal("100"));
                    }
                    paymentDetails.put("amount", amount);
                } else {
                    paymentDetails.put("amount", BigDecimal.ZERO);
                }
                
                paymentDetails.put("currency", payment.getCurrency());
                paymentDetails.put("status", payment.getStatus().toString());
                paymentDetails.put("paymentMethod", payment.getPaymentMethod());
                paymentDetails.put("email", payment.getEmail());
                paymentDetails.put("contact", payment.getContact());
                paymentDetails.put("createdAt", payment.getCreatedAt());
                paymentDetails.put("completedAt", payment.getCompletedAt());
                paymentDetails.put("bookingType", payment.getBookingType());
                
                if (payment.getAppointment() != null) {
                    paymentDetails.put("appointmentId", payment.getAppointment().getId());
                }
                if (payment.getLabTestBooking() != null) {
                    paymentDetails.put("labTestBookingId", payment.getLabTestBooking().getId());
                    
                    // Add lab test details to help with display
                    Map<String, Object> labTestDetails = new HashMap<>();
                    labTestDetails.put("id", payment.getLabTestBooking().getId());
                    labTestDetails.put("testName", payment.getLabTestBooking().getTestName());
                    labTestDetails.put("testPrice", payment.getLabTestBooking().getTestPrice());
                    labTestDetails.put("sampleType", payment.getLabTestBooking().getSampleType());
                    labTestDetails.put("homeCollection", payment.getLabTestBooking().getHomeCollection());
                    labTestDetails.put("status", payment.getLabTestBooking().getStatus());
                    
                    paymentDetails.put("labTestBooking", labTestDetails);
                }
                
                log.info("✅ Payment details fetched successfully for payment ID: {}", paymentId);
                return ResponseEntity.ok(paymentDetails);
            } else {
                log.warn("⚠️ Payment not found for payment ID: {}", paymentId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("❌ Error fetching payment details for payment ID {}: {}", paymentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
}