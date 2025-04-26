package com.mediconnect.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediconnect.dto.AppointmentDTO;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.model.Appointment;
import com.mediconnect.model.Appointment.AppointmentStatus;
import com.mediconnect.model.Appointment.AppointmentType;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.Patient;
import com.mediconnect.repository.AppointmentRepository;
import com.mediconnect.repository.DoctorRepository;
import com.mediconnect.repository.PatientRepository;

@Service
public class AppointmentService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
    
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }
    
    public List<Appointment> getAppointmentsByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }
    
    public List<Appointment> getAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }
    
    public List<Appointment> getAppointmentsByDoctorAndStatus(Long doctorId, AppointmentStatus status) {
        return appointmentRepository.findByDoctorIdAndStatus(doctorId, status);
    }
    
    public List<Appointment> getAppointmentsByPatientAndStatus(Long patientId, AppointmentStatus status) {
        return appointmentRepository.findByPatientIdAndStatus(patientId, status);
    }
    
    public List<Appointment> getDoctorAppointmentsByDateRange(Long doctorId, LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findByDoctorIdAndDateRange(doctorId, startDate, endDate);
    }
    
    public List<Appointment> getPatientAppointmentsByDateRange(Long patientId, LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findByPatientIdAndDateRange(patientId, startDate, endDate);
    }
    
    public List<Appointment> getAppointmentsByType(AppointmentType appointmentType) {
        return appointmentRepository.findByAppointmentType(appointmentType);
    }
    
    public List<Appointment> getUpcomingAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findUpcomingAppointmentsByStatus(status, LocalDateTime.now());
    }
    
    public List<Appointment> getUnpaidAppointments() {
        return appointmentRepository.findUnpaidAppointments();
    }
    
    @Transactional
    public Appointment createAppointment(AppointmentDTO appointmentDTO) {
        // Validate doctor exists
        Doctor doctor = doctorRepository.findById(appointmentDTO.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + appointmentDTO.getDoctorId()));
        
        // Validate patient exists
        Patient patient = patientRepository.findById(appointmentDTO.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + appointmentDTO.getPatientId()));
        
        // Check if appointment time is in the future
        if (appointmentDTO.getAppointmentDateTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Appointment time must be in the future");
        }
        
        // Check for doctor availability (simplified version - would need more logic in real app)
        List<Appointment> doctorAppointments = appointmentRepository.findByDoctorIdAndDateRange(
                doctor.getId(),
                appointmentDTO.getAppointmentDateTime().minusMinutes(30),
                appointmentDTO.getAppointmentDateTime().plusMinutes(appointmentDTO.getDurationMinutes() + 30)
        );
        
        if (!doctorAppointments.isEmpty()) {
            throw new BadRequestException("Doctor is not available at the selected time");
        }
        
        // Create new appointment
        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentDateTime(appointmentDTO.getAppointmentDateTime());
        appointment.setDurationMinutes(appointmentDTO.getDurationMinutes());
        appointment.setAppointmentType(appointmentDTO.getAppointmentType());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setPatientNotes(appointmentDTO.getPatientNotes());
        appointment.setFee(appointmentDTO.getFee() != null ? appointmentDTO.getFee() : doctor.getConsultationFee());
        appointment.setIsPaid(false);
        
        // Save appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // Send notifications
        notificationService.sendAppointmentConfirmationToPatient(savedAppointment);
        notificationService.sendAppointmentAlertToDoctor(savedAppointment);
        
        return savedAppointment;
    }
    
    @Transactional
    public Appointment updateAppointment(Long id, AppointmentDTO appointmentDTO) {
        Appointment appointment = getAppointmentById(id);
        
        // Update appointment fields
        if (appointmentDTO.getAppointmentDateTime() != null) {
            // Check if new date is in future
            if (appointmentDTO.getAppointmentDateTime().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Appointment time must be in the future");
            }
            appointment.setAppointmentDateTime(appointmentDTO.getAppointmentDateTime());
        }
        
        if (appointmentDTO.getDurationMinutes() != null) {
            appointment.setDurationMinutes(appointmentDTO.getDurationMinutes());
        }
        
        if (appointmentDTO.getAppointmentType() != null) {
            appointment.setAppointmentType(appointmentDTO.getAppointmentType());
        }
        
        if (appointmentDTO.getStatus() != null) {
            appointment.setStatus(appointmentDTO.getStatus());
            
            // Send notifications based on status change
            if (appointmentDTO.getStatus() == AppointmentStatus.CONFIRMED) {
                notificationService.sendAppointmentConfirmationToPatient(appointment);
            } else if (appointmentDTO.getStatus() == AppointmentStatus.CANCELLED) {
                notificationService.sendAppointmentCancellationToPatient(appointment);
                notificationService.sendAppointmentCancellationToDoctor(appointment);
            }
        }
        
        if (appointmentDTO.getPatientNotes() != null) {
            appointment.setPatientNotes(appointmentDTO.getPatientNotes());
        }
        
        if (appointmentDTO.getDoctorNotes() != null) {
            appointment.setDoctorNotes(appointmentDTO.getDoctorNotes());
        }
        
        if (appointmentDTO.getFee() != null) {
            appointment.setFee(appointmentDTO.getFee());
        }
        
        if (appointmentDTO.getIsPaid() != null) {
            appointment.setIsPaid(appointmentDTO.getIsPaid());
        }
        
        return appointmentRepository.save(appointment);
    }
    
    @Transactional
    public Appointment updateAppointmentStatus(Long id, AppointmentStatus status) {
        Appointment appointment = getAppointmentById(id);
        appointment.setStatus(status);
        
        // Send notifications based on status change
        if (status == AppointmentStatus.CONFIRMED) {
            notificationService.sendAppointmentConfirmationToPatient(appointment);
        } else if (status == AppointmentStatus.CANCELLED) {
            notificationService.sendAppointmentCancellationToPatient(appointment);
            notificationService.sendAppointmentCancellationToDoctor(appointment);
        }
        
        return appointmentRepository.save(appointment);
    }
    
    public void deleteAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        
        // Send cancellation notifications before deleting
        notificationService.sendAppointmentCancellationToPatient(appointment);
        notificationService.sendAppointmentCancellationToDoctor(appointment);
        
        appointmentRepository.delete(appointment);
    }
}