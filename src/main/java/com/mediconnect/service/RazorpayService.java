package com.mediconnect.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediconnect.dto.RazorpayDTO.OrderResponse;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.razorpay.Refund;
import com.mediconnect.exception.PaymentProcessingException;
import com.mediconnect.model.Appointment;
import com.mediconnect.model.RazorpayPayment;
import com.mediconnect.model.RazorpayPayment.PaymentStatus;
import com.mediconnect.repository.AppointmentRepository;
import com.mediconnect.repository.RazorpayPaymentRepository;

@Service
public class RazorpayService {
    
    @Autowired
    private RazorpayClient razorpayClient;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private RazorpayPaymentRepository razorpayPaymentRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Value("${razorpay.key.id}")
    private String keyId;
    
    @Value("${razorpay.key.secret}")
    private String keySecret;
    
    @Value("${razorpay.currency}")
    private String currency;
    
    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;
    
    /**
     * Create Razorpay order for appointment payment
     */
    @Transactional
    public OrderResponse createOrder(Long appointmentId, BigDecimal amount) {
        try {
            // Validate appointment
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new PaymentProcessingException("Appointment not found with id: " + appointmentId));
            
            if (appointment.getIsPaid()) {
                throw new PaymentProcessingException("Appointment is already paid");
            }
            
            // Convert amount to paise/cents (as per currency)
            int amountInSmallestUnit = amount.multiply(new BigDecimal("100")).intValue();
            
            // Generate a unique receipt ID
            String receiptId = "rcpt_" + appointmentId + "_" + System.currentTimeMillis();
            
            // Create order request
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInSmallestUnit);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", receiptId);
            orderRequest.put("payment_capture", 1);
            
            // Add notes for reference
            JSONObject notes = new JSONObject();
            notes.put("appointmentId", appointmentId.toString());
            notes.put("patientId", appointment.getPatient().getId().toString());
            notes.put("doctorId", appointment.getDoctor().getId().toString());
            notes.put("patientName", appointment.getPatient().getUser().getFirstName() + " " + 
                    appointment.getPatient().getUser().getLastName());
            notes.put("doctorName", appointment.getDoctor().getUser().getFirstName() + " " + 
                    appointment.getDoctor().getUser().getLastName());
            orderRequest.put("notes", notes);
            
            // Create order in Razorpay
            Order order = razorpayClient.orders.create(orderRequest);
            
            // Save order details in our database
            RazorpayPayment paymentRecord = new RazorpayPayment();
            paymentRecord.setAppointment(appointment);
            paymentRecord.setAmount(amount);
            paymentRecord.setCurrency(currency);
            paymentRecord.setStatus(PaymentStatus.CREATED);
            paymentRecord.setRazorpayOrderId(order.get("id"));
            paymentRecord.setEmail(appointment.getPatient().getUser().getEmail());
            paymentRecord.setContact(appointment.getPatient().getUser().getPhoneNumber());
            razorpayPaymentRepository.save(paymentRecord);
            
            // Prepare response for frontend
            OrderResponse response = new OrderResponse();
            response.setOrderId(order.get("id"));
            response.setAmount(amountInSmallestUnit);
            response.setCurrency(order.get("currency"));
            response.setReceipt(order.get("receipt"));
            response.setKeyId(keyId);
            
            // Add customer details
            OrderResponse.CustomerDetails customer = new OrderResponse.CustomerDetails();
            customer.setName(appointment.getPatient().getUser().getFirstName() + " " + 
                    appointment.getPatient().getUser().getLastName());
            customer.setEmail(appointment.getPatient().getUser().getEmail());
            customer.setContact(appointment.getPatient().getUser().getPhoneNumber());
            response.setPrefill(customer);
            
