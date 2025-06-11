package com.mediconnect.service;

import com.mediconnect.dto.AppointmentDTO;
import com.mediconnect.dto.AppointmentDetailsDTO;
import com.mediconnect.model.Appointment;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.Patient;
import com.mediconnect.repository.AppointmentRepository;
import com.mediconnect.repository.DoctorRepository;
import com.mediconnect.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AppointmentNotificationService appointmentNotificationService;

    public List<AppointmentDTO> getAppointmentsByPatientId(Long patientId) {
        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        return appointments.stream().map(AppointmentDTO::new).collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAppointmentsByDoctorId(Long doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        return appointments.stream().map(AppointmentDTO::new).collect(Collectors.toList());
    }

    public AppointmentDTO getAppointmentById(Long id) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        return appointmentOpt.map(AppointmentDTO::new).orElse(null);
    }

    public AppointmentDetailsDTO getAppointmentDetailsById(Long id) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isPresent()) {
            return new AppointmentDetailsDTO(appointmentOpt.get());
        } else {
            throw new RuntimeException("Appointment not found with id: " + id);
        }
    }

    public AppointmentDTO createAppointment(AppointmentDTO dto) {
        Appointment appointment = new Appointment();

        // Fetch and set Doctor
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
            .orElseThrow(() -> new RuntimeException("Doctor not found"));
        appointment.setDoctor(doctor);

        // Fetch and set Patient
        Patient patient = patientRepository.findById(dto.getPatientId())
            .orElseThrow(() -> new RuntimeException("Patient not found"));
        appointment.setPatient(patient);

        // Parse OffsetDateTime from ISO string & convert to IST
        try {
            OffsetDateTime utcDateTime = OffsetDateTime.parse(dto.getAppointmentDateTime());
            OffsetDateTime istDateTime = utcDateTime.atZoneSameInstant(ZoneId.of("Asia/Kolkata")).toOffsetDateTime();
            appointment.setAppointmentDateTime(istDateTime);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid date time format: " + dto.getAppointmentDateTime(), e);
        }

        appointment.setAppointmentType(Appointment.AppointmentType.valueOf(dto.getAppointmentType().toLowerCase()));
        appointment.setDurationMinutes(dto.getDurationMinutes());
        appointment.setStatus(Appointment.AppointmentStatus.upcoming);
        appointment.setFee(dto.getFee());
        appointment.setIsPaid(false);
        appointment.setPatientNotes(dto.getPatientNotes());
        appointment.setDoctorNotes(dto.getDoctorNotes());
		
		if (appointment.getAppointmentType() == Appointment.AppointmentType.video) {
        String roomName = "mediconnect-doctor-" + doctor.getId() + 
                         "-patient-" + patient.getId() + "-" + System.currentTimeMillis();
        appointment.setVideoRoomName(roomName);
        log.info("Generated video room name for new appointment: {}", roomName);
    }

        Appointment saved = appointmentRepository.save(appointment);
		
		 // Update room name with actual appointment ID
    if (saved.getAppointmentType() == Appointment.AppointmentType.video) {
        String finalRoomName = "mediconnect-doctor-" + doctor.getId() + 
                              "-patient-" + patient.getId() + "-" + saved.getId();
        saved.setVideoRoomName(finalRoomName);
        saved = appointmentRepository.save(saved);
        log.info("Updated video room name with appointment ID: {}", finalRoomName);
    }

 // Only send notification if appointment is paid (direct booking)
 // For payment flow, notifications are handled by AppointmentNotificationService
 if (Boolean.TRUE.equals(saved.getIsPaid())) {
     try {
         appointmentNotificationService.sendAppointmentConfirmation(saved);
         notificationService.sendAppointmentAlertToDoctor(saved);
         log.info("Appointment confirmation notifications sent for paid appointment: {}", saved.getId());
     } catch (Exception e) {
         log.error("Failed to send appointment notifications for appointment: {} - Error: {}", saved.getId(), e.getMessage());
     }
 } else {
     log.info("Appointment created but not paid yet - notifications will be sent after payment confirmation");
 }

        return new AppointmentDTO(saved);
    }

    public AppointmentDTO updateAppointmentStatus(Long id, String status) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            
            // Store previous status for notification logic
            Appointment.AppointmentStatus previousStatus = appointment.getStatus();
            
            // String to Enum conversion
            if (status != null) {
                try {
                    Appointment.AppointmentStatus newStatus = Appointment.AppointmentStatus.valueOf(status.toLowerCase());
                    appointment.setStatus(newStatus);
                    
                    // TRIGGER NOTIFICATIONS BASED ON STATUS CHANGE
                    if (newStatus == Appointment.AppointmentStatus.cancelled && 
                        previousStatus != Appointment.AppointmentStatus.cancelled) {
                        try {
                            notificationService.sendAppointmentCancellationToPatient(appointment);
                            notificationService.sendAppointmentCancellationToDoctor(appointment);
                            log.info("Cancellation notifications sent for appointment: {}", appointment.getId());
                        } catch (Exception e) {
                            log.error("Failed to send cancellation notifications for appointment: {} - Error: {}", 
                                    appointment.getId(), e.getMessage());
                        }
                    }
                    
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid appointment status: {}", status);
                    // Invalid status, ignore or handle
                }
            }
            
            Appointment updated = appointmentRepository.save(appointment);
            return new AppointmentDTO(updated);
        }
        return null;
    }

    public Appointment updateAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public Appointment getAppointmentEntityById(Long id) {
        return appointmentRepository.findById(id).orElse(null);
    }

    public void deleteAppointment(Long id) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            
            // TRIGGER CANCELLATION NOTIFICATIONS BEFORE DELETION
            try {
                notificationService.sendAppointmentCancellationToPatient(appointment);
                notificationService.sendAppointmentCancellationToDoctor(appointment);
                log.info("Deletion notifications sent for appointment: {}", appointment.getId());
            } catch (Exception e) {
                log.error("Failed to send deletion notifications for appointment: {} - Error: {}", 
                        appointment.getId(), e.getMessage());
            }
        }
        
        appointmentRepository.deleteById(id);
    }

    public List<AppointmentDTO> getAllAppointments() {
        List<Appointment> appointments = appointmentRepository.findAll();
        return appointments.stream().map(AppointmentDTO::new).collect(Collectors.toList());
    }

    public void sendAppointmentReminders(int hoursBeforeAppointment) {
        try {
            // This would typically be called by a scheduled task
            // Find appointments that are X hours away
            OffsetDateTime futureTime = OffsetDateTime.now().plusHours(hoursBeforeAppointment);
            List<Appointment> upcomingAppointments = appointmentRepository.findUpcomingAppointmentsInNextHours(futureTime);
            
            for (Appointment appointment : upcomingAppointments) {
                try {
                    notificationService.sendAppointmentReminderToPatient(appointment, hoursBeforeAppointment);
                    notificationService.sendAppointmentReminderToDoctor(appointment, hoursBeforeAppointment);
                    log.info("Reminder notifications sent for appointment: {} ({} hours before)", 
                            appointment.getId(), hoursBeforeAppointment);
                } catch (Exception e) {
                    log.error("Failed to send reminder notifications for appointment: {} - Error: {}", 
                            appointment.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error in sendAppointmentReminders process: {}", e.getMessage());
        }
    }

    public void sendPaymentReminders() {
        try {
            // Find appointments that are unpaid and scheduled within next 24 hours
            OffsetDateTime tomorrow = OffsetDateTime.now().plusDays(1);
            List<Appointment> unpaidAppointments = appointmentRepository.findUnpaidUpcomingAppointments(tomorrow);
            
            for (Appointment appointment : unpaidAppointments) {
                try {
                    notificationService.sendPaymentReminderToPatient(appointment);
                    log.info("Payment reminder sent for appointment: {}", appointment.getId());
                } catch (Exception e) {
                    log.error("Failed to send payment reminder for appointment: {} - Error: {}", 
                            appointment.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error in sendPaymentReminders process: {}", e.getMessage());
        }
    }

    public void markAppointmentAsPaid(Long appointmentId) {
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (appointmentOpt.isPresent()) {
                Appointment appointment = appointmentOpt.get();
                appointment.setIsPaid(true);
                appointment.setStatus(Appointment.AppointmentStatus.upcoming);
                appointmentRepository.save(appointment);
                log.info("Appointment {} marked as paid and confirmed", appointmentId);
            }
        } catch (Exception e) {
            log.error("Error marking appointment {} as paid: {}", appointmentId, e.getMessage());
        }
    }

    public List<AppointmentDTO> getAppointmentsByStatus(Appointment.AppointmentStatus status) {
        List<Appointment> appointments = appointmentRepository.findByStatus(status);
        return appointments.stream().map(AppointmentDTO::new).collect(Collectors.toList());
    }

    public List<AppointmentDTO> getTodaysAppointments() {
        List<Appointment> appointments = appointmentRepository.findTodaysAppointments();
        return appointments.stream().map(AppointmentDTO::new).collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAppointmentsByDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        List<Appointment> appointments = appointmentRepository.findByAppointmentDateTimeBetween(startDate, endDate);
        return appointments.stream().map(AppointmentDTO::new).collect(Collectors.toList());
    }
    
    public AppointmentDTO updateVideoRoomName(Long appointmentId, String videoRoomName) {
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (appointmentOpt.isPresent()) {
                Appointment appointment = appointmentOpt.get();
                appointment.setVideoRoomName(videoRoomName);
                Appointment updated = appointmentRepository.save(appointment);
                log.info("Video room name updated for appointment: {} - Room: {}", appointmentId, videoRoomName);
                return new AppointmentDTO(updated);
            } else {
                throw new RuntimeException("Appointment not found with id: " + appointmentId);
            }
        } catch (Exception e) {
            log.error("Error updating video room name for appointment {}: {}", appointmentId, e.getMessage());
            throw new RuntimeException("Failed to update video room name", e);
        }
    }
}