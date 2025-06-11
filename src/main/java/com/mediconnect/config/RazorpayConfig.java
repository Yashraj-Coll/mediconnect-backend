package com.mediconnect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class RazorpayConfig {
    
    private static final Logger log = LoggerFactory.getLogger(RazorpayConfig.class);
    
    @Value("${razorpay.key.id}")
    private String keyId;
    
    @Value("${razorpay.key.secret}")
    private String keySecret;
    
    @Bean
    public RazorpayClient razorpayClient() {
        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            log.info("Razorpay client initialized successfully with key: {}", keyId);
            return client;
        } catch (RazorpayException e) {
            log.error("Failed to initialize Razorpay client: {}", e.getMessage());
            // Returning null would cause beans to fail to initialize
            // Instead, throw a runtime exception
            throw new RuntimeException("Failed to initialize Razorpay client", e);
        }
    }
}