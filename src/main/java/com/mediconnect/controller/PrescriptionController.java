package com.mediconnect.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.mediconnect.dto.PrescriptionDTO;
import com.mediconnect.dto.PrescriptionItemDTO;
import com.mediconnect.model.Prescription;
import com.mediconnect.model.PrescriptionItem;
import com.mediconnect.service.PrescriptionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    // ========== GET endpoints (DTO returns, no recursion error) ==========

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PrescriptionDTO>> getAllPrescriptions() {
        List<PrescriptionDTO> prescriptions = prescriptionService.getAllPrescriptionDTOs();
        return new ResponseEntity<>(prescriptions, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.canAccessPrescription(#id)")
    public ResponseEntity<PrescriptionDTO> getPrescriptionById(@PathVariable Long id) {
        PrescriptionDTO prescription = prescriptionService.getPrescriptionDTOById(id);
        return new ResponseEntity<>(prescription, HttpStatus.OK);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.isPatientWithId(#patientId)")
    public ResponseEntity<List<PrescriptionDTO>> getPrescriptionsByPatientId(@PathVariable Long patientId) {
        List<PrescriptionDTO> prescriptions = prescriptionService.getPrescriptionDTOsByPatientId(patientId);
        return new ResponseEntity<>(prescriptions, HttpStatus.OK);
    }

    @GetMapping("/patient/{patientId}/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.isPatientWithId(#patientId)")
    public ResponseEntity<List<PrescriptionDTO>> getActivePrescriptionsForPatient(@PathVariable Long patientId) {
        List<PrescriptionDTO> prescriptions = prescriptionService.getActivePrescriptionDTOsForPatient(patientId);
        return new ResponseEntity<>(prescriptions, HttpStatus.OK);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isDoctorWithId(#doctorId)")
    public ResponseEntity<List<PrescriptionDTO>> getPrescriptionsByDoctorId(@PathVariable Long doctorId) {
        List<PrescriptionDTO> prescriptions = prescriptionService.getPrescriptionDTOsByDoctorId(doctorId);
        return new ResponseEntity<>(prescriptions, HttpStatus.OK);
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.canAccessAppointment(#appointmentId)")
    public ResponseEntity<PrescriptionDTO> getPrescriptionByAppointmentId(@PathVariable Long appointmentId) {
        PrescriptionDTO prescription = prescriptionService.getPrescriptionDTOByAppointmentId(appointmentId);
        return new ResponseEntity<>(prescription, HttpStatus.OK);
    }

    // ========== POST/PUT/DELETE endpoints (entity returns) ==========

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Prescription> createPrescription(@Valid @RequestBody PrescriptionDTO prescriptionDTO) {
        Prescription newPrescription = prescriptionService.createPrescription(prescriptionDTO);
        return new ResponseEntity<>(newPrescription, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Prescription> updatePrescription(@PathVariable Long id, @Valid @RequestBody PrescriptionDTO prescriptionDTO) {
        Prescription updatedPrescription = prescriptionService.updatePrescription(id, prescriptionDTO);
        return new ResponseEntity<>(updatedPrescription, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePrescription(@PathVariable Long id) {
        prescriptionService.deletePrescription(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{prescriptionId}/items")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<PrescriptionItem> addPrescriptionItem(
            @PathVariable Long prescriptionId,
            @Valid @RequestBody PrescriptionItemDTO itemDTO) {
        PrescriptionItem newItem = prescriptionService.addPrescriptionItem(prescriptionId, itemDTO);
        return new ResponseEntity<>(newItem, HttpStatus.CREATED);
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<PrescriptionItem> updatePrescriptionItem(
            @PathVariable Long itemId,
            @Valid @RequestBody PrescriptionItemDTO itemDTO) {
        PrescriptionItem updatedItem = prescriptionService.updatePrescriptionItem(itemId, itemDTO);
        return new ResponseEntity<>(updatedItem, HttpStatus.OK);
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> deletePrescriptionItem(@PathVariable Long itemId) {
        prescriptionService.deletePrescriptionItem(itemId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{prescriptionId}/items")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.canAccessPrescription(#prescriptionId)")
    public ResponseEntity<List<PrescriptionItem>> getPrescriptionItems(@PathVariable Long prescriptionId) {
        List<PrescriptionItem> items = prescriptionService.getPrescriptionItems(prescriptionId);
        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    @PostMapping("/{prescriptionId}/refill")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Prescription> processPrescriptionRefill(@PathVariable Long prescriptionId) {
        Prescription refilled = prescriptionService.processPrescriptionRefill(prescriptionId);
        return new ResponseEntity<>(refilled, HttpStatus.OK);
    }

    @PutMapping("/{prescriptionId}/sign")
    @PreAuthorize("hasRole('DOCTOR') and @securityService.isDoctorForPrescription(#prescriptionId)")
    public ResponseEntity<Prescription> markPrescriptionAsSigned(@PathVariable Long prescriptionId) {
        Prescription signed = prescriptionService.signPrescription(prescriptionId);
        return new ResponseEntity<>(signed, HttpStatus.OK);
    }

    @PutMapping("/{prescriptionId}/extend")
    @PreAuthorize("hasRole('DOCTOR') and @securityService.isDoctorForPrescription(#prescriptionId)")
    public ResponseEntity<Prescription> extendPrescriptionValidity(
            @PathVariable Long prescriptionId,
            @RequestBody int daysToExtend) {
        Prescription extended = prescriptionService.extendPrescriptionValidity(prescriptionId, daysToExtend);
        return new ResponseEntity<>(extended, HttpStatus.OK);
    }

    @GetMapping("/{prescriptionId}/remaining-days")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.canAccessPrescription(#prescriptionId)")
    public ResponseEntity<Integer> getPrescriptionRemainingDays(@PathVariable Long prescriptionId) {
        int remainingDays = prescriptionService.calculateRemainingDays(prescriptionId);
        return new ResponseEntity<>(remainingDays, HttpStatus.OK);
    }

    @PostMapping("/{prescriptionId}/reminder")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.canAccessPrescription(#prescriptionId)")
    public ResponseEntity<Boolean> setPrescriptionReminder(@PathVariable Long prescriptionId) {
        boolean success = prescriptionService.setPrescriptionReminder(prescriptionId);
        return new ResponseEntity<>(success, HttpStatus.OK);
    }

    @DeleteMapping("/{prescriptionId}/reminder")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.canAccessPrescription(#prescriptionId)")
    public ResponseEntity<Boolean> cancelPrescriptionReminder(@PathVariable Long prescriptionId) {
        boolean success = prescriptionService.cancelPrescriptionReminder(prescriptionId);
        return new ResponseEntity<>(success, HttpStatus.OK);
    }
}
