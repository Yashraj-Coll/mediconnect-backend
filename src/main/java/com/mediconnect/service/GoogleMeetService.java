package com.mediconnect.service;

import com.mediconnect.model.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GoogleMeetService {

    private static final Logger log = LoggerFactory.getLogger(GoogleMeetService.class);

    @Value("${mediconnect.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * DEPRECATED: Google Meet integration disabled in favor of Jitsi Meet
     * This method now returns a Jitsi Meet link instead
     */
    public String createMeetingForAppointment(Appointment appointment) {
        log.warn("Google Meet integration disabled. Using Jitsi Meet for appointment: {}", appointment.getId());
        
        // Generate Jitsi room name
        String jitsiRoomName = appointment.getVideoRoomName();
        if (jitsiRoomName == null || jitsiRoomName.trim().isEmpty()) {
            jitsiRoomName = String.format("mediconnect-doctor-%d-patient-%d-%d", 
                appointment.getDoctor().getId(), 
                appointment.getPatient().getId(), 
                appointment.getId());
        }
        
        // Return Jitsi Meet link instead of Google Meet
        String jitsiLink = String.format("%s/video-consultation/%d", frontendUrl, appointment.getId());
        
        log.info("Generated Jitsi Meet link for appointment {}: {}", appointment.getId(), jitsiLink);
        return jitsiLink;
    }

    /**
     * DEPRECATED: No longer creates Google Calendar events
     */
    public void createCalendarEvent(Appointment appointment) {
        log.warn("Google Calendar integration disabled for appointment: {}", appointment.getId());
        // This method is now a no-op
    }

    /**
     * DEPRECATED: No longer cancels Google Calendar events
     */
    public void cancelCalendarEvent(String eventId) {
        log.warn("Google Calendar event cancellation disabled for eventId: {}", eventId);
        // This method is now a no-op
    }
}