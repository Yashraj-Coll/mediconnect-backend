package com.mediconnect.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mediconnect.dto.AppointmentSearchDTO;
import com.mediconnect.dto.DoctorSearchDTO;
import com.mediconnect.dto.PatientSearchDTO;
import com.mediconnect.model.Appointment;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.Patient;
import com.mediconnect.service.AdvancedSearchService;

@RestController
@RequestMapping("/api/search")
public class AdvancedSearchController {

    @Autowired
    private AdvancedSearchService searchService;
    
    /**
     * Advanced patient search
     */
    @PostMapping("/patients")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<Page<Patient>> searchPatients(@RequestBody PatientSearchDTO searchDTO) {
        Page<Patient> results = searchService.searchPatients(searchDTO);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
    
    /**
     * Advanced doctor search
     */
    @PostMapping("/doctors")
    public ResponseEntity<Page<Doctor>> searchDoctors(@RequestBody DoctorSearchDTO searchDTO) {
        Page<Doctor> results = searchService.searchDoctors(searchDTO);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
    
    /**
     * Advanced appointment search
     */
    @PostMapping("/appointments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<Page<Appointment>> searchAppointments(@RequestBody AppointmentSearchDTO searchDTO) {
        Page<Appointment> results = searchService.searchAppointments(searchDTO);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
    
    /**
     * Find patients by vital signs
     */
    @GetMapping("/patients/vitals")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<Patient>> findPatientsByVitalSigns(
            @RequestParam(required = false) Integer minHeartRate,
            @RequestParam(required = false) Integer maxHeartRate,
            @RequestParam(required = false) Integer minSystolicBP,
            @RequestParam(required = false) Integer maxSystolicBP,
            @RequestParam(required = false) Double minTemperature,
            @RequestParam(required = false) Double maxTemperature,
            @RequestParam(required = false) Double minOxygenSaturation,
            @RequestParam(required = false) Double maxOxygenSaturation) {
        
        List<Patient> patients = searchService.findPatientsByVitalSigns(
                minHeartRate, maxHeartRate,
                minSystolicBP, maxSystolicBP,
                minTemperature, maxTemperature,
                minOxygenSaturation, maxOxygenSaturation);
        
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }
    
    /**
     * Find patients by BMI range
     */
    @GetMapping("/patients/bmi")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<Patient>> findPatientsByBMIRange(
            @RequestParam Double minBMI,
            @RequestParam Double maxBMI) {
        
        List<Patient> patients = searchService.findPatientsByBMIRange(minBMI, maxBMI);
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }
    
    /**
     * Find overlapping appointments for a doctor
     */
    @GetMapping("/appointments/overlapping")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<Appointment>> findOverlappingAppointments(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<Appointment> appointments = searchService.findOverlappingAppointments(doctorId, startTime, endTime);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }
}