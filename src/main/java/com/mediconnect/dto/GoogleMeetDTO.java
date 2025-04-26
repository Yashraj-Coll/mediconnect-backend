package com.mediconnect.dto;

public class GoogleMeetDTO {
    
    private String sessionId;
    private String meetLink;
    private String eventId;
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getMeetLink() {
        return meetLink;
    }
    
    public void setMeetLink(String meetLink) {
        this.meetLink = meetLink;
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}