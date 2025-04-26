package com.mediconnect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.MedicalRecordBlockchain;

@Repository
public interface MedicalRecordBlockchainRepository extends JpaRepository<MedicalRecordBlockchain, Long> {
    
    Optional<MedicalRecordBlockchain> findByMedicalRecordId(Long medicalRecordId);
    
}