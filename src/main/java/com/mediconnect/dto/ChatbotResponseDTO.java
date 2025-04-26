package com.mediconnect.dto;

import java.util.Map;

import lombok.Data;

@Data
public class ChatbotResponseDTO {
    
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

	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}

	public boolean isActionRequired() {
		return actionRequired;
	}

	public void setActionRequired(boolean actionRequired) {
		this.actionRequired = actionRequired;
	}

	public Map<String, Object> getActionData() {
		return actionData;
	}

	public void setActionData(Map<String, Object> actionData) {
		this.actionData = actionData;
	}

	private String sessionId;
    
    private String message;
    
    private String intent;
    
    private boolean actionRequired;
    
    private Map<String, Object> actionData;
}