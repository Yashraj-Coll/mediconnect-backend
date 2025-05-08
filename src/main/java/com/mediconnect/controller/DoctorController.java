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

import com.mediconnect.dto.DoctorDTO;
import com.mediconnect.dto.DoctorLanguageDTO;
import com.mediconnect.model.Doctor;
import com.mediconnect.service.DoctorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    /**
     * Get all doctors
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    /**
     * Get doctor by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        Doctor doctor = doctorService.getDoctorById(id);
        return new ResponseEntity<>(doctor, HttpStatus.OK);
    }

    /**
     * Get doctor by user ID
     */
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<Doctor> getDoctorByUserId(@PathVariable Long userId) {
        return doctorService.getDoctorByUserId(userId)
                .map(doctor -> new ResponseEntity<>(doctor, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Get doctors by specialization
     */
    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<List<Doctor>> getDoctorsBySpecialization(@PathVariable String specialization) {
        List<Doctor> doctors = doctorService.getDoctorsBySpecialization(specialization);
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    /**
     * Get doctors available for emergency
     */
    @GetMapping("/emergency")
    public ResponseEntity<List<Doctor>> getAvailableDoctorsForEmergency() {
        List<Doctor> doctors = doctorService.getAvailableDoctorsForEmergency();
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    /**
     * Search doctors by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<List<Doctor>> searchDoctors(@RequestParam String keyword) {
        List<Doctor> doctors = doctorService.searchDoctors(keyword);
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    /**
     * Get doctors by max consultation fee
     */
    @GetMapping("/fee")
    public ResponseEntity<List<Doctor>> getDoctorsByMaxFee(@RequestParam Double maxFee) {
        List<Doctor> doctors = doctorService.getDoctorsByMaxConsultationFee(maxFee);
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }

    /**
     * Get top rated doctors
     */
    @GetMapping("/top-rated")
    public ResponseEntity<List<Doctor>> getTopRatedDoctors() {
        List<Doctor> doctors = doctorService.getTopRatedDoctors();
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }
    
    /**
     * Get languages for a doctor
     */
    @GetMapping("/{id}/languages")
    public ResponseEntity<List<DoctorLanguageDTO>> getDoctorLanguages(@PathVariable Long id) {
        List<DoctorLanguageDTO> languages = doctorService.getDoctorLanguages(id);
        return new ResponseEntity<>(languages, HttpStatus.OK);
    }
    
    /**
     * Add language to a doctor
     */
    @PostMapping("/{id}/languages")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<DoctorLanguageDTO> addDoctorLanguage(@PathVariable Long id, @RequestBody DoctorLanguageDTO languageDTO) {
        DoctorLanguageDTO addedLanguage = doctorService.addDoctorLanguage(id, languageDTO.getLanguage());
        return new ResponseEntity<>(addedLanguage, HttpStatus.CREATED);
    }
    
    /**
     * Remove language from a doctor
     */
    @DeleteMapping("/{id}/languages/{language}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<Void> removeDoctorLanguage(@PathVariable Long id, @PathVariable String language) {
        doctorService.removeDoctorLanguage(id, language);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Create a new doctor
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Doctor> createDoctor(@Valid @RequestBody DoctorDTO doctorDTO) {
        Doctor newDoctor = doctorService.createDoctor(doctorDTO);
        return new ResponseEntity<>(newDoctor, HttpStatus.CREATED);
    }

    /**
     * Update an existing doctor
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable Long id, @Valid @RequestBody DoctorDTO doctorDTO) {
        Doctor updatedDoctor = doctorService.updateDoctor(id, doctorDTO);
        return new ResponseEntity<>(updatedDoctor, HttpStatus.OK);
    }

    /**
     * Delete a doctor
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * Public endpoints for doctor listings - No authentication required
     */
    @GetMapping("/public/all")
    public ResponseEntity<List<Doctor>> getPublicDoctorsList() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }
    
    @GetMapping("/public/specialization/{specialization}")
    public ResponseEntity<List<Doctor>> getPublicDoctorsBySpecialization(@PathVariable String specialization) {
        List<Doctor> doctors = doctorService.getDoctorsBySpecialization(specialization);
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }
    
    @GetMapping("/public/top-rated")
    public ResponseEntity<List<Doctor>> getPublicTopRatedDoctors() {
        List<Doctor> doctors = doctorService.getTopRatedDoctors();
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }
    
    @GetMapping("/public/search")
    public ResponseEntity<List<Doctor>> searchPublicDoctors(@RequestParam String keyword) {
        List<Doctor> doctors = doctorService.searchDoctors(keyword);
        return new ResponseEntity<>(doctors, HttpStatus.OK);
    }
    
    @GetMapping("/public/{id}")
    public ResponseEntity<Doctor> getPublicDoctorById(@PathVariable Long id) {
        Doctor doctor = doctorService.getDoctorById(id);
        return new ResponseEntity<>(doctor, HttpStatus.OK);
    }
    
    /**
     * Public endpoint for doctor languages - No authentication required
     */
    @GetMapping("/public/{id}/languages")
    public ResponseEntity<List<DoctorLanguageDTO>> getPublicDoctorLanguages(@PathVariable Long id) {
        List<DoctorLanguageDTO> languages = doctorService.getDoctorLanguages(id);
        return new ResponseEntity<>(languages, HttpStatus.OK);
    }
}