package com.mediconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.PrescriptionItem;

@Repository
public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {
    
    List<PrescriptionItem> findByPrescriptionId(Long prescriptionId);
    
    @Query("SELECT pi FROM PrescriptionItem pi WHERE " +
           "LOWER(pi.medicationName) LIKE LOWER(CONCAT('%', :medicationName, '%'))")
    List<PrescriptionItem> findByMedicationName(@Param("medicationName") String medicationName);
    
    @Query("SELECT pi FROM PrescriptionItem pi WHERE " +
           "pi.prescription.patient.id = :patientId")
    List<PrescriptionItem> findByPatientId(@Param("patientId") Long patientId);
    
    @Query("SELECT pi FROM PrescriptionItem pi WHERE " +
           "pi.prescription.doctor.id = :doctorId")
    List<PrescriptionItem> findByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT pi FROM PrescriptionItem pi WHERE " +
           "pi.beforeMeal = :beforeMeal")
    List<PrescriptionItem> findByMealInstruction(@Param("beforeMeal") Boolean beforeMeal);
    
    @Query("SELECT pi FROM PrescriptionItem pi WHERE " +
           "pi.route = :route")
    List<PrescriptionItem> findByRoute(@Param("route") String route);
    
    @Query("SELECT DISTINCT pi.medicationName FROM PrescriptionItem pi " +
           "WHERE pi.prescription.patient.id = :patientId")
    List<String> findDistinctMedicationsByPatientId(@Param("patientId") Long patientId);
}