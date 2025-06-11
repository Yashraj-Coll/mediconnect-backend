package com.mediconnect.controller;

import com.mediconnect.dto.AppointmentDTO;
import com.mediconnect.dto.AppointmentDetailsDTO;
import com.mediconnect.dto.PatientDTO;
import com.mediconnect.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/patient/{patientId}")
    public List<AppointmentDTO> getAppointmentsByPatient(@PathVariable Long patientId) {
        return appointmentService.getAppointmentsByPatientId(patientId);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<AppointmentDTO> getAppointmentsByDoctor(@PathVariable Long doctorId) {
        return appointmentService.getAppointmentsByDoctorId(doctorId);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id) {
        try {
            AppointmentDTO appointment = appointmentService.getAppointmentById(id);
            if (appointment != null) {
                return ResponseEntity.ok(Map.of("success", true, "data", appointment));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Appointment not found"));
            }
        } catch (Exception e) {
            log.error("Error fetching appointment {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to fetch appointment", "error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<?> getAppointmentDetails(@PathVariable Long id) {
        try {
            AppointmentDetailsDTO details = appointmentService.getAppointmentDetailsById(id);
            return ResponseEntity.ok(Map.of("success", true, "data", details));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to fetch appointment details", "error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/video-room")
    public ResponseEntity<?> updateVideoRoomName(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String videoRoomName = payload.get("videoRoomName");
            if (videoRoomName == null || videoRoomName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false, 
                    "message", "Video room name is required"
                ));
            }
            
            AppointmentDTO updatedAppointment = appointmentService.updateVideoRoomName(id, videoRoomName);
            return ResponseEntity.ok(Map.of("success", true, "data", updatedAppointment));
        } catch (Exception e) {
            log.error("Error updating video room name for appointment {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false, 
                    "message", "Failed to update video room name", 
                    "error", e.getMessage()
                ));
        }
    }
    

    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentDTO appointmentDTO) {
        try {
            log.info("Creating appointment with DTO: {}", appointmentDTO);
            
            // Validate required fields
            if (appointmentDTO.getDoctorId() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false, 
                    "message", "Doctor ID is required"
                ));
            }
            
            if (appointmentDTO.getPatientId() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false, 
                    "message", "Patient ID is required"
                ));
            }
            
            if (appointmentDTO.getAppointmentDateTime() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false, 
                    "message", "Appointment date and time are required"
                ));
            }
            
            AppointmentDTO createdAppointment = appointmentService.createAppointment(appointmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAppointment);
        } catch (Exception e) {
            log.error("Error creating appointment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false, 
                    "message", "Failed to create appointment", 
                    "error", e.getMessage()
                ));
        }
    }

    @PutMapping("/{id}/status/{status}")
    public AppointmentDTO updateAppointmentStatus(@PathVariable Long id, @PathVariable String status) {
        return appointmentService.updateAppointmentStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public void deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
    }

    @GetMapping("/")
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }
    
    //Add this method to get patients for a doctor
    @GetMapping("/doctor/{doctorId}/patients")
    public ResponseEntity<List<PatientDTO>> getDoctorPatients(@PathVariable Long doctorId) {
        try {
            // Get all appointments for this doctor
            List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDoctorId(doctorId);
            
            // Extract unique patients
            Set<Long> patientIds = new HashSet<>();
            List<PatientDTO> patients = new ArrayList<>();
            
            for (AppointmentDTO appointment : appointments) {
                if (appointment.getPatientId() != null && !patientIds.contains(appointment.getPatientId())) {
                    patientIds.add(appointment.getPatientId());
                    
                    // Create patient DTO with available data
                    PatientDTO patient = new PatientDTO();
                    patient.setId(appointment.getPatientId());
                    // You'll need to fetch full patient details here
                    patients.add(patient);
                }
            }
            
            return ResponseEntity.ok(patients);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    /**
     * Mark appointment as complete
     * PUT /api/appointments/{id}/complete
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> markAppointmentComplete(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        
        try {
            log.info("Marking appointment {} as complete", id);
            
            // Check if appointment exists
            AppointmentDTO appointment = appointmentService.getAppointmentById(id);
            if (appointment == null) {
                log.warn("Appointment with ID {} not found", id);
                return ResponseEntity.notFound().build();
            }
            
            // Check if appointment is already completed
            if ("completed".equalsIgnoreCase(appointment.getStatus())) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Appointment is already marked as completed"
                    ));
            }
            
            // Update appointment status to completed
            AppointmentDTO updatedAppointment = appointmentService.updateAppointmentStatus(id, "completed");
            
            log.info("Successfully marked appointment {} as complete", id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Appointment marked as completed successfully",
                "data", updatedAppointment
            ));
            
        } catch (Exception e) {
            log.error("Error marking appointment {} as complete: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Failed to mark appointment as complete",
                    "error", e.getMessage()
                ));
        }
    }
    
}