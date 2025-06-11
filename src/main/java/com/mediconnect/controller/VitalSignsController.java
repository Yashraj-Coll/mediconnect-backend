package com.mediconnect.controller;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.mediconnect.dto.VitalSignDTO;
import com.mediconnect.model.VitalSign;
import com.mediconnect.service.VitalSignService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vital-signs")
public class VitalSignsController {

    @Autowired
    private VitalSignService vitalSignService;

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.isPatientWithId(#patientId)")
    public ResponseEntity<List<VitalSign>> getVitalSignsByPatientId(@PathVariable Long patientId) {
        List<VitalSign> vitalSigns = vitalSignService.getVitalSignsByPatientId(patientId);
        return new ResponseEntity<>(vitalSigns, HttpStatus.OK);
    }

    @GetMapping("/patient/{patientId}/type/{vitalType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.isPatientWithId(#patientId)")
    public ResponseEntity<List<VitalSign>> getVitalSignsByPatientIdAndType(
            @PathVariable Long patientId,
            @PathVariable String vitalType) {
        List<VitalSign> vitalSigns = vitalSignService.getVitalSignsByPatientIdAndType(patientId, vitalType);
        return new ResponseEntity<>(vitalSigns, HttpStatus.OK);
    }

    @GetMapping("/patient/{patientId}/range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.isPatientWithId(#patientId)")
    public ResponseEntity<List<VitalSign>> getVitalSignsByPatientIdAndDateRange(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate) {
        List<VitalSign> vitalSigns = vitalSignService.getVitalSignsByPatientIdAndDateRange(patientId, startDate, endDate);
        return new ResponseEntity<>(vitalSigns, HttpStatus.OK);
    }

    @GetMapping("/patient/{patientId}/latest")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.isPatientWithId(#patientId)")
    public ResponseEntity<List<VitalSignDTO>> getLatestVitalSignsByPatientId(
            @PathVariable Long patientId,
            @RequestParam(required = false) Integer limit) {
        List<VitalSign> vitalSigns = vitalSignService.getLatestVitalSignsByPatientId(patientId, limit);
List<VitalSignDTO> vitalSignDTOs = vitalSigns.stream()
    .map(VitalSignDTO::fromEntity)
    .collect(Collectors.toList());
return new ResponseEntity<>(vitalSignDTOs, HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<VitalSign> addVitalSign(@Valid @RequestBody VitalSignDTO vitalSignDTO) {
        VitalSign vitalSign = vitalSignService.addVitalSign(vitalSignDTO);
        return new ResponseEntity<>(vitalSign, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.canAccessVitalSign(#id)")
    public ResponseEntity<VitalSign> updateVitalSign(
            @PathVariable Long id,
            @Valid @RequestBody VitalSignDTO vitalSignDTO) {
        VitalSign updatedVitalSign = vitalSignService.updateVitalSign(id, vitalSignDTO);
        return new ResponseEntity<>(updatedVitalSign, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.canAccessVitalSign(#id)")
    public ResponseEntity<Void> deleteVitalSign(@PathVariable Long id) {
        vitalSignService.deleteVitalSign(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/patient/{patientId}/out-of-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.isPatientWithId(#patientId)")
    public ResponseEntity<List<VitalSign>> getOutOfRangeVitalSigns(@PathVariable Long patientId) {
        List<VitalSign> vitalSigns = vitalSignService.getOutOfRangeVitalSigns(patientId);
        return new ResponseEntity<>(vitalSigns, HttpStatus.OK);
    }

    @GetMapping("/patient/{patientId}/analytics/{vitalType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @securityService.isPatientWithId(#patientId)")
    public ResponseEntity<List<VitalSign>> getVitalSignHistoryForAnalytics(
            @PathVariable Long patientId,
            @PathVariable String vitalType,
            @RequestParam(required = false) String timeFrame) {
        List<VitalSign> vitalSigns = vitalSignService.getVitalSignHistoryForAnalytics(patientId, vitalType, timeFrame);
        return new ResponseEntity<>(vitalSigns, HttpStatus.OK);
    }
}
