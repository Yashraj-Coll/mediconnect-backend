package com.mediconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.MedicalDocument;

@Repository
public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, Long> {
    
    List<MedicalDocument> findByPatientId(Long patientId);
    
    List<MedicalDocument> findByMedicalRecordId(Long medicalRecordId);
    
    List<MedicalDocument> findByDocumentType(String documentType);
    
}