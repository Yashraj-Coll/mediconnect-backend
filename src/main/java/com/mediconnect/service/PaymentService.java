package com.mediconnect.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediconnect.exception.PaymentProcessingException;
import com.mediconnect.model.Appointment;
import com.mediconnect.model.Payment;
import com.mediconnect.model.Payment.PaymentStatus;
import com.mediconnect.repository.AppointmentRepository;
import com.mediconnect.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.annotation.PostConstruct;

@Service
public class PaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private EmailService emailService;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }
    
    /**
     * Create a payment intent for direct payment processing
     */
    @Transactional
    public Map<String, Object> createPaymentIntent(Long appointmentId, BigDecimal amount, String currency) {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new PaymentProcessingException("Appointment not found"));
            
            if (appointment.getIsPaid()) {
                throw new PaymentProcessingException("Appointment is already paid");
            }
            
            // Create payment record with PENDING status
            Payment payment = new Payment();
            payment.setAppointment(appointment);
            payment.setAmount(amount);
            payment.setCurrency(currency);
            payment.setStatus(PaymentStatus.PENDING);
            payment = paymentRepository.save(payment);
            
            // Create payment intent with Stripe
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount.multiply(new BigDecimal("100")).longValue()) // convert to cents
                    .setCurrency(currency)
                    .putMetadata("appointmentId", appointmentId.toString())
                    .putMetadata("paymentId", payment.getId().toString())
                    .setDescription("Payment for appointment #" + appointmentId)
                    .build();
            
            PaymentIntent intent = PaymentIntent.create(params);
            
            // Update payment record with Stripe payment intent ID
            payment.setPaymentIntentId(intent.getId());
            paymentRepository.save(payment);
            
            // Return client secret to complete payment on frontend
            Map<String, Object> response = new HashMap<>();
            response.put("clientSecret", intent.getClientSecret());
            response.put("paymentId", payment.getId());
            
            return response;
        } catch (StripeException e) {
            throw new PaymentProcessingException("Stripe error: " + e.getMessage());
        }
    }
    
    /**
     * Create a checkout session for payment via Stripe Checkout
     */
    @Transactional
    public String createCheckoutSession(Long appointmentId, String successUrl, String cancelUrl) {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new PaymentProcessingException("Appointment not found"));
            
            if (appointment.getIsPaid()) {
                throw new PaymentProcessingException("Appointment is already paid");
            }
            
            // Create payment record with PENDING status
            Payment payment = new Payment();
            payment.setAppointment(appointment);
            payment.setAmount(BigDecimal.valueOf(appointment.getFee()));
            payment.setCurrency("USD"); // Default currency
            payment.setStatus(PaymentStatus.PENDING);
            payment = paymentRepository.save(payment);
            
            // Build line items
            List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
            lineItems.add(
                SessionCreateParams.LineItem.builder()
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("USD")
                            .setUnitAmount(payment.getAmount().multiply(new BigDecimal("100")).longValue())
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName("Appointment with Dr. " + appointment.getDoctor().getUser().getLastName())
                                    .setDescription("Appointment on " + appointment.getAppointmentDateTime())
                                    .build()
                            )
                            .build()
                    )
                    .setQuantity(1L)
                    .build()
            );
            
            // Create checkout session
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .addAllLineItem(lineItems)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .putMetadata("appointmentId", appointmentId.toString())
                    .putMetadata("paymentId", payment.getId().toString())
                    .build();
            
            Session session = Session.create(params);
            
            // Update payment record with Stripe session ID
            payment.setCheckoutSessionId(session.getId());
            paymentRepository.save(payment);
            
            return session.getUrl();
        } catch (StripeException e) {
            throw new PaymentProcessingException("Stripe error: " + e.getMessage());
        }
    }
    
    /**
     * Process a successful payment (called by webhook)
     */
    @Transactional
    public void processSuccessfulPayment(String paymentIntentId) {
        Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new PaymentProcessingException("Payment not found for intent ID: " + paymentIntentId));
        
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCompletedAt(java.time.LocalDateTime.now());
        paymentRepository.save(payment);
        
        // Update appointment as paid
        Appointment appointment = payment.getAppointment();
        appointment.setIsPaid(true);
        appointmentRepository.save(appointment);
        
        // Send confirmation email
        try {
            sendPaymentConfirmationEmail(payment);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Failed to send payment confirmation email: " + e.getMessage());
        }
    }
    
    /**
     * Process a failed payment (called by webhook)
     */
    @Transactional
    public void processFailedPayment(String paymentIntentId, String errorMessage) {
        Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new PaymentProcessingException("Payment not found for intent ID: " + paymentIntentId));
        
        payment.setStatus(PaymentStatus.FAILED);
        payment.setErrorMessage(errorMessage);
        paymentRepository.save(payment);
    }
    
    /**
     * Process a payment from a checkout session (called by webhook)
     */
    @Transactional
    public void processCheckoutSession(String sessionId) {
        Payment payment = paymentRepository.findByCheckoutSessionId(sessionId)
                .orElseThrow(() -> new PaymentProcessingException("Payment not found for session ID: " + sessionId));
        
        try {
            Session session = Session.retrieve(sessionId);
            
            String sessionStatus = session.toJson().contains("\"status\":\"complete\"") ? "complete" : 
                (session.toJson().contains("\"status\":\"completed\"") ? "completed" : "");
                
if ("complete".equals(sessionStatus) || "completed".equals(sessionStatus)) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setCompletedAt(java.time.LocalDateTime.now());
                payment.setPaymentIntentId(session.getPaymentIntent());
                paymentRepository.save(payment);
                
                // Update appointment as paid
                Appointment appointment = payment.getAppointment();
                appointment.setIsPaid(true);
                appointmentRepository.save(appointment);
                
                // Send confirmation email
                try {
                    sendPaymentConfirmationEmail(payment);
                } catch (Exception e) {
                    // Log error but don't fail the transaction
                    System.err.println("Failed to send payment confirmation email: " + e.getMessage());
                }
            }
        } catch (StripeException e) {
            throw new PaymentProcessingException("Error retrieving checkout session: " + e.getMessage());
        }
    }
    
    public String getStripeApiKey() {
		return stripeApiKey;
	}

	public void setStripeApiKey(String stripeApiKey) {
		this.stripeApiKey = stripeApiKey;
	}

	public AppointmentRepository getAppointmentRepository() {
		return appointmentRepository;
	}

	public void setAppointmentRepository(AppointmentRepository appointmentRepository) {
		this.appointmentRepository = appointmentRepository;
	}

	public PaymentRepository getPaymentRepository() {
		return paymentRepository;
	}

	public void setPaymentRepository(PaymentRepository paymentRepository) {
		this.paymentRepository = paymentRepository;
	}

	public EmailService getEmailService() {
		return emailService;
	}

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	/**
     * Send payment confirmation email
     */
    private void sendPaymentConfirmationEmail(Payment payment) {
        Appointment appointment = payment.getAppointment();
        String recipientEmail = appointment.getPatient().getUser().getEmail();
        String subject = "Payment Confirmation - MediConnect";
        
        String content = String.format(
            "Dear %s,\n\n" +
            "Thank you for your payment of %s %s for your appointment with Dr. %s on %s.\n\n" +
            "Payment Details:\n" +
            "- Amount: %s %s\n" +
            "- Date: %s\n" +
            "- Transaction ID: %s\n\n" +
            "Your appointment has been fully confirmed. We look forward to seeing you!\n\n" +
            "Best regards,\n" +
            "The MediConnect Team",
            appointment.getPatient().getUser().getFirstName(),
            payment.getAmount(),
            payment.getCurrency(),
            appointment.getDoctor().getUser().getLastName(),
            appointment.getAppointmentDateTime(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getCompletedAt(),
            payment.getPaymentIntentId()
        );
        
        emailService.sendSimpleMessage(recipientEmail, subject, content);
    }
    
    /**
     * Get payment history for an appointment
     */
    public List<Payment> getPaymentHistoryForAppointment(Long appointmentId) {
        return paymentRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId);
    }
    
    /**
     * Get all payments by status
     */
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }
    
    /**
     * Refund a payment
     */
    @Transactional
    public void refundPayment(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentProcessingException("Payment not found"));
                
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentProcessingException("Only completed payments can be refunded");
        }
        
        try {
            PaymentIntent intent = PaymentIntent.retrieve(payment.getPaymentIntentId());
            Map<String, Object> params = new HashMap<>();
            params.put("payment_intent", intent.getId());
            com.stripe.model.Refund.create(params);
            
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundReason(reason);
            payment.setRefundedAt(java.time.LocalDateTime.now());
            paymentRepository.save(payment);
            
            // Update appointment as unpaid
            Appointment appointment = payment.getAppointment();
            appointment.setIsPaid(false);
            appointmentRepository.save(appointment);
            
        } catch (StripeException e) {
            throw new PaymentProcessingException("Error refunding payment: " + e.getMessage());
        }
    }
}