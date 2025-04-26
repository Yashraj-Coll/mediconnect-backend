package com.mediconnect.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediconnect.dto.GoogleMeetDTO;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.model.Appointment;
import com.mediconnect.model.Appointment.AppointmentStatus;
import com.mediconnect.model.Appointment.AppointmentType;
import com.mediconnect.model.VideoSession;
import com.mediconnect.model.VideoSession.SessionStatus;
import com.mediconnect.repository.AppointmentRepository;
import com.mediconnect.repository.VideoSessionRepository;

@Service
public class VideoService {
    
    @Autowired
    private VideoSessionRepository videoSessionRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private GoogleMeetService googleMeetService;
    
    public List<VideoSession> getAllVideoSessions() {
        return videoSessionRepository.findAll();
    }
    
    public VideoSession getVideoSessionById(Long id) {
        return videoSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video session not found with id: " + id));
    }
    
    public Optional<VideoSession> getVideoSessionByAppointmentId(Long appointmentId) {
        return videoSessionRepository.findByAppointmentId(appointmentId);
    }
    
    public Optional<VideoSession> getVideoSessionBySessionId(String sessionId) {
        return videoSessionRepository.findBySessionId(sessionId);
    }
    
    public List<VideoSession> getVideoSessionsByStatus(SessionStatus status) {
        return videoSessionRepository.findByStatus(status);
    }
    
