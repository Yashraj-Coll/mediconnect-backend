package com.mediconnect.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mediconnect.dto.VideoSessionMessageDTO;
import com.mediconnect.model.VideoSession;
import com.mediconnect.model.VideoSession.SessionStatus;
import com.mediconnect.service.VideoService;

@RestController
@RequestMapping("/api/video-sessions")
public class VideoConsultationController {

    @Autowired
    private VideoService videoService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Get all video sessions
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VideoSession>> getAllVideoSessions() {
        List<VideoSession> sessions = videoService.getAllVideoSessions();
        return new ResponseEntity<>(sessions, HttpStatus.OK);
    }

    /**
     * Get video session by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.canAccessVideoSession(#id)")
    public ResponseEntity<VideoSession> getVideoSessionById(@PathVariable Long id) {
        VideoSession session = videoService.getVideoSessionById(id);
        return new ResponseEntity<>(session, HttpStatus.OK);
    }

    /**
     * Get video session by appointment ID
     */
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.canAccessAppointment(#appointmentId)")
    public ResponseEntity<VideoSession> getVideoSessionByAppointmentId(@PathVariable Long appointmentId) {
        return videoService.getVideoSessionByAppointmentId(appointmentId)
                .map(session -> new ResponseEntity<>(session, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Get video sessions by session ID
     */
    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.canAccessVideoSessionBySessionId(#sessionId)")
    public ResponseEntity<VideoSession> getVideoSessionBySessionId(@PathVariable String sessionId) {
        return videoService.getVideoSessionBySessionId(sessionId)
                .map(session -> new ResponseEntity<>(session, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Get video sessions by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VideoSession>> getVideoSessionsByStatus(@PathVariable SessionStatus status) {
        List<VideoSession> sessions = videoService.getVideoSessionsByStatus(status);
        return new ResponseEntity<>(sessions, HttpStatus.OK);
    }

    /**
     * Get video sessions by scheduled date range
     */
    @GetMapping("/scheduled")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<VideoSession>> getVideoSessionsByScheduledDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<VideoSession> sessions = videoService.getVideoSessionsByScheduledDateRange(startDate, endDate);
        return new ResponseEntity<>(sessions, HttpStatus.OK);
    }

    /**
     * Get video sessions by doctor and status
     */
    @GetMapping("/doctor/{doctorId}/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isDoctorWithId(#doctorId)")
    public ResponseEntity<List<VideoSession>> getVideoSessionsByDoctorAndStatus(
            @PathVariable Long doctorId,
            @PathVariable SessionStatus status) {
        List<VideoSession> sessions = videoService.getVideoSessionsByDoctorAndStatus(doctorId, status);
        return new ResponseEntity<>(sessions, HttpStatus.OK);
    }

    /**
     * Get video sessions by patient and status
     */
    @GetMapping("/patient/{patientId}/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isPatientWithId(#patientId)")
    public ResponseEntity<List<VideoSession>> getVideoSessionsByPatientAndStatus(
            @PathVariable Long patientId,
            @PathVariable SessionStatus status) {
        List<VideoSession> sessions = videoService.getVideoSessionsByPatientAndStatus(patientId, status);
        return new ResponseEntity<>(sessions, HttpStatus.OK);
    }

    /**
     * Get upcoming video sessions
     */
    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<VideoSession>> getUpcomingVideoSessions(@RequestParam int hoursAhead) {
        List<VideoSession> sessions = videoService.getUpcomingVideoSessions(hoursAhead);
        return new ResponseEntity<>(sessions, HttpStatus.OK);
    }

    /**
     * Create video session for appointment
     */
    @PostMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<VideoSession> createVideoSessionForAppointment(
            @PathVariable Long appointmentId,
            @RequestParam(defaultValue = "false") boolean recordingEnabled) {
        VideoSession session = videoService.createVideoSessionForAppointment(appointmentId, recordingEnabled);
        return new ResponseEntity<>(session, HttpStatus.CREATED);
    }

    /**
     * Start video session
     */
    @PutMapping("/{id}/start")
    @PreAuthorize("hasRole('ADMIN') or @securityService.canAccessVideoSession(#id)")
    public ResponseEntity<VideoSession> startVideoSession(@PathVariable Long id) {
        VideoSession session = videoService.startVideoSession(id);
        
        // Notify participants that session has started
        sendVideoSessionUpdateNotification(session, "SESSION_STARTED");
        
        return new ResponseEntity<>(session, HttpStatus.OK);
    }

    /**
     * End video session
     */
    @PutMapping("/{id}/end")
    @PreAuthorize("hasRole('ADMIN') or @securityService.canAccessVideoSession(#id)")
    public ResponseEntity<VideoSession> endVideoSession(@PathVariable Long id) {
        VideoSession session = videoService.endVideoSession(id);
        
        // Notify participants that session has ended
        sendVideoSessionUpdateNotification(session, "SESSION_ENDED");
        
        return new ResponseEntity<>(session, HttpStatus.OK);
    }

    /**
     * Update video session notes
     */
    @PutMapping("/{id}/notes")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isDoctorForVideoSession(#id)")
    public ResponseEntity<VideoSession> updateVideoSessionNotes(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        VideoSession updatedSession = new VideoSession();
        if (payload.containsKey("sessionNotes")) {
            updatedSession.setSessionNotes(payload.get("sessionNotes"));
        }
        if (payload.containsKey("technicalIssues")) {
            updatedSession.setTechnicalIssues(payload.get("technicalIssues"));
        }
        
        VideoSession session = videoService.updateVideoSession(id, updatedSession);
        return new ResponseEntity<>(session, HttpStatus.OK);
    }

    /**
     * Cancel video session
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @securityService.canAccessVideoSession(#id)")
    public ResponseEntity<VideoSession> cancelVideoSession(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String reason = payload.getOrDefault("reason", "Canceled by user");
        VideoSession session = videoService.cancelVideoSession(id, reason);
        
        // Notify participants that session has been canceled
        sendVideoSessionUpdateNotification(session, "SESSION_CANCELED");
        
        return new ResponseEntity<>(session, HttpStatus.OK);
    }

    /**
     * Video session WebSocket message handling
     */
    @MessageMapping("/video-sessions/message")
    public void handleVideoSessionMessage(@Payload VideoSessionMessageDTO message) {
        // Forward the message to the appropriate session channel
        messagingTemplate.convertAndSend(
                "/topic/video-session/" + message.getSessionId(), message);
    }
    
    /**
     * Helper method to send session update notifications via WebSocket
     */
    private void sendVideoSessionUpdateNotification(VideoSession session, String eventType) {
        VideoSessionMessageDTO message = new VideoSessionMessageDTO();
        message.setSessionId(session.getSessionId());
        message.setType(eventType);
        message.setTimestamp(LocalDateTime.now());
        
        messagingTemplate.convertAndSend(
                "/topic/video-session/" + session.getSessionId(), message);
    }
}