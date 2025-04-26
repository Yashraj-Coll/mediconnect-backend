package com.mediconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mediconnect.dto.GoogleMeetDTO;
import com.mediconnect.model.VideoSession;
import com.mediconnect.service.GoogleMeetService;
import com.mediconnect.service.VideoService;

@RestController
@RequestMapping("/api/google-meet")
public class GoogleMeetController {

    @Autowired
    private GoogleMeetService googleMeetService;
    
    @Autowired
    private VideoService videoService;
    
    /**
     * Create a Google Meet meeting for a video session
     */
    @PostMapping("/sessions/{id}/meeting")
    @PreAuthorize("hasRole('ADMIN') or @securityService.canAccessVideoSession(#id)")
    public ResponseEntity<GoogleMeetDTO> createMeeting(@PathVariable Long id) {
        try {
            VideoSession session = videoService.getVideoSessionById(id);
            GoogleMeetDTO meetDTO = googleMeetService.createMeeting(session);
            return new ResponseEntity<>(meetDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Delete a Google Meet meeting
     */
    @DeleteMapping("/meetings/{eventId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteMeeting(@PathVariable String eventId) {
        try {
            googleMeetService.deleteMeeting(eventId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}