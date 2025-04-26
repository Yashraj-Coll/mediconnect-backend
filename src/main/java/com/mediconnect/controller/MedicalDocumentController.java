// src/main/java/com/mediconnect/controller/MedicalDocumentController.java

package com.mediconnect.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mediconnect.dto.MedicalDocumentDTO;
import com.mediconnect.model.MedicalDocument;
import com.mediconnect.service.MedicalDocumentService;

@RestController
@RequestMapping("/api/medical-documents")
public class MedicalDocumentController {

    @Autowired
    private MedicalDocumentService documentService;
    
    /**
     * Upload a document for a patient
     */
    @PostMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or @securityService.isPatientWithId(#patientId)")
    public ResponseEntity<MedicalDocumentDTO> uploadPatientDocument(
            @PathVariable Long patientId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentType", required = false, defaultValue = "Other") String documentType,
            @RequestParam(value = "description", required = false) String description) {
        
        MedicalDocument document = documentService.uploadPatientDocument(patientId, file, documentType, description);
        
        MedicalDocumentDTO documentDTO = mapToDTO(document);
        
        return ResponseEntity.ok(documentDTO);
    }
    
    /**
     * Upload a document for a medical record
     */
    @PostMapping("/medical-record/{medicalRecordId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or @securityService.canAccessMedicalRecord(#medicalRecordId)")
    public ResponseEntity<MedicalDocumentDTO> uploadMedicalRecordDocument(
            @PathVariable Long medicalRecordId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentType", required = false, defaultValue = "Other") String documentType,
            @RequestParam(value = "description", required = false) String description) {
        
        MedicalDocument document = documentService.uploadMedicalRecordDocument(medicalRecordId, file, documentType, description);
        
        MedicalDocumentDTO documentDTO = mapToDTO(document);
        
        return ResponseEntity.ok(documentDTO);
    }
    
    /**
     * Get document by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or @securityService.canAccessDocument(#id)")
    public ResponseEntity<MedicalDocumentDTO> getDocumentById(@PathVariable Long id) {
        MedicalDocument document = documentService.getDocumentById(id);
        MedicalDocumentDTO documentDTO = mapToDTO(document);
        
        return ResponseEntity.ok(documentDTO);
    }
    
    /**
     * Get documents by patient ID
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or @securityService.isPatientWithId(#patientId)")
    public ResponseEntity<List<MedicalDocumentDTO>> getDocumentsByPatientId(@PathVariable Long patientId) {
        List<MedicalDocument> documents = documentService.getDocumentsByPatientId(patientId);
        List<MedicalDocumentDTO> documentDTOs = documents.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(documentDTOs);
    }
    
    /**
     * Get documents by medical record ID
     */
    @GetMapping("/medical-record/{medicalRecordId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or @securityService.canAccessMedicalRecord(#medicalRecordId)")
    public ResponseEntity<List<MedicalDocumentDTO>> getDocumentsByMedicalRecordId(@PathVariable Long medicalRecordId) {
        List<MedicalDocument> documents = documentService.getDocumentsByMedicalRecordId(medicalRecordId);
        List<MedicalDocumentDTO> documentDTOs = documents.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(documentDTOs);
    }
    
    /**
     * Download a document
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or @securityService.canAccessDocument(#id)")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        MedicalDocument document = documentService.getDocumentById(id);
        Resource resource = documentService.downloadDocument(id);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                .body(resource);
    }
    
    /**
     * Delete a document
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or @securityService.canAccessDocument(#id)")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Update document metadata
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or @securityService.canAccessDocument(#id)")
    public ResponseEntity<MedicalDocumentDTO> updateDocumentMetadata(
            @PathVariable Long id,
            @RequestParam(value = "documentType", required = false) String documentType,
            @RequestParam(value = "description", required = false) String description) {
        
        MedicalDocument document = documentService.updateDocumentMetadata(id, documentType, description);
        MedicalDocumentDTO documentDTO = mapToDTO(document);
        
        return ResponseEntity.ok(documentDTO);
    }
    
    /**
     * Map document entity to DTO
     */
    private MedicalDocumentDTO mapToDTO(MedicalDocument document) {
        String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/medical-documents/")
                .path(document.getId().toString())
                .path("/download")
                .toUriString();
        
        MedicalDocumentDTO dto = new MedicalDocumentDTO();
        dto.setId(document.getId());
        dto.setPatientId(document.getPatient().getId());
        dto.setMedicalRecordId(document.getMedicalRecord() != null ? document.getMedicalRecord().getId() : null);
        dto.setFileName(document.getOriginalFileName());
        dto.setFileType(document.getFileType());
        dto.setDocumentType(document.getDocumentType());
        dto.setDescription(document.getDescription());
        dto.setFileSize(document.getFileSize());
        dto.setUploadedAt(document.getUploadedAt());
        dto.setDownloadUrl(downloadUrl);
        
        return dto;
    }
}