package com.mediconnect.dto;

import java.math.BigDecimal;

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
}