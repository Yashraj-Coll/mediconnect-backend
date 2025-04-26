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

import com.mediconnect.dto.AiDiagnosisRequestDTO;
import com.mediconnect.model.AiDiagnosisResult;
import com.mediconnect.model.AiDiagnosisResult.AiDiagnosisStatus;
import com.mediconnect.model.DiagnosisPrediction;
import com.mediconnect.service.AiDiagnosticService;
import com.mediconnect.service.DiagnosisPredictionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai-diagnosis")
public class AiDiagnosisController {

    @Autowired
    private AiDiagnosticService aiDiagnosticService;
    
    @Autowired
    private DiagnosisPredictionService diagnosisPredictionService;

    /**
     * Get all AI diagnosis results
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<AiDiagnosisResult>> getAllDiagnosisResults() {
        List<AiDiagnosisResult> results = aiDiagnosticService.getAllDiagnosisResults();
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    /**
     * Get AI diagnosis result by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.canAccessAiDiagnosis(#id)")
    public ResponseEntity<AiDiagnosisResult> getDiagnosisResultById(@PathVariable Long id) {
        AiDiagnosisResult result = aiDiagnosticService.getDiagnosisResultById(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Get AI diagnosis result by medical record ID
     */
    @GetMapping("/medical-record/{medicalRecordId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.canAccessMedicalRecord(#medicalRecordId)")
    public ResponseEntity<AiDiagnosisResult> getDiagnosisResultByMedicalRecordId(@PathVariable Long medicalRecordId) {
        return aiDiagnosticService.getDiagnosisResultByMedicalRecordId(medicalRecordId)
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Get AI diagnosis results by patient ID
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isPatientWithId(#patientId) or hasRole('DOCTOR')")
    public ResponseEntity<List<AiDiagnosisResult>> getDiagnosisResultsByPatientId(@PathVariable Long patientId) {
        List<AiDiagnosisResult> results = aiDiagnosticService.getDiagnosisResultsByPatientId(patientId);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    /**
     * Get AI diagnosis results by doctor ID
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isDoctorWithId(#doctorId)")
    public ResponseEntity<List<AiDiagnosisResult>> getDiagnosisResultsByDoctorId(@PathVariable Long doctorId) {
        List<AiDiagnosisResult> results = aiDiagnosticService.getDiagnosisResultsByDoctorId(doctorId);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    /**
     * Get AI diagnosis results by condition
     */
    @GetMapping("/condition")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<AiDiagnosisResult>> getDiagnosisResultsByCondition(@RequestParam String condition) {
        List<AiDiagnosisResult> results = aiDiagnosticService.getDiagnosisResultsByCondition(condition);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    /**
     * Get diagnosis predictions for an AI diagnosis result
     */
    @GetMapping("/{id}/predictions")
    @PreAuthorize("hasRole('ADMIN') or @securityService.canAccessAiDiagnosis(#id)")
    public ResponseEntity<List<DiagnosisPrediction>> getPredictionsForDiagnosis(@PathVariable Long id) {
        List<DiagnosisPrediction> predictions = diagnosisPredictionService.findByAiDiagnosisResultId(id);
        return new ResponseEntity<>(predictions, HttpStatus.OK);
    }

    /**
     * Generate AI diagnosis for a medical record
     */
    @PostMapping("/medical-record/{medicalRecordId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<AiDiagnosisResult> generateDiagnosis(
            @PathVariable Long medicalRecordId,
            @Valid @RequestBody AiDiagnosisRequestDTO requestDTO) {
        AiDiagnosisResult result = aiDiagnosticService.generateDiagnosis(medicalRecordId, requestDTO);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    /**
     * Mark AI diagnosis as reviewed by doctor
     */
    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isDoctorForAiDiagnosis(#id)")
    public ResponseEntity<AiDiagnosisResult> markAsReviewed(
            @PathVariable Long id,
            @RequestBody(required = false) String doctorFeedback) {
        AiDiagnosisResult result = aiDiagnosticService.markAsReviewed(id, doctorFeedback);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Delete AI diagnosis result
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDiagnosisResult(@PathVariable Long id) {
        aiDiagnosticService.deleteDiagnosisResult(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * Get diagnosis results by analyzed date range
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<AiDiagnosisResult>> getDiagnosisByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AiDiagnosisResult> results = aiDiagnosticService.getDiagnosisResultsByAnalyzedDateRange(startDate, endDate);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
    
    /**
     * Get diagnosis results by review status
     */
    @GetMapping("/reviewed/{reviewed}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<AiDiagnosisResult>> getDiagnosisByReviewStatus(@PathVariable Boolean reviewed) {
        List<AiDiagnosisResult> results = aiDiagnosticService.getDiagnosisResultsByReviewStatus(reviewed);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    /**
     * Get diagnosis results by processing status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<AiDiagnosisResult>> getDiagnosisByStatus(@PathVariable AiDiagnosisStatus status) {
        List<AiDiagnosisResult> results = aiDiagnosticService.getDiagnosisResultsByStatus(status);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
    
    /**
     * Get diagnosis results with minimum confidence score
     */
    @GetMapping("/confidence")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<AiDiagnosisResult>> getDiagnosisByMinimumConfidence(@RequestParam Double minScore) {
        List<AiDiagnosisResult> results = aiDiagnosticService.getDiagnosisResultsByMinimumConfidence(minScore);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
    
    /**
     * Get diagnosis results by model version
     */
    @GetMapping("/model-version/{version}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<AiDiagnosisResult>> getDiagnosisByModelVersion(@PathVariable String version) {
        List<AiDiagnosisResult> results = aiDiagnosticService.getDiagnosisResultsByModelVersion(version);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
}