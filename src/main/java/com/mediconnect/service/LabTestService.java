package com.mediconnect.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediconnect.dto.LabTestBookingDTO;
import com.mediconnect.model.LabTestBooking;
import com.mediconnect.model.Patient;
import com.mediconnect.repository.LabTestBookingRepository;
import com.mediconnect.repository.PatientRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LabTestService {

    private static final Logger log = LoggerFactory.getLogger(LabTestService.class);

    @Autowired
    private LabTestBookingRepository labTestBookingRepository;

    @Autowired
    private PatientRepository patientRepository;

    // Make this optional since it might not exist yet
    // @Autowired(required = false)
    // private LabTestNotificationService labTestNotificationService;

    /**
     * Create a new lab test booking
     */
    @Transactional
    public LabTestBooking createLabTestBooking(LabTestBookingDTO bookingDTO) {
        try {
            log.info("Creating lab test booking for patient ID: {}", bookingDTO.getPatientId());
            
            // Find patient
            Patient patient = patientRepository.findById(bookingDTO.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found with id: " + bookingDTO.getPatientId()));

            // Create lab test booking
            LabTestBooking booking = new LabTestBooking();
            booking.setPatient(patient);
            booking.setTestName(bookingDTO.getTestName());
            booking.setTestDescription(bookingDTO.getTestDescription());
            booking.setTestPrice(bookingDTO.getTestPrice());
            booking.setRegistrationFee(bookingDTO.getRegistrationFee());
            booking.setTaxAmount(bookingDTO.getTaxAmount());
            booking.setTotalAmount(bookingDTO.getTotalAmount());
            booking.setSampleType(bookingDTO.getSampleType());
            booking.setProcessingTime(bookingDTO.getProcessingTime());
            booking.setHomeCollection(bookingDTO.getHomeCollection());
            booking.setScheduledDate(bookingDTO.getScheduledDate());
            
            // Set patient contact information
            booking.setPatientAddress(bookingDTO.getPatientAddress() != null ? 
                bookingDTO.getPatientAddress() : bookingDTO.getAddress());
            booking.setPatientPhone(bookingDTO.getPatientPhone());
            booking.setPatientEmail(bookingDTO.getPatientEmail());
            
            // Set notes
            booking.setNotes(bookingDTO.getNotes() != null ? 
                bookingDTO.getNotes() : bookingDTO.getSpecialInstructions());
            
            // Set default status
            booking.setStatus(LabTestBooking.BookingStatus.PENDING);
            booking.setIsPaid(false);

            // Save booking
            LabTestBooking savedBooking = labTestBookingRepository.save(booking);
            
            log.info("Lab test booking created successfully with ID: {}", savedBooking.getId());
            return savedBooking;

        } catch (Exception e) {
            log.error("Error creating lab test booking: ", e);
            throw new RuntimeException("Failed to create lab test booking: " + e.getMessage());
        }
    }

    /**
     * Get lab test booking by ID
     */
    public LabTestBooking getLabTestBookingById(Long bookingId) {
        return labTestBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Lab test booking not found with id: " + bookingId));
    }

    /**
     * Get all lab test bookings for a patient
     */
    public List<LabTestBooking> getPatientLabTestBookings(Long patientId) {
        return labTestBookingRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    /**
     * Get all lab test bookings (admin)
     */
    public List<LabTestBooking> getAllLabTestBookings() {
        return labTestBookingRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Update lab test booking status
     */
    @Transactional
    public LabTestBooking updateLabTestBookingStatus(Long bookingId, String status) {
        LabTestBooking booking = getLabTestBookingById(bookingId);
        
        try {
            LabTestBooking.BookingStatus newStatus = LabTestBooking.BookingStatus.valueOf(status.toUpperCase());
            booking.setStatus(newStatus);
            
            LabTestBooking updatedBooking = labTestBookingRepository.save(booking);
            
            // Send notification if status changed to important states
            if (newStatus == LabTestBooking.BookingStatus.CONFIRMED || 
                newStatus == LabTestBooking.BookingStatus.SAMPLE_COLLECTED ||
                newStatus == LabTestBooking.BookingStatus.COMPLETED) {
                try {
                    // Notification service call - commented out until service is implemented
                    // if (labTestNotificationService != null) {
                    //     labTestNotificationService.sendStatusUpdateNotification(updatedBooking);
                    // }
                    log.info("Status update notification would be sent for booking: {}", bookingId);
                } catch (Exception e) {
                    log.error("Failed to send status update notification for booking: {} - Error: {}", 
                            bookingId, e.getMessage());
                }
            }
            
            log.info("Lab test booking status updated: {} - {}", bookingId, status);
            return updatedBooking;
            
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }
    }

    /**
     * Cancel lab test booking
     */
    @Transactional
    public LabTestBooking cancelLabTestBooking(Long bookingId, String reason) {
        LabTestBooking booking = getLabTestBookingById(bookingId);
        
        booking.setStatus(LabTestBooking.BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        
        LabTestBooking cancelledBooking = labTestBookingRepository.save(booking);
        
        // Send cancellation notification
        try {
            // Notification service call - commented out until service is implemented
            // if (labTestNotificationService != null) {
            //     labTestNotificationService.sendCancellationNotification(cancelledBooking);
            // }
            log.info("Cancellation notification would be sent for booking: {}", bookingId);
        } catch (Exception e) {
            log.error("Failed to send cancellation notification for booking: {} - Error: {}", 
                    bookingId, e.getMessage());
        }
        
        log.info("Lab test booking cancelled: {} - Reason: {}", bookingId, reason);
        return cancelledBooking;
    }

    /**
     * Update payment status
     */
    @Transactional
    public LabTestBooking updatePaymentStatus(Long bookingId, Boolean isPaid) {
        LabTestBooking booking = getLabTestBookingById(bookingId);
        booking.setIsPaid(isPaid);
        
        if (isPaid) {
            // Auto-confirm booking when payment is successful
            booking.setStatus(LabTestBooking.BookingStatus.CONFIRMED);
        }
        
        LabTestBooking updatedBooking = labTestBookingRepository.save(booking);
        
        log.info("Lab test booking payment status updated: {} - Paid: {}", bookingId, isPaid);
        return updatedBooking;
    }

    /**
     * Get lab test bookings by status
     */
    public List<LabTestBooking> getLabTestBookingsByStatus(LabTestBooking.BookingStatus status) {
        return labTestBookingRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * Get pending lab test bookings
     */
    public List<LabTestBooking> getPendingLabTestBookings() {
        return getLabTestBookingsByStatus(LabTestBooking.BookingStatus.PENDING);
    }

    /**
     * Get confirmed lab test bookings
     */
    public List<LabTestBooking> getConfirmedLabTestBookings() {
        return getLabTestBookingsByStatus(LabTestBooking.BookingStatus.CONFIRMED);
    }
}