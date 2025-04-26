package com.mediconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.TriageRecord;

@Repository
public interface TriageRecordRepository extends JpaRepository<TriageRecord, Long> {
    
    List<TriageRecord> findByPatientIdOrderByTriagedAtDesc(Long patientId);
    
}