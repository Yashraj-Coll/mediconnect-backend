package com.mediconnect.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mediconnect.dto.AppointmentDetailsDTO;
import com.mediconnect.dto.RazorpayDTO.OrderRequest;
import com.mediconnect.dto.RazorpayDTO.OrderResponse;
import com.mediconnect.dto.RazorpayDTO.PaymentVerificationRequest;
import com.mediconnect.dto.RazorpayDTO.PaymentDetailsResponse;
import com.mediconnect.model.RazorpayPayment;
import com.mediconnect.service.RazorpayServiceInterface;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/payments/razorpay")
public class RazorpayController {

    @Autowired
    private RazorpayServiceInterface razorpayService;

    /**
     * Create a new Razorpay order for appointment payment
     */
    @PostMapping("/order")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        Long appointmentId = request.getAppointmentId();
        OrderResponse order = razorpayService.createOrder(
                appointmentId,
                request.getAmount(),
                request.getCurrency(),
                request.getName(),
                request.getEmail(),
                request.getContact()
        );
        return ResponseEntity.ok(order);
    }
    
    /**
     * Create a new Razorpay order for lab test payment
     */
    @PostMapping("/lab-test/order")
    public ResponseEntity<?> createLabTestOrder(@RequestBody Map<String, Object> request) {
        try {
            // Extract and validate required fields
            if (!request.containsKey("labTestBookingId")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Lab test booking ID is required"));
            }
            
            if (!request.containsKey("amount")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Amount is required"));
            }
            
            Long labTestBookingId = Long.valueOf(request.get("labTestBookingId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String currency = request.getOrDefault("currency", "INR").toString();
            String name = (String) request.get("name");
            String email = (String) request.get("email");
            String contact = (String) request.get("contact");
            
            OrderResponse order = razorpayService.createLabTestOrder(
                    labTestBookingId, amount, currency, name, email, contact);
            return ResponseEntity.ok(order);
            
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Invalid number format in request"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to create lab test order: " + e.getMessage()));
        }
    }

    /**
     * Verify payment after completion
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest request) {

        Map<String, Object> result = razorpayService.verifyPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature());

        return ResponseEntity.ok(result);
    }

    /**
     * Handle Razorpay webhook events
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        razorpayService.processWebhookEvent(payload, signature);
        return ResponseEntity.ok("Webhook processed successfully");
    }

    /**
     * Get all payments for an appointment
     */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<Map<String, Object>> getPaymentsByAppointmentId(@PathVariable Long appointmentId) {
        try {
            List<RazorpayPayment> payments = razorpayService.getPaymentsByAppointmentId(appointmentId);
            if (!payments.isEmpty()) {
                // Return the most recent payment with appointment details
                RazorpayPayment latestPayment = payments.get(0);
                
                Map<String, Object> response = new HashMap<>();
                response.put("paymentId", latestPayment.getRazorpayPaymentId());
                response.put("orderId", latestPayment.getRazorpayOrderId());
                response.put("amount", latestPayment.getAmount());
                response.put("status", latestPayment.getStatus().name());
                response.put("createdAt", latestPayment.getCreatedAt());
                response.put("appointmentId", appointmentId);
                
                // Include appointment details if available
                if (latestPayment.getAppointment() != null) {
                    response.put("appointment", new AppointmentDetailsDTO(latestPayment.getAppointment()));
                }
                
                return ResponseEntity.ok(Map.of("success", true, "data", response));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "No payment found for appointment"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to fetch payment details", "error", e.getMessage()));
        }
    }
    
    /**
     * Get all payments for a lab test booking
     */
    @GetMapping("/lab-test/{labTestBookingId}")
    public ResponseEntity<Map<String, Object>> getPaymentsByLabTestBookingId(@PathVariable Long labTestBookingId) {
        try {
            List<RazorpayPayment> payments = razorpayService.getPaymentsByLabTestBookingId(labTestBookingId);
            if (!payments.isEmpty()) {
                RazorpayPayment latestPayment = payments.get(0);
                
                Map<String, Object> response = new HashMap<>();
                response.put("paymentId", latestPayment.getRazorpayPaymentId());
                response.put("orderId", latestPayment.getRazorpayOrderId());
                response.put("amount", latestPayment.getAmount());
                response.put("status", latestPayment.getStatus().name());
                response.put("createdAt", latestPayment.getCreatedAt());
                response.put("labTestBookingId", labTestBookingId);
                
                if (latestPayment.getLabTestBooking() != null) {
                    response.put("labTestBooking", latestPayment.getLabTestBooking());
                }
                
                return ResponseEntity.ok(Map.of("success", true, "data", response));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "No payment found for lab test booking"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to fetch payment details", "error", e.getMessage()));
        }
    }
    

    /**
     * Get payments by patient ID
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<RazorpayPayment>> getPaymentsByPatientId(@PathVariable Long patientId) {
        List<RazorpayPayment> payments = razorpayService.getPaymentsByPatientId(patientId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get payments by doctor ID
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<RazorpayPayment>> getPaymentsByDoctorId(@PathVariable Long doctorId) {
        List<RazorpayPayment> payments = razorpayService.getPaymentsByDoctorId(doctorId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Process refund for a payment
     */
    @PostMapping("/refund/{paymentId}")
    public ResponseEntity<Map<String, Object>> processRefund(
            @PathVariable String paymentId,
            @RequestParam @NotBlank String reason) {

        Map<String, Object> result = razorpayService.processRefund(paymentId, reason);
        return ResponseEntity.ok(result);
    }

    /**
     * GET Payment Details by paymentId (for frontend PaymentConfirmationPage)
     */
    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getPaymentDetails(@RequestParam String paymentId) {
        try {
            RazorpayPayment payment = razorpayService.getPaymentDetails(paymentId);

            // Map entity to DTO
            PaymentDetailsResponse dto = new PaymentDetailsResponse();
            dto.setPaymentId(payment.getRazorpayPaymentId());
            dto.setOrderId(payment.getRazorpayOrderId());
            dto.setStatus(payment.getStatus().name());
            dto.setAmount(payment.getAmount());
            dto.setCurrency(payment.getCurrency());
            dto.setMethod(payment.getPaymentMethod());
            dto.setCardLast4(payment.getCardLast4());
            dto.setCardNetwork(payment.getCardNetwork());
            dto.setEmail(payment.getEmail());
            dto.setContact(payment.getContact());
            dto.setCreatedAt(payment.getCreatedAt());
            dto.setCompletedAt(payment.getCompletedAt());
            dto.setRefundedAt(payment.getRefundedAt());
            dto.setRefundReason(payment.getRefundReason());
            dto.setAppointmentId(payment.getAppointment() != null ? payment.getAppointment().getId() : null);

            return ResponseEntity.ok(Map.of("success", true, "data", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to fetch payment details", "error", e.getMessage()));
        }
    }
}