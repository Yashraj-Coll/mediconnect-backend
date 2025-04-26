package com.mediconnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.GenomicData;

@Repository
public interface GenomicDataRepository extends JpaRepository<GenomicData, Long> {
    
    List<GenomicData> findByPatientId(Long patientId);
    
    Optional<GenomicData> findByPatientIdAndDataType(Long patientId, String dataType);
    
}