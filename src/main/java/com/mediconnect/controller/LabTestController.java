package com.mediconnect.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mediconnect.dto.LabTestBookingDTO;
import com.mediconnect.model.LabTestBooking;
import com.mediconnect.service.LabTestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/lab-tests")
public class LabTestController {

	private static final Logger log = LoggerFactory.getLogger(LabTestController.class);
    @Autowired
    private LabTestService labTestService;

    /**
     * Create a new lab test booking
     */
    @PostMapping("/book")
    public ResponseEntity<Map<String, Object>> createLabTestBooking(@RequestBody LabTestBookingDTO labTestBookingDTO) {
        try {
            LabTestBooking booking = labTestService.createLabTestBooking(labTestBookingDTO);
            
            // 🆕 Create a simple response map without circular references
            Map<String, Object> bookingData = new HashMap<>();
            bookingData.put("id", booking.getId());
            bookingData.put("testName", booking.getTestName());
            bookingData.put("testDescription", booking.getTestDescription());
            bookingData.put("testPrice", booking.getTestPrice());
            bookingData.put("registrationFee", booking.getRegistrationFee());
            bookingData.put("taxAmount", booking.getTaxAmount());
            bookingData.put("totalAmount", booking.getTotalAmount());
            bookingData.put("sampleType", booking.getSampleType());
            bookingData.put("processingTime", booking.getProcessingTime());
            bookingData.put("homeCollection", booking.getHomeCollection());
            bookingData.put("status", booking.getStatus().toString());
            bookingData.put("isPaid", booking.getIsPaid());
            bookingData.put("createdAt", booking.getCreatedAt());
            bookingData.put("scheduledDate", booking.getScheduledDate());
            bookingData.put("patientAddress", booking.getPatientAddress());
            bookingData.put("patientPhone", booking.getPatientPhone());
            bookingData.put("patientEmail", booking.getPatientEmail());
            bookingData.put("notes", booking.getNotes());
            
            // Add simple patient info without circular references
            if (booking.getPatient() != null) {
                bookingData.put("patientId", booking.getPatient().getId());
                if (booking.getPatient().getUser() != null) {
                    bookingData.put("patientName", 
                        booking.getPatient().getUser().getFirstName() + " " + 
                        booking.getPatient().getUser().getLastName());
                }
            }
            
            return ResponseEntity.ok(Map.of("success", true, "data", bookingData));
            
        } catch (Exception e) {
            log.error("Error creating lab test booking: ", e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Get lab test booking by ID
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<Map<String, Object>> getLabTestBookingById(@PathVariable Long bookingId) {
        try {
            LabTestBooking booking = labTestService.getLabTestBookingById(bookingId);
            return ResponseEntity.ok(Map.of("success", true, "data", booking));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to fetch lab test booking", "error", e.getMessage()));
        }
    }

    /**
     * Get all lab test bookings for a patient
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Map<String, Object>> getPatientLabTestBookings(@PathVariable Long patientId) {
        try {
            List<LabTestBooking> bookings = labTestService.getPatientLabTestBookings(patientId);
            return ResponseEntity.ok(Map.of("success", true, "data", bookings));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to fetch lab test bookings", "error", e.getMessage()));
        }
    }

    /**
     * Update lab test booking status
     */
    @PutMapping("/booking/{bookingId}/status")
    public ResponseEntity<Map<String, Object>> updateLabTestBookingStatus(
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            LabTestBooking booking = labTestService.updateLabTestBookingStatus(bookingId, status);
            return ResponseEntity.ok(Map.of("success", true, "data", booking));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to update booking status", "error", e.getMessage()));
        }
    }

    /**
     * Cancel lab test booking
     */
    @PutMapping("/booking/{bookingId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelLabTestBooking(
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            LabTestBooking booking = labTestService.cancelLabTestBooking(bookingId, reason);
            return ResponseEntity.ok(Map.of("success", true, "data", booking));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to cancel booking", "error", e.getMessage()));
        }
    }

    /**
     * Get all lab test bookings (admin)
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllLabTestBookings() {
        try {
            List<LabTestBooking> bookings = labTestService.getAllLabTestBookings();
            return ResponseEntity.ok(Map.of("success", true, "data", bookings));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to fetch all bookings", "error", e.getMessage()));
        }
    }

    /**
     * Update lab test booking payment status
     */
    @PutMapping("/booking/{bookingId}/payment")
    public ResponseEntity<Map<String, Object>> updatePaymentStatus(
            @PathVariable Long bookingId,
            @RequestBody Map<String, Boolean> request) {
        try {
            Boolean isPaid = request.get("isPaid");
            LabTestBooking booking = labTestService.updatePaymentStatus(bookingId, isPaid);
            return ResponseEntity.ok(Map.of("success", true, "data", booking));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Failed to update payment status", "error", e.getMessage()));
        }
    }
}