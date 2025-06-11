package com.mediconnect.dto;

public class PasswordResetResponse {
    
    private boolean success;
    private String message;
    private Object data;
    private int remainingAttempts;
    private Integer expiryMinutes;
    
    public PasswordResetResponse() {}
    
    public PasswordResetResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public PasswordResetResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    public static PasswordResetResponse success(String message) {
        return new PasswordResetResponse(true, message);
    }
    
    public static PasswordResetResponse success(String message, Object data) {
        return new PasswordResetResponse(true, message, data);
    }
    
    public static PasswordResetResponse error(String message) {
        return new PasswordResetResponse(false, message);
    }
    
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
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public int getRemainingAttempts() {
        return remainingAttempts;
    }
    
    public void setRemainingAttempts(int remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }
    
    public Integer getExpiryMinutes() {
        return expiryMinutes;
    }
    
    public void setExpiryMinutes(Integer expiryMinutes) {
        this.expiryMinutes = expiryMinutes;
    }
}