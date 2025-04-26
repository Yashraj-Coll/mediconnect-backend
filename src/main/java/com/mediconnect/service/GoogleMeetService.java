package com.mediconnect.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.ConferenceSolutionKey;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.mediconnect.dto.GoogleMeetDTO;
import com.mediconnect.model.Appointment;
import com.mediconnect.model.VideoSession;
import com.mediconnect.repository.VideoSessionRepository;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class GoogleMeetService {

    @Value("${google.service-account-key}")
    private Resource serviceAccountKey;
    
    @Autowired
    private VideoSessionRepository videoSessionRepository;
    
    /**
     * Create a Google Meet meeting for a video session
     */
    public GoogleMeetDTO createMeeting(VideoSession session) throws Exception {
        // Load credentials from service account key file
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(serviceAccountKey.getFile()))
                .createScoped(List.of("https://www.googleapis.com/auth/calendar"));
        
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
        
        // Initialize the Calendar service
        Calendar service = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer)
                .setApplicationName("MediConnect")
                .build();
        
        // Get appointment and participants information
        Appointment appointment = session.getAppointment();
        
        // Access email information from Doctor and Patient through the User object
        String doctorEmail = appointment.getDoctor().getUser().getEmail();
        String patientEmail = appointment.getPatient().getUser().getEmail();
        
        // Create a descriptive title for the meeting
        String doctorName = appointment.getDoctor().getUser().getFirstName() + " " + 
                           appointment.getDoctor().getUser().getLastName();
        String patientName = appointment.getPatient().getUser().getFirstName() + " " + 
                            appointment.getPatient().getUser().getLastName();
        
        String meetingTitle = "Medical Consultation: Dr. " + doctorName + " with " + patientName;
        
        // Convert appointment datetime to Date for Google Calendar
        LocalDateTime appointmentDateTime = appointment.getAppointmentDateTime();
        Date startDate = Date.from(appointmentDateTime.atZone(ZoneId.systemDefault()).toInstant());
        
        // Calculate end time using appointment duration
        Integer durationMinutes = appointment.getDurationMinutes() != null ? appointment.getDurationMinutes() : 30;
        Date endDate = Date.from(appointmentDateTime.plusMinutes(durationMinutes).atZone(ZoneId.systemDefault()).toInstant());
        
        // Create a new Google Calendar event with Google Meet conference
        Event event = new Event()
                .setSummary(meetingTitle)
                .setDescription("Video consultation for appointment #" + appointment.getId())
                .setStart(new EventDateTime().setDateTime(new DateTime(startDate)))
                .setEnd(new EventDateTime().setDateTime(new DateTime(endDate)))
                .setAttendees(Arrays.asList(
                        new EventAttendee().setEmail(doctorEmail),
                        new EventAttendee().setEmail(patientEmail)
                ))
                .setConferenceData(new ConferenceData()
                        .setCreateRequest(new CreateConferenceRequest()
                                .setRequestId(session.getSessionId())
                                .setConferenceSolutionKey(new ConferenceSolutionKey()
                                        .setType("hangoutsMeet"))));
        
        // Insert the event and get the conference details
        event = service.events().insert("primary", event)
                .setConferenceDataVersion(1)
                .execute();
        
        ConferenceData conferenceData = event.getConferenceData();
        String meetLink = null;
        if (conferenceData != null && conferenceData.getEntryPoints() != null && !conferenceData.getEntryPoints().isEmpty()) {
            meetLink = conferenceData.getEntryPoints().get(0).getUri();
        }
        
        // Save the meeting link to the video session
        session.setSessionNotes("Google Meet Link: " + meetLink);
        videoSessionRepository.save(session);
        
        // Create DTO with meeting information
        GoogleMeetDTO meetDTO = new GoogleMeetDTO();
        meetDTO.setSessionId(session.getSessionId());
        meetDTO.setMeetLink(meetLink);
        meetDTO.setEventId(event.getId());
        
        return meetDTO;
    }
    
    /**
     * Delete a Google Meet meeting
     */
    public void deleteMeeting(String eventId) throws Exception {
        // Load credentials
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(serviceAccountKey.getFile()))
                .createScoped(List.of("https://www.googleapis.com/auth/calendar"));
        
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
        
        // Initialize the Calendar service
        Calendar service = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(), 
                requestInitializer)
                .setApplicationName("MediConnect")
                .build();
        
        // Delete the event
        service.events().delete("primary", eventId).execute();
    }
}