package com.mediconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mediconnect.model.GenomicRiskFactor;

@Repository
public interface GenomicRiskFactorRepository extends JpaRepository<GenomicRiskFactor, Long> {
    
    List<GenomicRiskFactor> findByGenomicDataId(Long genomicDataId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM GenomicRiskFactor g WHERE g.genomicData.id = :genomicDataId")
    void deleteByGenomicDataId(Long genomicDataId);
    
}