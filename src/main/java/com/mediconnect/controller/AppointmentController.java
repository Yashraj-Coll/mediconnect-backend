package com.mediconnect.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mediconnect.dto.AppointmentDTO;
import com.mediconnect.model.Appointment;
import com.mediconnect.model.Appointment.AppointmentStatus;
import com.mediconnect.model.Appointment.AppointmentType;
import com.mediconnect.service.AppointmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * Get all appointments
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    /**
     * Get appointment by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.canAccessAppointment(#id)")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        return new ResponseEntity<>(appointment, HttpStatus.OK);
    }

    /**
     * Get appointments for a doctor
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isDoctorWithId(#doctorId)")
    public ResponseEntity<List<Appointment>> getAppointmentsByDoctorId(@PathVariable Long doctorId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByDoctorId(doctorId);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    /**
     * Get appointments for a patient
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isPatientWithId(#patientId)")
    public ResponseEntity<List<Appointment>> getAppointmentsByPatientId(@PathVariable Long patientId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByPatientId(patientId);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    /**
     * Get appointments by doctor and status
     */
    @GetMapping("/doctor/{doctorId}/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isDoctorWithId(#doctorId)")
    public ResponseEntity<List<Appointment>> getAppointmentsByDoctorAndStatus(
            @PathVariable Long doctorId, 
            @PathVariable AppointmentStatus status) {
        List<Appointment> appointments = appointmentService.getAppointmentsByDoctorAndStatus(doctorId, status);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    /**
     * Get appointments by patient and status
     */
    @GetMapping("/patient/{patientId}/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isPatientWithId(#patientId)")
    public ResponseEntity<List<Appointment>> getAppointmentsByPatientAndStatus(
            @PathVariable Long patientId, 
            @PathVariable AppointmentStatus status) {
        List<Appointment> appointments = appointmentService.getAppointmentsByPatientAndStatus(patientId, status);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    /**
     * Get doctor's appointments by date range
     */
    @GetMapping("/doctor/{doctorId}/range")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isDoctorWithId(#doctorId)")
    public ResponseEntity<List<Appointment>> getDoctorAppointmentsByDateRange(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Appointment> appointments = appointmentService.getDoctorAppointmentsByDateRange(doctorId, startDate, endDate);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    /**
     * Get patient's appointments by date range
     */
    @GetMapping("/patient/{patientId}/range")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isPatientWithId(#patientId)")
    public ResponseEntity<List<Appointment>> getPatientAppointmentsByDateRange(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Appointment> appointments = appointmentService.getPatientAppointmentsByDateRange(patientId, startDate, endDate);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    /**
     * Get appointments by type
     */
    @GetMapping("/type/{appointmentType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<Appointment>> getAppointmentsByType(@PathVariable AppointmentType appointmentType) {
        List<Appointment> appointments = appointmentService.getAppointmentsByType(appointmentType);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    /**
     * Get upcoming appointments by status
     */
    @GetMapping("/upcoming/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<Appointment>> getUpcomingAppointmentsByStatus(@PathVariable AppointmentStatus status) {
        List<Appointment> appointments = appointmentService.getUpcomingAppointmentsByStatus(status);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    /**
     * Get unpaid appointments
     */
    @GetMapping("/unpaid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Appointment>> getUnpaidAppointments() {
        List<Appointment> appointments = appointmentService.getUnpaidAppointments();
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    /**
     * Create a new appointment
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PATIENT')")
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody AppointmentDTO appointmentDTO) {
        Appointment newAppointment = appointmentService.createAppointment(appointmentDTO);
        return new ResponseEntity<>(newAppointment, HttpStatus.CREATED);
    }

    /**
     * Update an existing appointment
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.canAccessAppointment(#id)")
    public ResponseEntity<Appointment> updateAppointment(
            @PathVariable Long id, 
            @Valid @RequestBody AppointmentDTO appointmentDTO) {
        Appointment updatedAppointment = appointmentService.updateAppointment(id, appointmentDTO);
        return new ResponseEntity<>(updatedAppointment, HttpStatus.OK);
    }

    /**
     * Update appointment status
     */
    @PutMapping("/{id}/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.canAccessAppointment(#id)")
    public ResponseEntity<Appointment> updateAppointmentStatus(
            @PathVariable Long id, 
            @PathVariable AppointmentStatus status) {
        Appointment updatedAppointment = appointmentService.updateAppointmentStatus(id, status);
        return new ResponseEntity<>(updatedAppointment, HttpStatus.OK);
    }

    /**
     * Delete an appointment
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.canAccessAppointment(#id)")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}