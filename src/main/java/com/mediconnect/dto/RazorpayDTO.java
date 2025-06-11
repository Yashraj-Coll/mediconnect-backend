package com.mediconnect.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RazorpayDTO {

    /**
     * Request DTO for creating Razorpay order
     */
    public static class OrderRequest {
        @NotNull
        private Long appointmentId;

        @NotNull
        @DecimalMin(value = "1.00")
        private BigDecimal amount;

        @NotBlank
        private String currency;

        private String name;
        private String email;
        private String contact;

        // Getters and setters
        public Long getAppointmentId() {
            return appointmentId;
        }

        public void setAppointmentId(Long appointmentId) {
            this.appointmentId = appointmentId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getContact() {
            return contact;
        }

        public void setContact(String contact) {
            this.contact = contact;
        }
    }

    /**
     * Response DTO for Razorpay order creation
     */
    public static class OrderResponse {
        private String orderId;
        private int amount;
        private String currency;
        private String keyId;
        private String receipt;
        private CustomerDetails prefill;

        public static class CustomerDetails {
            private String name;
            private String email;
            private String contact;

            // Getters and setters
            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }

            public String getContact() {
                return contact;
            }

            public void setContact(String contact) {
                this.contact = contact;
            }
        }

        // Getters and setters
        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getKeyId() {
            return keyId;
        }

        public void setKeyId(String keyId) {
            this.keyId = keyId;
        }

        public String getReceipt() {
            return receipt;
        }

        public void setReceipt(String receipt) {
            this.receipt = receipt;
        }

        public CustomerDetails getPrefill() {
            return prefill;
        }

        public void setPrefill(CustomerDetails prefill) {
            this.prefill = prefill;
        }
    }

    /**
     * Request DTO for payment verification
     */
    public static class PaymentVerificationRequest {
        @NotBlank
        private String razorpayOrderId;

        @NotBlank
        private String razorpayPaymentId;

        @NotBlank
        private String razorpaySignature;

        // Getters and setters
        public String getRazorpayOrderId() {
            return razorpayOrderId;
        }

        public void setRazorpayOrderId(String razorpayOrderId) {
            this.razorpayOrderId = razorpayOrderId;
        }

        public String getRazorpayPaymentId() {
            return razorpayPaymentId;
        }

        public void setRazorpayPaymentId(String razorpayPaymentId) {
            this.razorpayPaymentId = razorpayPaymentId;
        }

        public String getRazorpaySignature() {
            return razorpaySignature;
        }

        public void setRazorpaySignature(String razorpaySignature) {
            this.razorpaySignature = razorpaySignature;
        }
    }

    /**
     * Response DTO for payment verification
     */
    public static class PaymentVerificationResponse {
        private boolean success;
        private String message;
        private String transactionId;
        private String amount;
        private String email;

        // Getters and setters
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    /**
     * DTO for Payment Details Response (for /details endpoint)
     */
    public static class PaymentDetailsResponse {
        private String paymentId;
        private String orderId;
        private String status;
        private BigDecimal amount;
        private String currency;
        private String method;
        private String cardLast4;
        private String cardNetwork;
        private String email;
        private String contact;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;
        private LocalDateTime refundedAt;
        private String refundReason;
        private Long appointmentId;

        // Getters and setters
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }

        public String getCardLast4() { return cardLast4; }
        public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }

        public String getCardNetwork() { return cardNetwork; }
        public void setCardNetwork(String cardNetwork) { this.cardNetwork = cardNetwork; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getContact() { return contact; }
        public void setContact(String contact) { this.contact = contact; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

        public LocalDateTime getRefundedAt() { return refundedAt; }
        public void setRefundedAt(LocalDateTime refundedAt) { this.refundedAt = refundedAt; }

        public String getRefundReason() { return refundReason; }
        public void setRefundReason(String refundReason) { this.refundReason = refundReason; }

        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    }
}
