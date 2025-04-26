// src/main/java/com/mediconnect/service/MedicalDocumentService.java

package com.mediconnect.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.model.MedicalDocument;
import com.mediconnect.model.MedicalRecord;
import com.mediconnect.model.Patient;
import com.mediconnect.repository.MedicalDocumentRepository;
import com.mediconnect.repository.MedicalRecordRepository;
import com.mediconnect.repository.PatientRepository;

@Service
public class MedicalDocumentService {

    @Autowired
    private MedicalDocumentRepository documentRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    /**
     * Upload a document for a patient
     */
    @Transactional
    public MedicalDocument uploadPatientDocument(Long patientId, MultipartFile file, String documentType, String description) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
        
        // Store the file
        String fileName = fileStorageService.storeFile(file);
        
        // Create document record
        MedicalDocument document = new MedicalDocument();
        document.setPatient(patient);
        document.setFileName(fileName);
        document.setOriginalFileName(file.getOriginalFilename());
        document.setFileType(file.getContentType());
        document.setDocumentType(documentType);
        document.setDescription(description);
        document.setFileSize(file.getSize());
        document.setUploadedAt(LocalDateTime.now());
        
        return documentRepository.save(document);
    }
    
    /**
     * Upload a document for a medical record
     */
    @Transactional
    public MedicalDocument uploadMedicalRecordDocument(Long medicalRecordId, MultipartFile file, String documentType, String description) {
        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + medicalRecordId));
        
        // Store the file
        String fileName = fileStorageService.storeFile(file);
        
        // Create document record
        MedicalDocument document = new MedicalDocument();
        document.setPatient(medicalRecord.getPatient());
        document.setMedicalRecord(medicalRecord);
        document.setFileName(fileName);
        document.setOriginalFileName(file.getOriginalFilename());
        document.setFileType(file.getContentType());
        document.setDocumentType(documentType);
        document.setDescription(description);
        document.setFileSize(file.getSize());
        document.setUploadedAt(LocalDateTime.now());
        
        return documentRepository.save(document);
    }
    
    /**
     * Get document by ID
     */
    public MedicalDocument getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
    }
    
    /**
     * Get documents by patient ID
     */
    public List<MedicalDocument> getDocumentsByPatientId(Long patientId) {
        return documentRepository.findByPatientId(patientId);
    }
    
    /**
     * Get documents by medical record ID
     */
    public List<MedicalDocument> getDocumentsByMedicalRecordId(Long medicalRecordId) {
        return documentRepository.findByMedicalRecordId(medicalRecordId);
    }
    
    /**
     * Get documents by type
     */
    public List<MedicalDocument> getDocumentsByType(String documentType) {
        return documentRepository.findByDocumentType(documentType);
    }
    
    /**
     * Download a document
     */
    public Resource downloadDocument(Long documentId) {
        MedicalDocument document = getDocumentById(documentId);
        return fileStorageService.loadFileAsResource(document.getFileName());
    }
    
    /**
     * Delete a document
     */
    @Transactional
    public void deleteDocument(Long documentId) {
        MedicalDocument document = getDocumentById(documentId);
        
        // Delete the file from storage
        boolean deleted = fileStorageService.deleteFile(document.getFileName());
        
        if (!deleted) {
            throw new BadRequestException("Failed to delete file from storage");
        }
        
        // Delete the document record
        documentRepository.delete(document);
    }
    
    /**
     * Update document metadata
     */
    @Transactional
    public MedicalDocument updateDocumentMetadata(Long documentId, String documentType, String description) {
        MedicalDocument document = getDocumentById(documentId);
        
        if (documentType != null) {
            document.setDocumentType(documentType);
        }
        
        if (description != null) {
            document.setDescription(description);
        }
        
        return documentRepository.save(document);
    }
}