    public List<VideoSession> getVideoSessionsByScheduledDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return videoSessionRepository.findByScheduledDateRange(startDate, endDate);
    }
    
    public List<VideoSession> getVideoSessionsByDoctorAndStatus(Long doctorId, SessionStatus status) {
        return videoSessionRepository.findByDoctorIdAndStatus(doctorId, status);
    }
    
    public List<VideoSession> getVideoSessionsByPatientAndStatus(Long patientId, SessionStatus status) {
        return videoSessionRepository.findByPatientIdAndStatus(patientId, status);
    }
    
    public List<VideoSession> getUpcomingVideoSessions(int hoursAhead) {
        return videoSessionRepository.findUpcomingSessions(
                LocalDateTime.now(), 
                LocalDateTime.now().plusHours(hoursAhead)
        );
    }
    
    public List<VideoSession> getSessionsWithRecording() {
        return videoSessionRepository.findSessionsWithRecording();
    }
    
    @Transactional
    public VideoSession createVideoSessionForAppointment(Long appointmentId, boolean recordingEnabled) {
        // Check if appointment exists and is of type VIDEO_CALL
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
        
        if (appointment.getAppointmentType() != AppointmentType.VIDEO_CALL) {
            throw new BadRequestException("Cannot create video session for non-video appointment");
        }
        
        // Check if a video session already exists for this appointment
        if (videoSessionRepository.findByAppointmentId(appointmentId).isPresent()) {
            throw new BadRequestException("Video session already exists for this appointment");
        }
        
        // Create a new video session
        VideoSession videoSession = new VideoSession();
        videoSession.setAppointment(appointment);
        videoSession.setSessionId(generateUniqueSessionId());
        videoSession.setDoctorToken(generateToken("doctor", appointment.getId()));
        videoSession.setPatientToken(generateToken("patient", appointment.getId()));
        videoSession.setStatus(SessionStatus.SCHEDULED);
        videoSession.setScheduledStartTime(appointment.getAppointmentDateTime());
        videoSession.setRecordingEnabled(recordingEnabled);
        
        // Update appointment status to CONFIRMED if it's not already
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            appointmentRepository.save(appointment);
        }
        
        // Save the video session
        VideoSession savedSession = videoSessionRepository.save(videoSession);
        
        // Create Google Meet meeting
        try {
            GoogleMeetDTO meetDTO = googleMeetService.createMeeting(savedSession);
            System.out.println("Created Google Meet meeting: " + meetDTO.getMeetLink() + " for session: " + meetDTO.getSessionId());
            // Meeting link is already saved in the session notes by the GoogleMeetService
            
            // Send notifications with Google Meet link
            notificationService.sendVideoSessionDetailsToDoctor(savedSession);
            notificationService.sendVideoSessionDetailsToPatient(savedSession);
        } catch (Exception e) {
            // If Google Meet creation fails, still continue with the normal flow
            // but log the error and note it in the session
            e.printStackTrace();
            savedSession.setSessionNotes("Failed to create Google Meet link: " + e.getMessage());
            videoSessionRepository.save(savedSession);
            
            // Send normal notifications
            notificationService.sendVideoSessionDetailsToDoctor(savedSession);
            notificationService.sendVideoSessionDetailsToPatient(savedSession);
        }
        
        return savedSession;
    }
    
    @Transactional
    public VideoSession startVideoSession(Long sessionId) {
        VideoSession session = getVideoSessionById(sessionId);
        
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new BadRequestException("Cannot start a session that is not in SCHEDULED status");
        }
        
        session.setStatus(SessionStatus.ONGOING);
        session.setActualStartTime(LocalDateTime.now());
        
        return videoSessionRepository.save(session);
    }
    
    @Transactional
    public VideoSession endVideoSession(Long sessionId) {
        VideoSession session = getVideoSessionById(sessionId);
        
        if (session.getStatus() != SessionStatus.ONGOING) {
            throw new BadRequestException("Cannot end a session that is not in ONGOING status");
        }
        
        session.setStatus(SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());
        
        // Calculate duration in minutes
        if (session.getActualStartTime() != null) {
            long durationMinutes = java.time.Duration.between(
                    session.getActualStartTime(), 
                    session.getEndTime()
            ).toMinutes();
            
            session.setDurationMinutes((int) durationMinutes);
        }
        
        // Update appointment status
        Appointment appointment = session.getAppointment();
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
        
        return videoSessionRepository.save(session);
    }
    
    @Transactional
    public VideoSession updateVideoSession(Long id, VideoSession updatedSession) {
        VideoSession session = getVideoSessionById(id);
        
        if (updatedSession.getSessionNotes() != null) {
            session.setSessionNotes(updatedSession.getSessionNotes());
        }
        
        if (updatedSession.getTechnicalIssues() != null) {
            session.setTechnicalIssues(updatedSession.getTechnicalIssues());
        }
        
        if (updatedSession.getConnectionQuality() != null) {
            session.setConnectionQuality(updatedSession.getConnectionQuality());
        }
        
        if (updatedSession.getRecordingUrl() != null) {
            session.setRecordingUrl(updatedSession.getRecordingUrl());
        }
        
        if (updatedSession.getRecordingEnabled() != null) {
            session.setRecordingEnabled(updatedSession.getRecordingEnabled());
        }
        
        return videoSessionRepository.save(session);
    }
    
    @Transactional
    public VideoSession cancelVideoSession(Long id, String reason) {
        VideoSession session = getVideoSessionById(id);
        
        if (session.getStatus() == SessionStatus.COMPLETED || session.getStatus() == SessionStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel a completed or already cancelled session");
        }
        
        // Extract Google Meet event ID if it exists
        String sessionNotes = session.getSessionNotes();
        if (sessionNotes != null && sessionNotes.contains("Google Meet Link: https://meet.google.com/")) {
            try {
                // This is a simplified way to extract the event ID
                // In a real app, you might store it more formally
                // Here we're assuming the event ID is stored somewhere in the notes or can be derived
                // For now, we'll just log that we would delete the meeting
                System.out.println("Would delete Google Meet for session: " + session.getSessionId());
                // If you have the event ID stored, you would call:
                // googleMeetService.deleteMeeting(eventId);
            } catch (Exception e) {
                // Log error but continue with cancellation
                e.printStackTrace();
            }
        }
        
        session.setStatus(SessionStatus.CANCELLED);
        session.setSessionNotes((sessionNotes != null ? sessionNotes + "\n" : "") + "Cancellation reason: " + reason);
        
        // Update appointment status
        Appointment appointment = session.getAppointment();
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        
        // Send notifications
        notificationService.sendVideoSessionCancellationToDoctor(session, reason);
        notificationService.sendVideoSessionCancellationToPatient(session, reason);
        
        return videoSessionRepository.save(session);
    }
    
    // Helper methods
    private String generateUniqueSessionId() {
        return UUID.randomUUID().toString();
    }
    
    private String generateToken(String role, Long appointmentId) {
        // In a real application, this would generate a secure token for video service authentication
        return role + "-" + UUID.randomUUID().toString() + "-" + appointmentId;
    }
}