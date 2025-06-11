package com.mediconnect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled tasks for password reset token cleanup and maintenance
 */
@Service
public class PasswordResetScheduledTasks {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetScheduledTasks.class);
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    /**
     * Clean up expired tokens every hour
     * This helps keep the database clean and improves performance
     */
    @Scheduled(fixedRate = 3600000) // Every hour (3600000 ms)
    public void cleanupExpiredTokens() {
        logger.info("Starting scheduled cleanup of expired password reset tokens");
        try {
            passwordResetService.cleanupExpiredTokens();
            logger.info("Completed scheduled cleanup of expired password reset tokens");
        } catch (Exception e) {
            logger.error("Error during scheduled token cleanup: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Health check log every 24 hours
     * This helps monitor the password reset service health
     */
    @Scheduled(fixedRate = 86400000) // Every 24 hours (86400000 ms)
    public void logPasswordResetServiceHealth() {
        logger.info("Password reset service health check - Service is running normally");
    }
}