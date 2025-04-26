package com.mediconnect.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mediconnect.dto.PaymentIntentDTO;
import com.mediconnect.model.Payment;
import com.mediconnect.model.Payment.PaymentStatus;
import com.mediconnect.service.PaymentService;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;
    
    /**
     * Create payment intent for direct payment
     */
    @PostMapping("/create-payment-intent")
    @PreAuthorize("hasRole('PATIENT') or @securityService.canAccessAppointment(#paymentIntentDTO.getAppointmentId())")
    public ResponseEntity<Map<String, Object>> createPaymentIntent(@Valid @RequestBody PaymentIntentDTO paymentIntentDTO) {
        Map<String, Object> clientSecret = paymentService.createPaymentIntent(
                paymentIntentDTO.getAppointmentId(),
                paymentIntentDTO.getAmount(),
                paymentIntentDTO.getCurrency());
        
        return new ResponseEntity<>(clientSecret, HttpStatus.OK);
    }
    
    /**
     * Create checkout session for payment via Stripe Checkout
     */
    @PostMapping("/create-checkout-session")
    @PreAuthorize("hasRole('PATIENT') or @securityService.canAccessAppointment(#paymentIntentDTO.getAppointmentId())")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@Valid @RequestBody PaymentIntentDTO paymentIntentDTO) {
        String sessionUrl = paymentService.createCheckoutSession(
                paymentIntentDTO.getAppointmentId(),
                paymentIntentDTO.getSuccessUrl(),
                paymentIntentDTO.getCancelUrl());
        
        Map<String, String> response = Map.of("url", sessionUrl);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    /**
     * Webhook for Stripe events
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestParam("signature") String sigHeader) {
        try {
            // This is your Stripe CLI webhook secret for testing
            String endpointSecret = "whsec_your_webhook_secret";
            
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            
            // Handle the event
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
                    StripeObject stripeObject = dataObjectDeserializer.getObject().orElse(null);
                    if (stripeObject instanceof PaymentIntent) {
                        PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                        // Process payment success
                        paymentService.processSuccessfulPayment(paymentIntent.getId());
                    }
                    break;
                case "payment_intent.payment_failed":
                    dataObjectDeserializer = event.getDataObjectDeserializer();
                    stripeObject = dataObjectDeserializer.getObject().orElse(null);
                    if (stripeObject instanceof PaymentIntent) {
                        PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                        // Process payment failure
                        String errorMessage = paymentIntent.getLastPaymentError() != null ? 
                                paymentIntent.getLastPaymentError().getMessage() : "Unknown error";
                        paymentService.processFailedPayment(paymentIntent.getId(), errorMessage);
                    }
                    break;
                case "checkout.session.completed":
                    dataObjectDeserializer = event.getDataObjectDeserializer();
                    stripeObject = dataObjectDeserializer.getObject().orElse(null);
                    if (stripeObject instanceof Session) {
                        Session session = (Session) stripeObject;
                        // Process checkout session completion
                        paymentService.processCheckoutSession(session.getId());
                    }
                    break;
                default:
                    // Unexpected event type
                    System.out.println("Unhandled event type: " + event.getType());
            }
            
            return new ResponseEntity<>("Webhook received", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Webhook error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Get payment history for an appointment
     */
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.canAccessAppointment(#appointmentId)")
    public ResponseEntity<List<Payment>> getPaymentHistoryForAppointment(@PathVariable Long appointmentId) {
        List<Payment> payments = paymentService.getPaymentHistoryForAppointment(appointmentId);
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }
    
    /**
     * Get all payments by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }
    
    /**
     * Refund a payment
     */
    @PutMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> refundPayment(@PathVariable Long paymentId, @RequestBody Map<String, String> refundData) {
        String reason = refundData.getOrDefault("reason", "Requested by admin");
        paymentService.refundPayment(paymentId, reason);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}