            return response;
            
        } catch (RazorpayException e) {
            throw new PaymentProcessingException("Failed to create Razorpay order: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verify and process Razorpay payment
     */
    @Transactional
    public Map<String, Object> verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Find payment record
            RazorpayPayment paymentRecord = razorpayPaymentRepository.findByRazorpayOrderId(razorpayOrderId)
                    .orElseThrow(() -> new PaymentProcessingException("Payment record not found for order: " + razorpayOrderId));
            
            // Verify signature
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);
            
            // Use keySecret directly
            boolean isValidSignature = Utils.verifyPaymentSignature(options, keySecret);
            
            if (!isValidSignature) {
                paymentRecord.setStatus(PaymentStatus.FAILED);
                paymentRecord.setErrorMessage("Invalid payment signature");
                razorpayPaymentRepository.save(paymentRecord);
                
                result.put("success", false);
                result.put("message", "Payment verification failed");
                return result;
            }
            
            // Get payment details from Razorpay
            Payment razorpayPayment = razorpayClient.payments.fetch(razorpayPaymentId);
            
            // Update payment record
            paymentRecord.setRazorpayPaymentId(razorpayPaymentId);
            paymentRecord.setRazorpaySignature(razorpaySignature);
            paymentRecord.setPaymentMethod(razorpayPayment.get("method"));
            
            // Handle card details if available - using String manipulation instead of JSONObject
            if (razorpayPayment.has("card")) {
                String cardJson = razorpayPayment.get("card");
                // Simple parsing - for production, use a more robust approach
                if (cardJson != null && !cardJson.isEmpty()) {
                    try {
                        JSONObject card = new JSONObject(cardJson);
                        if (card.has("id")) paymentRecord.setCardId(card.getString("id"));
                        if (card.has("network")) paymentRecord.setCardNetwork(card.getString("network"));
                        if (card.has("last4")) paymentRecord.setCardLast4(card.getString("last4"));
                    } catch (Exception e) {
                        System.err.println("Error parsing card data: " + e.getMessage());
                    }
                }
            }
            
            // Update status based on Razorpay payment status
            String razorpayStatus = razorpayPayment.get("status");
            if ("captured".equals(razorpayStatus)) {
                paymentRecord.setStatus(PaymentStatus.CAPTURED);
                paymentRecord.setCompletedAt(LocalDateTime.now());
                
                // Update appointment as paid
                Appointment appointment = paymentRecord.getAppointment();
                appointment.setIsPaid(true);
                appointmentRepository.save(appointment);
                
                // Send payment confirmation email
                try {
                    sendPaymentConfirmationEmail(paymentRecord);
                } catch (Exception e) {
                    // Log but don't fail the transaction
                    System.err.println("Failed to send payment confirmation email: " + e.getMessage());
                }
                
                result.put("success", true);
                result.put("message", "Payment successful");
            } else if ("authorized".equals(razorpayStatus)) {
                paymentRecord.setStatus(PaymentStatus.AUTHORIZED);
                result.put("success", true);
                result.put("message", "Payment authorized but not captured");
            } else {
                paymentRecord.setStatus(PaymentStatus.FAILED);
                paymentRecord.setErrorMessage("Payment failed with status: " + razorpayStatus);
                result.put("success", false);
                result.put("message", "Payment failed");
            }
            
            // Save updated payment record
            razorpayPaymentRepository.save(paymentRecord);
            
            // Add additional details to response
            result.put("transactionId", razorpayPaymentId);
            result.put("amount", paymentRecord.getAmount().toString());
            result.put("email", paymentRecord.getEmail());
            
            return result;
            
        } catch (RazorpayException e) {
            throw new PaymentProcessingException("Payment verification failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Process Razorpay webhook events
     */
    @Transactional
    public void processWebhookEvent(String payload, String signature) {
        try {
            // Verify webhook signature
            boolean isValidSignature = Utils.verifyWebhookSignature(payload, signature, webhookSecret);
            
            if (!isValidSignature) {
                throw new PaymentProcessingException("Invalid webhook signature");
            }
            
            // Parse payload
            JSONObject payloadJson = new JSONObject(payload);
            String event = payloadJson.getString("event");
            
            // Process based on event type
            switch (event) {
                case "payment.authorized":
                    handlePaymentAuthorized(payloadJson);
                    break;
                case "payment.captured":
                    handlePaymentCaptured(payloadJson);
                    break;
                case "payment.failed":
                    handlePaymentFailed(payloadJson);
                    break;
                case "refund.created":
                    handleRefundCreated(payloadJson);
                    break;
                default:
                    // Log unhandled event
                    System.out.println("Unhandled Razorpay webhook event: " + event);
            }
            
        } catch (Exception e) {
            throw new PaymentProcessingException("Failed to process webhook: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle payment.authorized webhook event
     */
    private void handlePaymentAuthorized(JSONObject payload) {
        try {
            JSONObject paymentEntity = payload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
            String paymentId = paymentEntity.getString("id");
            String orderId = paymentEntity.getString("order_id");
            
            // Update payment record
            razorpayPaymentRepository.findByRazorpayOrderId(orderId).ifPresent(payment -> {
                payment.setRazorpayPaymentId(paymentId);
                payment.setStatus(PaymentStatus.AUTHORIZED);
                razorpayPaymentRepository.save(payment);
            });
            
        } catch (Exception e) {
            throw new PaymentProcessingException("Failed to process payment.authorized webhook: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle payment.captured webhook event
     */
    private void handlePaymentCaptured(JSONObject payload) {
        try {
            JSONObject paymentEntity = payload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
            String paymentId = paymentEntity.getString("id");
            String orderId = paymentEntity.getString("order_id");
            
            // Update payment record
            razorpayPaymentRepository.findByRazorpayOrderId(orderId).ifPresent(payment -> {
                payment.setRazorpayPaymentId(paymentId);
                payment.setStatus(PaymentStatus.CAPTURED);
                payment.setCompletedAt(LocalDateTime.now());
                
                // Get payment method details if available
                payment.setPaymentMethod(paymentEntity.getString("method"));
                
                if (paymentEntity.has("card") && !paymentEntity.isNull("card")) {
                    JSONObject card = paymentEntity.getJSONObject("card");
                    payment.setCardId(card.getString("id"));
                    payment.setCardNetwork(card.getString("network"));
                    payment.setCardLast4(card.getString("last4"));
                }
                
                razorpayPaymentRepository.save(payment);
                
                // Update appointment as paid
                Appointment appointment = payment.getAppointment();
                appointment.setIsPaid(true);
                appointmentRepository.save(appointment);
                
                // Send email notification
                try {
                    sendPaymentConfirmationEmail(payment);
                } catch (Exception e) {
                    // Log but don't fail the transaction
                    System.err.println("Failed to send payment confirmation email: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            throw new PaymentProcessingException("Failed to process payment.captured webhook: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle payment.failed webhook event
     */
    private void handlePaymentFailed(JSONObject payload) {
        try {
            JSONObject paymentEntity = payload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
            String paymentId = paymentEntity.getString("id");
            String orderId = paymentEntity.getString("order_id");
            String errorDescription = paymentEntity.has("error_description") ? 
                    paymentEntity.getString("error_description") : "Payment failed";
            
            // Update payment record
            razorpayPaymentRepository.findByRazorpayOrderId(orderId).ifPresent(payment -> {
                payment.setRazorpayPaymentId(paymentId);
                payment.setStatus(PaymentStatus.FAILED);
                payment.setErrorMessage(errorDescription);
                razorpayPaymentRepository.save(payment);
            });
            
        } catch (Exception e) {
            throw new PaymentProcessingException("Failed to process payment.failed webhook: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle refund.created webhook event
     */
    private void handleRefundCreated(JSONObject payload) {
        try {
            JSONObject refundEntity = payload.getJSONObject("payload").getJSONObject("refund").getJSONObject("entity");
            String paymentId = refundEntity.getString("payment_id");
            
            // Update payment record
            razorpayPaymentRepository.findByRazorpayPaymentId(paymentId).ifPresent(payment -> {
                payment.setStatus(PaymentStatus.REFUNDED);
                payment.setRefundedAt(LocalDateTime.now());
                
                // Set refund reason if available
                if (refundEntity.has("notes") && refundEntity.getJSONObject("notes").has("reason")) {
                    payment.setRefundReason(refundEntity.getJSONObject("notes").getString("reason"));
                }
                
                razorpayPaymentRepository.save(payment);
            });
            
        } catch (Exception e) {
            throw new PaymentProcessingException("Failed to process refund.created webhook: " + e.getMessage(), e);
        }
    }
    
    /**
     * Send payment confirmation email
     */
    private void sendPaymentConfirmationEmail(RazorpayPayment payment) throws Exception {
        Appointment appointment = payment.getAppointment();
        String patientEmail = payment.getEmail();
        
        if (patientEmail == null || patientEmail.isEmpty()) {
            patientEmail = appointment.getPatient().getUser().getEmail();
        }
        
        String patientName = appointment.getPatient().getUser().getFirstName() + " " + 
                appointment.getPatient().getUser().getLastName();
        String doctorName = appointment.getDoctor().getUser().getFirstName() + " " + 
                appointment.getDoctor().getUser().getLastName();
        
        String subject = "Payment Confirmation - MediConnect";
        String template = "payment-confirmation";
        
        Map<String, Object> mailProps = new HashMap<>();
        mailProps.put("patientName", patientName);
        mailProps.put("doctorName", doctorName);
        mailProps.put("appointmentDate", appointment.getAppointmentDateTime().toLocalDate().toString());
        mailProps.put("appointmentTime", appointment.getAppointmentDateTime().toLocalTime().toString());
        mailProps.put("transactionId", payment.getRazorpayPaymentId());
        mailProps.put("orderId", payment.getRazorpayOrderId());
        mailProps.put("amount", payment.getAmount().toString());
        mailProps.put("currency", payment.getCurrency());
        mailProps.put("paymentMethod", payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "Online");
        mailProps.put("paymentDate", payment.getCompletedAt().toString());
        
        emailService.sendTemplateMessage(patientEmail, subject, template, mailProps);
    }
    
    /**
     * Process refund for a payment
     */
    @Transactional
    public Map<String, Object> processRefund(String paymentId, String reason) {
        try {
            // Find payment record
            RazorpayPayment payment = razorpayPaymentRepository.findByRazorpayPaymentId(paymentId)
                    .orElseThrow(() -> new PaymentProcessingException("Payment not found with payment ID: " + paymentId));
            
            // Validate payment status
            if (payment.getStatus() != PaymentStatus.CAPTURED) {
                throw new PaymentProcessingException("Cannot refund payment with status: " + payment.getStatus());
            }
            
            // Process refund in Razorpay
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", payment.getAmount().multiply(new BigDecimal("100")).intValue());
            refundRequest.put("speed", "normal");
            
            // Add notes for the refund
            JSONObject notes = new JSONObject();
            notes.put("reason", reason);
            notes.put("appointmentId", payment.getAppointment().getId().toString());
            refundRequest.put("notes", notes);
            
            Refund refund = razorpayClient.payments.refund(paymentId, refundRequest);
            
            // Update payment status
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundReason(reason);
            payment.setRefundedAt(LocalDateTime.now());
            razorpayPaymentRepository.save(payment);
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("refundId", refund.get("id"));
            response.put("paymentId", paymentId);
            response.put("status", "refunded");
            response.put("amount", payment.getAmount().toString());
            
            return response;
            
        } catch (RazorpayException e) {
            throw new PaymentProcessingException("Error processing refund: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get payments by appointment ID
     */
    public List<RazorpayPayment> getPaymentsByAppointmentId(Long appointmentId) {
        return razorpayPaymentRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId);
    }
    
    /**
     * Get payments by patient ID
     */
    public List<RazorpayPayment> getPaymentsByPatientId(Long patientId) {
        return razorpayPaymentRepository.findByPatientId(patientId);
    }
    
    /**
     * Get payments by doctor ID
     */
    public List<RazorpayPayment> getPaymentsByDoctorId(Long doctorId) {
        return razorpayPaymentRepository.findByDoctorId(doctorId);
    }
}