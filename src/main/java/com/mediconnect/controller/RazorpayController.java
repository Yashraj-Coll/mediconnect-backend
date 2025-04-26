package com.mediconnect.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mediconnect.dto.RazorpayDTO.OrderResponse;
import com.mediconnect.model.RazorpayPayment;
import com.mediconnect.service.RazorpayService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/payments/razorpay")
public class RazorpayController {
    
    @Autowired
    private RazorpayService razorpayService;
    
    /**
     * Create a new Razorpay order for appointment payment
     * 
     * @param appointmentId Appointment ID
     * @param amount Payment amount
     * @return Order response with payment details
     */
    @PostMapping("/order")
    public ResponseEntity<OrderResponse> createOrder(
            @RequestParam @NotNull Long appointmentId,
            @RequestParam @NotNull BigDecimal amount) {
        
        OrderResponse order = razorpayService.createOrder(appointmentId, amount);
        return ResponseEntity.ok(order);
    }
    
    /**
     * Verify payment after completion
     * 
     * @param razorpayOrderId Razorpay order ID
     * @param razorpayPaymentId Razorpay payment ID  
     * @param razorpaySignature Razorpay signature
     * @return Verification result
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @RequestParam @NotBlank String razorpayOrderId,
            @RequestParam @NotBlank String razorpayPaymentId,
            @RequestParam @NotBlank String razorpaySignature) {
        
        Map<String, Object> result = razorpayService.verifyPayment(
                razorpayOrderId, razorpayPaymentId, razorpaySignature);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Handle Razorpay webhook events
     * 
     * @param payload Event payload
     * @param signature Webhook signature
     * @return Success response
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
     * 
     * @param appointmentId Appointment ID
     * @return List of payments
     */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<RazorpayPayment>> getPaymentsByAppointmentId(@PathVariable Long appointmentId) {
        List<RazorpayPayment> payments = razorpayService.getPaymentsByAppointmentId(appointmentId);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Get payments by patient ID
     * 
     * @param patientId Patient ID
     * @return List of payments
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<RazorpayPayment>> getPaymentsByPatientId(@PathVariable Long patientId) {
        List<RazorpayPayment> payments = razorpayService.getPaymentsByPatientId(patientId);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Get payments by doctor ID
     * 
     * @param doctorId Doctor ID
     * @return List of payments
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<RazorpayPayment>> getPaymentsByDoctorId(@PathVariable Long doctorId) {
        List<RazorpayPayment> payments = razorpayService.getPaymentsByDoctorId(doctorId);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Process refund for a payment
     * 
     * @param paymentId Razorpay payment ID
     * @param reason Refund reason
     * @return Refund details
     */
    @PostMapping("/refund/{paymentId}")
    public ResponseEntity<Map<String, Object>> processRefund(
            @PathVariable String paymentId,
            @RequestParam @NotBlank String reason) {
        
        Map<String, Object> result = razorpayService.processRefund(paymentId, reason);
        return ResponseEntity.ok(result);
    }
}