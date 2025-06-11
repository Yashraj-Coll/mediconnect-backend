package com.mediconnect.repository;

import com.mediconnect.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatientIdOrderByRecordDateDesc(Long patientId);
 // ADD this method to your existing MedicalRecordRepository.java
    List<MedicalRecord> findByPatientIdOrderByUpdatedAtDesc(Long patientId);

    @Query("SELECT m FROM MedicalRecord m WHERE m.patient.id = :patientId ORDER BY m.recordDate DESC")
    List<MedicalRecord> findLatestRecordsByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT m FROM MedicalRecord m WHERE m.patient.id = :patientId AND m.recordDate BETWEEN :start AND :end")
    List<MedicalRecord> findByPatientIdAndDateRange(@Param("patientId") Long patientId,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);
}
