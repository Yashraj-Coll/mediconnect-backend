package com.mediconnect.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
import com.mediconnect.model.LabTestBooking;
import com.mediconnect.repository.LabTestBookingRepository;
import com.mediconnect.config.ApplicationContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RazorpayService implements RazorpayServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(RazorpayService.class);

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private RazorpayPaymentRepository razorpayPaymentRepository;

    @Autowired
    private LabTestBookingRepository labTestBookingRepository;
    
    @Autowired
    private AppointmentNotificationService appointmentNotificationService;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.currency:INR}")
    private String defaultCurrency;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @Transactional
    public OrderResponse createOrder(
            Long appointmentId,
            BigDecimal amount,
            String currency,
            String name,
            String email,
            String contact) {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new PaymentProcessingException("Appointment not found with id: " + appointmentId));

            if (appointment.getIsPaid()) {
                throw new PaymentProcessingException("Appointment is already paid");
            }

            int amountInSmallestUnit = amount.multiply(new BigDecimal("100")).intValue();

            String actualCurrency = (currency != null && !currency.isEmpty()) ? currency : defaultCurrency;
            String receiptId = "rcpt_" + appointmentId + "_" + System.currentTimeMillis();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInSmallestUnit);
            orderRequest.put("currency", actualCurrency);
            orderRequest.put("receipt", receiptId);
            orderRequest.put("payment_capture", 1);

            JSONObject notes = new JSONObject();
            notes.put("appointmentId", appointmentId.toString());
            notes.put("patientId", appointment.getPatient().getId().toString());
            notes.put("doctorId", appointment.getDoctor().getId().toString());
            notes.put("patientName", appointment.getPatient().getUser().getFirstName() + " " +
                    appointment.getPatient().getUser().getLastName());
            notes.put("doctorName", appointment.getDoctor().getUser().getFirstName() + " " +
                    appointment.getDoctor().getUser().getLastName());
            orderRequest.put("notes", notes);

            Order order = razorpayClient.orders.create(orderRequest);

            RazorpayPayment paymentRecord = new RazorpayPayment();
            paymentRecord.setAppointment(appointment);
            paymentRecord.setAmount(amount);
            paymentRecord.setCurrency(actualCurrency);
            paymentRecord.setStatus(PaymentStatus.CREATED);
            paymentRecord.setRazorpayOrderId(order.get("id"));
            paymentRecord.setEmail((email != null && !email.isEmpty()) ? email : appointment.getPatient().getUser().getEmail());
            paymentRecord.setContact((contact != null && !contact.isEmpty()) ? contact : appointment.getPatient().getUser().getPhoneNumber());
            razorpayPaymentRepository.save(paymentRecord);

            OrderResponse response = new OrderResponse();
            response.setOrderId(order.get("id"));
            response.setAmount(amountInSmallestUnit);
            response.setCurrency(order.get("currency"));
            response.setReceipt(order.get("receipt"));
            response.setKeyId(keyId);

            OrderResponse.CustomerDetails customer = new OrderResponse.CustomerDetails();
            customer.setName((name != null && !name.isEmpty()) ? name :
                    (appointment.getPatient().getUser().getFirstName() + " " +
                            appointment.getPatient().getUser().getLastName()));
            customer.setEmail((email != null && !email.isEmpty()) ? email : appointment.getPatient().getUser().getEmail());
            customer.setContact((contact != null && !contact.isEmpty()) ? contact : appointment.getPatient().getUser().getPhoneNumber());
            response.setPrefill(customer);

            return response;

        } catch (RazorpayException e) {
            throw new PaymentProcessingException("Failed to create Razorpay order: " + e.getMessage(), e);
        }
    }
    
    public List<RazorpayPayment> getPaymentsByLabTestBookingId(Long labTestBookingId) {
        return razorpayPaymentRepository.findByLabTestBookingIdOrderByCreatedAtDesc(labTestBookingId);
    }
    
    @Transactional
    public OrderResponse createLabTestOrder(
            Long labTestBookingId,
            BigDecimal amount,
            String currency,
            String name,
            String email,
            String contact) {
        try {
            // Find lab test booking
            LabTestBooking labTestBooking = labTestBookingRepository.findById(labTestBookingId)
                    .orElseThrow(() -> new PaymentProcessingException("Lab test booking not found with id: " + labTestBookingId));

            if (labTestBooking.getIsPaid()) {
                throw new PaymentProcessingException("Lab test is already paid");
            }

            int amountInSmallestUnit = amount.multiply(new BigDecimal("100")).intValue();
            String actualCurrency = (currency != null && !currency.isEmpty()) ? currency : defaultCurrency;
            String receiptId = "labtest_" + labTestBookingId + "_" + System.currentTimeMillis();

            // Create Razorpay order
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInSmallestUnit);
            orderRequest.put("currency", actualCurrency);
            orderRequest.put("receipt", receiptId);
            orderRequest.put("payment_capture", 1);

            JSONObject notes = new JSONObject();
            notes.put("labTestBookingId", labTestBookingId.toString());
            notes.put("patientId", labTestBooking.getPatient().getId().toString());
            notes.put("testName", labTestBooking.getTestName());
            notes.put("bookingType", "LAB_TEST");
            orderRequest.put("notes", notes);

            Order order = razorpayClient.orders.create(orderRequest);

            // Save payment record
            RazorpayPayment paymentRecord = new RazorpayPayment();
            paymentRecord.setLabTestBooking(labTestBooking); // This sets bookingType to "LAB_TEST"
            paymentRecord.setAmount(amount);
            paymentRecord.setCurrency(actualCurrency);
            paymentRecord.setStatus(PaymentStatus.CREATED);
            paymentRecord.setRazorpayOrderId(order.get("id"));
            paymentRecord.setEmail((email != null && !email.isEmpty()) ? email : labTestBooking.getPatient().getUser().getEmail());
            paymentRecord.setContact((contact != null && !contact.isEmpty()) ? contact : labTestBooking.getPatient().getUser().getPhoneNumber());
            razorpayPaymentRepository.save(paymentRecord);

            // Build response
            OrderResponse response = new OrderResponse();
            response.setOrderId(order.get("id"));
            response.setAmount(amountInSmallestUnit);
            response.setCurrency(order.get("currency"));
            response.setReceipt(order.get("receipt"));
            response.setKeyId(keyId);

            OrderResponse.CustomerDetails customer = new OrderResponse.CustomerDetails();
            customer.setName((name != null && !name.isEmpty()) ? name :
                    (labTestBooking.getPatient().getUser().getFirstName() + " " +
                     labTestBooking.getPatient().getUser().getLastName()));
            customer.setEmail((email != null && !email.isEmpty()) ? email : labTestBooking.getPatient().getUser().getEmail());
            customer.setContact((contact != null && !contact.isEmpty()) ? contact : labTestBooking.getPatient().getUser().getPhoneNumber());
            response.setPrefill(customer);

            return response;

        } catch (RazorpayException e) {
            throw new PaymentProcessingException("Failed to create Razorpay lab test order: " + e.getMessage(), e);
        }
    }

    @Transactional
    public OrderResponse createOrder(Long appointmentId, BigDecimal amount) {
        return createOrder(appointmentId, amount, null, null, null, null);
    }

    @Transactional
    public Map<String, Object> verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            log.info("🔍 Starting payment verification for order: {}, payment: {}", razorpayOrderId, razorpayPaymentId);
            
            Map<String, Object> result = new HashMap<>();
            RazorpayPayment paymentRecord = razorpayPaymentRepository.findByRazorpayOrderId(razorpayOrderId)
                    .orElseThrow(() -> new PaymentProcessingException("Payment record not found for order: " + razorpayOrderId));
            
            // Verify signature
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);
            
            boolean isValidSignature = Utils.verifyPaymentSignature(options, keySecret);
            if (!isValidSignature) {
                paymentRecord.setStatus(PaymentStatus.FAILED);
                paymentRecord.setErrorMessage("Invalid payment signature");
                razorpayPaymentRepository.save(paymentRecord);
                result.put("success", false);
                result.put("message", "Payment verification failed");
                return result;
            }
            
            // Fetch payment details from Razorpay
            Payment razorpayPayment = razorpayClient.payments.fetch(razorpayPaymentId);
            paymentRecord.setRazorpayPaymentId(razorpayPaymentId);
            paymentRecord.setRazorpaySignature(razorpaySignature);
            paymentRecord.setPaymentMethod(razorpayPayment.get("method"));
            
            // Handle card details
            if (razorpayPayment.has("card")) {
                String cardJson = razorpayPayment.get("card");
                if (cardJson != null && !cardJson.isEmpty()) {
                    try {
                        JSONObject card = new JSONObject(cardJson);
                        if (card.has("id")) paymentRecord.setCardId(card.getString("id"));
                        if (card.has("network")) paymentRecord.setCardNetwork(card.getString("network"));
                        if (card.has("last4")) paymentRecord.setCardLast4(card.getString("last4"));
                    } catch (Exception e) {
                        log.error("Error parsing card data: {}", e.getMessage());
                    }
                }
            }
            
            String razorpayStatus = razorpayPayment.get("status");
            log.info("💳 Razorpay payment status: {} for payment: {}", razorpayStatus, razorpayPaymentId);
            
            if ("captured".equals(razorpayStatus)) {
                paymentRecord.setStatus(PaymentStatus.CAPTURED);
                paymentRecord.setCompletedAt(LocalDateTime.now());
                
                // 🔧 CRITICAL: Handle both LAB TEST and APPOINTMENT payments with same structure
                if ("LAB_TEST".equals(paymentRecord.getBookingType()) && paymentRecord.getLabTestBooking() != null) {
                    log.info("🧪 Processing lab test payment for booking: {}", paymentRecord.getLabTestBooking().getId());
                    
                    // Update lab test booking
                    LabTestBooking labTestBooking = paymentRecord.getLabTestBooking();
                    labTestBooking.setIsPaid(true);
                    labTestBooking.setStatus(LabTestBooking.BookingStatus.CONFIRMED);
                    labTestBookingRepository.save(labTestBooking);
                    
                    // Set lab test booking ID
                    result.put("labTestBookingId", labTestBooking.getId());
                    
                    // Create simplified lab test booking data (NO nested objects)
                    Map<String, Object> simpleLabTestBooking = new HashMap<>();
                    simpleLabTestBooking.put("id", labTestBooking.getId());
                    simpleLabTestBooking.put("testName", labTestBooking.getTestName());
                    simpleLabTestBooking.put("testPrice", labTestBooking.getTestPrice());
                    simpleLabTestBooking.put("sampleType", labTestBooking.getSampleType());
                    simpleLabTestBooking.put("homeCollection", labTestBooking.getHomeCollection());
                    simpleLabTestBooking.put("status", labTestBooking.getStatus().toString());
                    simpleLabTestBooking.put("isPaid", labTestBooking.getIsPaid());
                    
                    // Add simple patient info without deep nesting
                    if (labTestBooking.getPatient() != null && labTestBooking.getPatient().getUser() != null) {
                        simpleLabTestBooking.put("patientName", 
                            labTestBooking.getPatient().getUser().getFirstName() + " " + 
                            labTestBooking.getPatient().getUser().getLastName());
                        simpleLabTestBooking.put("patientEmail", 
                            labTestBooking.getPatient().getUser().getEmail());
                    }
                    
                    result.put("labTestBooking", simpleLabTestBooking);
                    
                    // Send lab test notifications asynchronously
                    final LabTestBooking finalLabTestBooking = labTestBooking;
                    final RazorpayPayment finalPaymentRecord = paymentRecord;
                    
                    CompletableFuture.runAsync(() -> {
                        try {
                            LabTestNotificationService labTestNotificationService = 
                                ApplicationContextProvider.getBean(LabTestNotificationService.class);
                            if (labTestNotificationService != null) {
                                labTestNotificationService.sendAllNotifications(finalPaymentRecord, finalLabTestBooking);
                            }
                            log.info("✅ Lab test notification process initiated for payment: {}", finalPaymentRecord.getId());
                        } catch (Exception e) {
                            log.error("❌ Failed to send lab test notifications for payment: {} - Error: {}", 
                                    finalPaymentRecord.getId(), e.getMessage());
                        }
                    });
                    
                } else if (paymentRecord.getAppointment() != null) {
                    log.info("🏥 Processing appointment payment for appointment: {}", paymentRecord.getAppointment().getId());
                    
                    // Update appointment
                    Appointment appointment = paymentRecord.getAppointment();
                    appointment.setIsPaid(true);
                    appointmentRepository.save(appointment);
                    
                    // Set appointment ID
                    result.put("appointmentId", appointment.getId());
                    
                    // 🔥 CRITICAL FIX: Create simplified appointment data (NO nested objects) - SAME AS LAB TEST
                    Map<String, Object> simpleAppointment = new HashMap<>();
                    simpleAppointment.put("id", appointment.getId());
                    simpleAppointment.put("appointmentType", appointment.getAppointmentType().toString());
                    simpleAppointment.put("appointmentDateTime", appointment.getAppointmentDateTime().toString());
                    simpleAppointment.put("status", appointment.getStatus().toString());
                    simpleAppointment.put("fee", appointment.getFee());
                    simpleAppointment.put("isPaid", appointment.getIsPaid());
                    simpleAppointment.put("videoRoomName", appointment.getVideoRoomName());
                    
                    // Add simple doctor info without deep nesting
                    if (appointment.getDoctor() != null && appointment.getDoctor().getUser() != null) {
                        simpleAppointment.put("doctorName", 
                            appointment.getDoctor().getUser().getFirstName() + " " + 
                            appointment.getDoctor().getUser().getLastName());
                        simpleAppointment.put("doctorSpecialization", 
                            appointment.getDoctor().getSpecialization());
                        simpleAppointment.put("doctorEmail", 
                            appointment.getDoctor().getUser().getEmail());
                    }
                    
                    // Add simple patient info without deep nesting
                    if (appointment.getPatient() != null && appointment.getPatient().getUser() != null) {
                        simpleAppointment.put("patientName", 
                            appointment.getPatient().getUser().getFirstName() + " " + 
                            appointment.getPatient().getUser().getLastName());
                        simpleAppointment.put("patientEmail", 
                            appointment.getPatient().getUser().getEmail());
                    }
                    
                    result.put("appointment", simpleAppointment);
                    
                    // Send appointment notifications asynchronously  
                    final Appointment finalAppointment = appointment;
                    final RazorpayPayment finalPaymentRecord = paymentRecord;
                    
                    CompletableFuture.runAsync(() -> {
                        try {
                            appointmentNotificationService.sendAllNotifications(finalPaymentRecord, finalAppointment);
                            log.info("✅ Appointment notification process initiated for payment: {}", finalPaymentRecord.getId());
                        } catch (Exception e) {
                            log.error("❌ Failed to send appointment notifications for payment: {} - Error: {}", 
                                    finalPaymentRecord.getId(), e.getMessage());
                        }
                    });
                }
                
                result.put("success", true);
                result.put("message", "Payment successful");
                
            } else if ("authorized".equals(razorpayStatus)) {
                paymentRecord.setStatus(PaymentStatus.AUTHORIZED);
                result.put("success", true);
                result.put("message", "Payment authorized but not captured");
                
                // Add simplified data for authorized payments
                if (paymentRecord.getAppointment() != null) {
                    Appointment appointment = paymentRecord.getAppointment();
                    result.put("appointmentId", appointment.getId());
                } else if (paymentRecord.getLabTestBooking() != null) {
                    LabTestBooking labTestBooking = paymentRecord.getLabTestBooking();
                    result.put("labTestBookingId", labTestBooking.getId());
                }
                
            } else {
                paymentRecord.setStatus(PaymentStatus.FAILED);
                paymentRecord.setErrorMessage("Payment failed with status: " + razorpayStatus);
                result.put("success", false);
                result.put("message", "Payment failed");
            }
            
            // Save payment record
            razorpayPaymentRepository.save(paymentRecord);
            
            // Add common response fields
            result.put("transactionId", razorpayPaymentId);
            result.put("amount", paymentRecord.getAmount().toString());
            result.put("email", paymentRecord.getEmail());
            
            log.info("✅ Payment verification completed successfully for payment: {}", razorpayPaymentId);
            return result;
            
        } catch (RazorpayException e) {
            log.error("❌ Payment verification failed for payment: {} - Error: {}", razorpayPaymentId, e.getMessage());
            throw new PaymentProcessingException("Payment verification failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void processWebhookEvent(String payload, String signature) {
        try {
            boolean isValidSignature = Utils.verifyWebhookSignature(payload, signature, webhookSecret);
            if (!isValidSignature) {
                throw new PaymentProcessingException("Invalid webhook signature");
            }
            JSONObject payloadJson = new JSONObject(payload);
            String event = payloadJson.getString("event");
            
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
                    log.info("Unhandled Razorpay webhook event: {}", event);
            }
        } catch (Exception e) {
            throw new PaymentProcessingException("Failed to process webhook: " + e.getMessage(), e);
        }
    }

    private void handlePaymentAuthorized(JSONObject payload) {
        try {
            JSONObject paymentEntity = payload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
            String paymentId = paymentEntity.getString("id");
            String orderId = paymentEntity.getString("order_id");
            
            razorpayPaymentRepository.findByRazorpayOrderId(orderId).ifPresent(payment -> {
                payment.setRazorpayPaymentId(paymentId);
                payment.setStatus(PaymentStatus.AUTHORIZED);
                razorpayPaymentRepository.save(payment);
            });
        } catch (Exception e) {
            throw new PaymentProcessingException("Failed to process payment.authorized webhook: " + e.getMessage(), e);
        }
    }

    private void handlePaymentCaptured(JSONObject payload) {
        try {
            JSONObject paymentEntity = payload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
            String paymentId = paymentEntity.getString("id");
            String orderId = paymentEntity.getString("order_id");
            
            razorpayPaymentRepository.findByRazorpayOrderId(orderId).ifPresent(payment -> {
                payment.setRazorpayPaymentId(paymentId);
                payment.setStatus(PaymentStatus.CAPTURED);
                payment.setCompletedAt(LocalDateTime.now());
                payment.setPaymentMethod(paymentEntity.getString("method"));
                
                if (paymentEntity.has("card") && !paymentEntity.isNull("card")) {
                    JSONObject card = paymentEntity.getJSONObject("card");
                    payment.setCardId(card.getString("id"));
                    payment.setCardNetwork(card.getString("network"));
                    payment.setCardLast4(card.getString("last4"));
                }
                
                razorpayPaymentRepository.save(payment);
                
                if (payment.getAppointment() != null) {
                    Appointment appointment = payment.getAppointment();
                    appointment.setIsPaid(true);
                    appointmentRepository.save(appointment);
                    
                    log.info("Payment captured via webhook for appointment: {} - Notifications already sent via frontend verification", appointment.getId());
                } else if (payment.getLabTestBooking() != null) {
                    LabTestBooking labTestBooking = payment.getLabTestBooking();
                    labTestBooking.setIsPaid(true);
                    labTestBooking.setStatus(LabTestBooking.BookingStatus.CONFIRMED);
                    labTestBookingRepository.save(labTestBooking);
                    
                    log.info("Payment captured via webhook for lab test: {} - Notifications already sent via frontend verification", labTestBooking.getId());
                }
            });
        } catch (Exception e) {
            throw new PaymentProcessingException("Failed to process payment.captured webhook: " + e.getMessage(), e);
        }
    }

    private void handlePaymentFailed(JSONObject payload) {
        try {
            JSONObject paymentEntity = payload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
            String paymentId = paymentEntity.getString("id");
            String orderId = paymentEntity.getString("order_id");
            String errorDescription = paymentEntity.has("error_description") ?
                    paymentEntity.getString("error_description") : "Payment failed";
            
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

    private void handleRefundCreated(JSONObject payload) {
        try {
            JSONObject refundEntity = payload.getJSONObject("payload").getJSONObject("refund").getJSONObject("entity");
            String paymentId = refundEntity.getString("payment_id");
            String refundId = refundEntity.getString("id");
            
            log.info("Refund created for payment: {} with refund ID: {}", paymentId, refundId);
            // Additional refund handling logic can be added here
        } catch (Exception e) {
            log.error("Failed to process refund.created webhook: ", e);
        }
    }

    public List<RazorpayPayment> getPaymentsByAppointmentId(Long appointmentId) {
        return razorpayPaymentRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId);
    }

    public List<RazorpayPayment> getPaymentsByPatientId(Long patientId) {
        return razorpayPaymentRepository.findByPatientId(patientId);
    }

    public List<RazorpayPayment> getPaymentsByDoctorId(Long doctorId) {
        return razorpayPaymentRepository.findByDoctorId(doctorId);
    }

    public Map<String, Object> processRefund(String paymentId, String reason) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Find the payment record
            RazorpayPayment paymentRecord = razorpayPaymentRepository.findByRazorpayPaymentId(paymentId)
                    .orElseThrow(() -> new PaymentProcessingException("Payment not found: " + paymentId));
            
            // Create refund request
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", paymentRecord.getAmount().multiply(new BigDecimal("100")).intValue());
            refundRequest.put("speed", "normal");
            
            JSONObject notes = new JSONObject();
            notes.put("reason", reason);
            notes.put("refund_date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            refundRequest.put("notes", notes);
            
            // Process refund with Razorpay
            Refund refund = razorpayClient.payments.refund(paymentId, refundRequest);
            
            // Update payment record
            paymentRecord.setStatus(PaymentStatus.REFUNDED);
            paymentRecord.setRefundReason(reason);
            paymentRecord.setRefundedAt(LocalDateTime.now());
            razorpayPaymentRepository.save(paymentRecord);
            
            result.put("success", true);
            result.put("refundId", refund.get("id"));
            result.put("amount", refund.get("amount"));
            result.put("status", refund.get("status"));
            
            log.info("Refund processed successfully for payment: {}", paymentId);
            return result;
            
        } catch (RazorpayException e) {
            log.error("Refund processing failed for payment: {} - Error: {}", paymentId, e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Refund processing failed: " + e.getMessage());
            return result;
        }
    }

    @Transactional(readOnly = true)
    public RazorpayPayment getPaymentDetails(String paymentId) {
        return razorpayPaymentRepository.findByRazorpayPaymentId(paymentId)
            .orElseThrow(() -> new PaymentProcessingException("Payment details not found for ID: " + paymentId));
    }
}