package com.mediconnect.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.mediconnect.dto.RazorpayDTO.OrderResponse;
import com.mediconnect.model.RazorpayPayment;

public interface RazorpayServiceInterface {
    
    /**
     * Create Razorpay order for appointment payment (Simple version)
     * 
     * @param appointmentId Appointment ID
     * @param amount Payment amount
     * @return Order response with details for frontend
     */
    OrderResponse createOrder(Long appointmentId, BigDecimal amount);
    
    /**
     * Create Razorpay order for appointment payment (Full version)
     * 
     * @param appointmentId Appointment ID
     * @param amount Payment amount
     * @param currency Currency code
     * @param name Customer name
     * @param email Customer email
     * @param contact Customer contact
     * @return Order response with details for frontend
     */
    OrderResponse createOrder(Long appointmentId, BigDecimal amount, String currency, String name, String email, String contact);
    
    /**
     * Verify and process Razorpay payment
     * 
     * @param razorpayOrderId Razorpay order ID
     * @param razorpayPaymentId Razorpay payment ID
     * @param razorpaySignature Razorpay signature
     * @return Map with verification results
     */
    Map<String, Object> verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature);
    
    /**
     * Process Razorpay webhook events
     * 
     * @param payload Event payload
     * @param signature Webhook signature
     */
    void processWebhookEvent(String payload, String signature);
    
    /**
     * Process refund for a payment
     * 
     * @param paymentId Razorpay payment ID
     * @param reason Refund reason
     * @return Map with refund results
     */
    Map<String, Object> processRefund(String paymentId, String reason);
    
    /**
     * Get all payments for an appointment
     * 
     * @param appointmentId Appointment ID
     * @return List of payments
     */
    List<RazorpayPayment> getPaymentsByAppointmentId(Long appointmentId);
    
    /**
     * Get payments by patient ID
     * 
     * @param patientId Patient ID
     * @return List of payments
     */
    List<RazorpayPayment> getPaymentsByPatientId(Long patientId);
    
    /**
     * Get payments by doctor ID
     * 
     * @param doctorId Doctor ID
     * @return List of payments
     */
    List<RazorpayPayment> getPaymentsByDoctorId(Long doctorId);
    RazorpayPayment getPaymentDetails(String paymentId);
    OrderResponse createLabTestOrder(Long labTestBookingId, BigDecimal amount, String currency, String name, String email, String contact);
    List<RazorpayPayment> getPaymentsByLabTestBookingId(Long labTestBookingId);
}