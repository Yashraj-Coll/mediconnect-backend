// src/main/java/com/mediconnect/controller/MedicalImageController.java

package com.mediconnect.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mediconnect.model.ImageAnalysisResult;
import com.mediconnect.model.ImageAnalysisResult.AnalysisStatus;
import com.mediconnect.repository.ImageAnalysisResultRepository;
import com.mediconnect.service.MedicalImageAnalysisService;

@RestController
@RequestMapping("/api/medical-images")
public class MedicalImageController {

    @Autowired
    private MedicalImageAnalysisService imageAnalysisService;
    
    @Autowired
    private ImageAnalysisResultRepository imageAnalysisResultRepository;
    
    /**
     * Upload and analyze an X-ray image
     */
    @PostMapping("/xray/{medicalRecordId}")
    @PreAuthorize("hasRole('DOCTOR') or @securityService.canAccessMedicalRecord(#medicalRecordId)")
    public ResponseEntity<ImageAnalysisResult> analyzeXRay(
            @PathVariable Long medicalRecordId,
            @RequestParam("image") MultipartFile imageFile) {
        
        CompletableFuture<ImageAnalysisResult> futureResult = imageAnalysisService.analyzeXRay(medicalRecordId, imageFile);
        
        // Return immediate response with the created analysis job
        ImageAnalysisResult initialResult = futureResult.getNow(null);
        return new ResponseEntity<>(initialResult, HttpStatus.ACCEPTED);
    }
    
    /**
     * Upload and analyze an MRI scan
     */
    @PostMapping("/mri/{medicalRecordId}")
    @PreAuthorize("hasRole('DOCTOR') or @securityService.canAccessMedicalRecord(#medicalRecordId)")
    public ResponseEntity<ImageAnalysisResult> analyzeMRI(
            @PathVariable Long medicalRecordId,
            @RequestParam("image") MultipartFile imageFile) {
        
        CompletableFuture<ImageAnalysisResult> futureResult = imageAnalysisService.analyzeMRI(medicalRecordId, imageFile);
        
        ImageAnalysisResult initialResult = futureResult.getNow(null);
        return new ResponseEntity<>(initialResult, HttpStatus.ACCEPTED);
    }
    
    /**
     * Upload and analyze a CT scan
     */
    @PostMapping("/ct-scan/{medicalRecordId}")
    @PreAuthorize("hasRole('DOCTOR') or @securityService.canAccessMedicalRecord(#medicalRecordId)")
    public ResponseEntity<ImageAnalysisResult> analyzeCTScan(
            @PathVariable Long medicalRecordId,
            @RequestParam("image") MultipartFile imageFile) {
        
        CompletableFuture<ImageAnalysisResult> futureResult = imageAnalysisService.analyzeCTScan(medicalRecordId, imageFile);
        
        ImageAnalysisResult initialResult = futureResult.getNow(null);
        return new ResponseEntity<>(initialResult, HttpStatus.ACCEPTED);
    }
    
    /**
     * Upload and analyze a dermatology image
     */
    @PostMapping("/dermatology/{medicalRecordId}")
    @PreAuthorize("hasRole('DOCTOR') or @securityService.canAccessMedicalRecord(#medicalRecordId)")
    public ResponseEntity<ImageAnalysisResult> analyzeDermatologyImage(
            @PathVariable Long medicalRecordId,
            @RequestParam("image") MultipartFile imageFile) {
        
        CompletableFuture<ImageAnalysisResult> futureResult = imageAnalysisService.analyzeDermatologyImage(medicalRecordId, imageFile);
        
        ImageAnalysisResult initialResult = futureResult.getNow(null);
        return new ResponseEntity<>(initialResult, HttpStatus.ACCEPTED);
    }
    
    /**
     * Get analysis result by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or @securityService.canAccessImageAnalysis(#id)")
    public ResponseEntity<ImageAnalysisResult> getAnalysisResult(@PathVariable Long id) {
        return imageAnalysisResultRepository.findById(id)
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * Get all analysis results for a medical record
     */
    @GetMapping("/medical-record/{medicalRecordId}")
    @PreAuthorize("hasRole('DOCTOR') or @securityService.canAccessMedicalRecord(#medicalRecordId)")
    public ResponseEntity<List<ImageAnalysisResult>> getAnalysisResultsByMedicalRecord(@PathVariable Long medicalRecordId) {
        List<ImageAnalysisResult> results = imageAnalysisResultRepository.findByMedicalRecordId(medicalRecordId);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
    
    /**
     * Get analysis results by status for a medical record
     */
    @GetMapping("/medical-record/{medicalRecordId}/status/{status}")
    @PreAuthorize("hasRole('DOCTOR') or @securityService.canAccessMedicalRecord(#medicalRecordId)")
    public ResponseEntity<List<ImageAnalysisResult>> getAnalysisResultsByMedicalRecordAndStatus(
            @PathVariable Long medicalRecordId,
            @PathVariable AnalysisStatus status) {
        
        List<ImageAnalysisResult> results = imageAnalysisResultRepository.findByMedicalRecordIdAndStatus(medicalRecordId, status);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
}