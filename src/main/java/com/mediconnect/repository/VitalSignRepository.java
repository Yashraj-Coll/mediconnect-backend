package com.mediconnect.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.VitalSign;

@Repository
public interface VitalSignRepository extends JpaRepository<VitalSign, Long> {

    // 1. Find all by patient ID (newest first) - EXPLICIT QUERY
    @Query("SELECT v FROM VitalSign v WHERE v.patient.id = :patientId ORDER BY v.readingDate DESC")
    List<VitalSign> findByPatientIdOrderByReadingDateDesc(@Param("patientId") Long patientId);

    // 2. Find by patient ID + vitalType (newest first) - EXPLICIT QUERY
    @Query("SELECT v FROM VitalSign v WHERE v.patient.id = :patientId AND v.vitalType = :vitalType ORDER BY v.readingDate DESC")
    List<VitalSign> findByPatientIdAndVitalTypeOrderByReadingDateDesc(@Param("patientId") Long patientId, @Param("vitalType") String vitalType);

    // 3. Find by patient ID + date range (newest first) - EXPLICIT QUERY
    @Query("SELECT v FROM VitalSign v WHERE v.patient.id = :patientId AND v.readingDate BETWEEN :startDate AND :endDate ORDER BY v.readingDate DESC")
    List<VitalSign> findByPatientIdAndReadingDateBetweenOrderByReadingDateDesc(@Param("patientId") Long patientId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    // 4. Find by patient ID and status not equal - EXPLICIT QUERY
    @Query("SELECT v FROM VitalSign v WHERE v.patient.id = :patientId AND v.status <> :status")
    List<VitalSign> findByPatientIdAndStatusNot(@Param("patientId") Long patientId, @Param("status") VitalSign.Status status);

    // 5. Find by patient ID + vitalType after a date (ordered) - EXPLICIT QUERY
    @Query("SELECT v FROM VitalSign v WHERE v.patient.id = :patientId AND v.vitalType = :vitalType AND v.readingDate > :startDate ORDER BY v.readingDate")
    List<VitalSign> findByPatientIdAndVitalTypeAndReadingDateAfterOrderByReadingDate(@Param("patientId") Long patientId, @Param("vitalType") String vitalType, @Param("startDate") Date startDate);

    // 6. Find latest readings per type for a patient with proper JOINs - COMPLETELY REWRITTEN
    @Query("SELECT v FROM VitalSign v JOIN FETCH v.patient p JOIN FETCH p.user " +
           "LEFT JOIN FETCH v.doctor d LEFT JOIN FETCH d.user " +
           "WHERE v.patient.id = :patientId AND v.id IN (" +
           "SELECT MAX(v2.id) FROM VitalSign v2 WHERE v2.patient.id = :patientId GROUP BY v2.vitalType" +
           ") ORDER BY v.readingDate DESC")
    List<VitalSign> findLatestByPatientId(@Param("patientId") Long patientId);
}