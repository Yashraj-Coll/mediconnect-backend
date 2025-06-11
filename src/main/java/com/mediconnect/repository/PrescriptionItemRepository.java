package com.mediconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.PrescriptionItem;

@Repository
public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {

    /**
     * Find all prescription items for a prescription
     */
    List<PrescriptionItem> findByPrescriptionId(Long prescriptionId);
    
    /**
     * Delete all prescription items for a prescription
     */
    @Modifying
    @Query("DELETE FROM PrescriptionItem pi WHERE pi.prescription.id = :prescriptionId")
    void deleteByPrescriptionId(@Param("prescriptionId") Long prescriptionId);
}