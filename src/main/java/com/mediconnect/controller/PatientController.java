package com.mediconnect.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.mediconnect.dto.PatientDTO;
import com.mediconnect.model.Patient;
import com.mediconnect.service.PatientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    /**
     * Get all patients
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<Patient>> getAllPatients() {
        List<Patient> patients = patientService.getAllPatients();
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    /**
     * Get patient by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.isOwner(#id)")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        Patient patient = patientService.getPatientById(id);
        return new ResponseEntity<>(patient, HttpStatus.OK);
    }

    /**
     * Get patient by user ID
     */
    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<Patient> getPatientByUserId(@PathVariable Long userId) {
        return patientService.getPatientByUserId(userId)
                .map(patient -> new ResponseEntity<>(patient, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Search patients by keyword
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<Patient>> searchPatients(@RequestParam String keyword) {
        List<Patient> patients = patientService.searchPatients(keyword);
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    /**
     * Get patients by medical condition
     */
    @GetMapping("/condition")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<Patient>> getPatientsByMedicalCondition(@RequestParam String condition) {
        List<Patient> patients = patientService.getPatientsByMedicalCondition(condition);
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    /**
     * Get patients by doctor
     */
    @GetMapping("/by-doctor/{doctorId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isDoctorWithId(#doctorId)")
    public ResponseEntity<List<Patient>> getPatientsByDoctor(@PathVariable Long doctorId) {
        List<Patient> patients = patientService.getPatientsByDoctor(doctorId);
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    /**
     * Get patients by insurance provider
     */
    @GetMapping("/insurance/{provider}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<Patient>> getPatientsByInsuranceProvider(@PathVariable String provider) {
        List<Patient> patients = patientService.getPatientsByInsuranceProvider(provider);
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    /**
     * Create a new patient
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Patient> createPatient(@Valid @RequestBody PatientDTO patientDTO) {
        Patient newPatient = patientService.createPatient(patientDTO);
        return new ResponseEntity<>(newPatient, HttpStatus.CREATED);
    }

    /**
     * Update an existing patient
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#id)")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientDTO patientDTO) {
        Patient updatedPatient = patientService.updatePatient(id, patientDTO);
        return new ResponseEntity<>(updatedPatient, HttpStatus.OK);
    }

    /**
     * Delete a patient
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}