package com.mediconnect.dto;

import lombok.Data;

@Data
public class ChatbotMessageDTO {
    
    public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	private String sessionId;
    
    private String message;
    
    private String messageType;
}