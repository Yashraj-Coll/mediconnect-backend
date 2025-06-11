package com.mediconnect.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mediconnect.dto.ForgotPasswordRequest;
import com.mediconnect.dto.PasswordResetResponse;
import com.mediconnect.dto.ResetPasswordRequest;
import com.mediconnect.dto.ResendOtpRequest;
import com.mediconnect.dto.VerifyOtpRequest;
import com.mediconnect.service.PasswordResetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    /**
     * Step 1: Send OTP for password reset
     * POST /api/auth/password-reset/send-otp
     */
    @PostMapping("/send-otp")
    public ResponseEntity<PasswordResetResponse> sendPasswordResetOtp(
            @Valid @RequestBody ForgotPasswordRequest request) {
        
        logger.info("Password reset OTP requested for identifier: {}", 
                   maskIdentifier(request.getIdentifier()));
        
        try {
            PasswordResetResponse response = passwordResetService
                .sendPasswordResetOtp(request.getIdentifier());
            
            // Always return 200 to prevent user enumeration
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error in sendPasswordResetOtp: {}", e.getMessage());
            return ResponseEntity.ok(
                PasswordResetResponse.error("An error occurred. Please try again.")
            );
        }
    }
    
    /**
     * Step 2: Verify OTP
     * POST /api/auth/password-reset/verify-otp
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<PasswordResetResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        
        logger.info("OTP verification requested for identifier: {}", 
                   maskIdentifier(request.getIdentifier()));
        
        try {
            PasswordResetResponse response = passwordResetService
                .verifyOtp(request.getIdentifier(), request.getOtp());
            
            HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(response);
            
        } catch (Exception e) {
            logger.error("Error in verifyOtp: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PasswordResetResponse.error("An error occurred. Please try again."));
        }
    }
    
    /**
     * Step 3: Reset password using verified OTP
     * POST /api/auth/password-reset/reset
     */
    @PostMapping("/reset")
    public ResponseEntity<PasswordResetResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        
        logger.info("Password reset requested for identifier: {}", 
                   maskIdentifier(request.getIdentifier()));
        
        try {
            PasswordResetResponse response = passwordResetService.resetPassword(
                request.getIdentifier(),
                request.getOtp(),
                request.getNewPassword(),
                request.getConfirmPassword()
            );
            
            HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(response);
            
        } catch (Exception e) {
            logger.error("Error in resetPassword: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PasswordResetResponse.error("An error occurred. Please try again."));
        }
    }
    
    /**
     * Resend OTP if the previous one expired or was not received
     * POST /api/auth/password-reset/resend-otp
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<PasswordResetResponse> resendOtp(
            @Valid @RequestBody ResendOtpRequest request) {
        
        logger.info("OTP resend requested for identifier: {}", 
                   maskIdentifier(request.getIdentifier()));
        
        try {
            PasswordResetResponse response = passwordResetService
                .resendOtp(request.getIdentifier());
            
            // Always return 200 to prevent user enumeration
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error in resendOtp: {}", e.getMessage());
            return ResponseEntity.ok(
                PasswordResetResponse.error("An error occurred. Please try again.")
            );
        }
    }
    
    /**
     * Health check endpoint for password reset service
     * GET /api/auth/password-reset/health
     */
    @GetMapping("/health")
    public ResponseEntity<PasswordResetResponse> healthCheck() {
        return ResponseEntity.ok(
            PasswordResetResponse.success("Password reset service is running")
        );
    }
    
    /**
     * Get password reset flow information
     * GET /api/auth/password-reset/info
     */
    @GetMapping("/info")
    public ResponseEntity<?> getPasswordResetInfo() {
        return ResponseEntity.ok(new PasswordResetInfo());
    }
    
    // ============ PRIVATE HELPER METHODS ============
    
    /**
     * Mask identifier for logging (security)
     */
    private String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() < 4) {
            return "***";
        }
        
        if (identifier.contains("@")) {
            // Email masking: test@example.com -> t***@e***.com
            String[] parts = identifier.split("@");
            if (parts.length == 2) {
                String username = parts[0];
                String domain = parts[1];
                
                String maskedUsername = username.length() > 2 ? 
                    username.charAt(0) + "***" : "***";
                
                String maskedDomain = domain.length() > 4 ? 
                    domain.charAt(0) + "***" + domain.substring(domain.lastIndexOf('.')) : "***";
                
                return maskedUsername + "@" + maskedDomain;
            }
        } else {
            // Phone masking: 1234567890 -> 123***7890
            if (identifier.length() >= 6) {
                return identifier.substring(0, 3) + "***" + 
                       identifier.substring(identifier.length() - 4);
            }
        }
        
        return "***";
    }
    
    // ============ INNER CLASSES ============
    
    /**
     * Information about the password reset flow
     */
    public static class PasswordResetInfo {
        private String flowDescription = "3-step password reset process";
        private String[] steps = {
            "1. Send OTP to email/phone",
            "2. Verify OTP",
            "3. Reset password"
        };
        private int otpExpiryMinutes = 15;
        private int maxAttemptsPerOtp = 3;
        private int maxOtpRequestsPerHour = 5;
        private int maxOtpRequestsPerDay = 10;
        
        // Getters
        public String getFlowDescription() { return flowDescription; }
        public String[] getSteps() { return steps; }
        public int getOtpExpiryMinutes() { return otpExpiryMinutes; }
        public int getMaxAttemptsPerOtp() { return maxAttemptsPerOtp; }
        public int getMaxOtpRequestsPerHour() { return maxOtpRequestsPerHour; }
        public int getMaxOtpRequestsPerDay() { return maxOtpRequestsPerDay; }
    }
}