package com.mediconnect.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;
    
    @Column(name = "sender_id", nullable = false)
    private Long senderId;
    
    @Column(name = "sender_type", nullable = false)
    private String senderType; // "DOCTOR" or "PATIENT"
    
    @Column(name = "sender_name", nullable = false)
    private String senderName;
    
    @Column(name = "message_text", columnDefinition = "TEXT", nullable = false)
    private String messageText;
    
    @Column(name = "timestamp", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    // Default constructor
    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }

    // Constructor with parameters
    public ChatMessage(Long appointmentId, Long senderId, String senderType, 
                      String senderName, String messageText) {
        this.appointmentId = appointmentId;
        this.senderId = senderId;
        this.senderType = senderType;
        this.senderName = senderName;
        this.messageText = messageText;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "id=" + id +
                ", appointmentId=" + appointmentId +
                ", senderId=" + senderId +
                ", senderType='" + senderType + '\'' +
                ", senderName='" + senderName + '\'' +
                ", messageText='" + messageText + '\'' +
                ", timestamp=" + timestamp +
                ", isRead=" + isRead +
                '}';
    }
}