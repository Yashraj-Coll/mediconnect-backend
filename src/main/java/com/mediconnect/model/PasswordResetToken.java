package com.mediconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 6)
    private String otp;
    
    @Column(nullable = false)
    private LocalDateTime expiryDate;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime usedAt;
    
    @Column(nullable = false)
    private boolean isUsed = false;
    
    @Column(nullable = false)
    private int attemptCount = 0;
    
    @Column(nullable = false)
    private int maxAttempts = 3;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType tokenType = TokenType.PASSWORD_RESET;
    
    public enum TokenType {
        PASSWORD_RESET,
        EMAIL_VERIFICATION,
        PHONE_VERIFICATION
    }
    
    // Constructors
    public PasswordResetToken() {
        this.createdAt = LocalDateTime.now();
        this.expiryDate = LocalDateTime.now().plusMinutes(15); // 15 minutes validity
    }
    
    public PasswordResetToken(User user, String otp) {
        this();
        this.user = user;
        this.otp = otp;
    }
    
    public PasswordResetToken(User user, String otp, TokenType tokenType) {
        this(user, otp);
        this.tokenType = tokenType;
    }
    
    // Helper methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
    
    public boolean isValid() {
        return !isUsed && !isExpired() && attemptCount < maxAttempts;
    }
    
    public void markAsUsed() {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }
    
    public void incrementAttempt() {
        this.attemptCount++;
    }
    
    public boolean hasExceededMaxAttempts() {
        return this.attemptCount >= this.maxAttempts;
    }
    
    public long getMinutesUntilExpiry() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiryDate).toMinutes();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getOtp() {
        return otp;
    }
    
    public void setOtp(String otp) {
        this.otp = otp;
    }
    
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUsedAt() {
        return usedAt;
    }
    
    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
    
    public boolean isUsed() {
        return isUsed;
    }
    
    public void setUsed(boolean used) {
        isUsed = used;
    }
    
    public int getAttemptCount() {
        return attemptCount;
    }
    
    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }
    
    public int getMaxAttempts() {
        return maxAttempts;
    }
    
    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
    
    public TokenType getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }
}