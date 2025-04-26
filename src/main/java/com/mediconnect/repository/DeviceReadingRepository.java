package com.mediconnect.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.DeviceReading;
import com.mediconnect.model.MonitoringDevice;

@Repository
public interface DeviceReadingRepository extends JpaRepository<DeviceReading, Long> {
    
    Optional<DeviceReading> findTopByDeviceAndReadingTypeOrderByTimestampDesc(
            MonitoringDevice device, String readingType);
    
    @Query("SELECT r FROM DeviceReading r WHERE r.device.patient.id = :patientId " +
           "AND r.readingType = :readingType " +
           "AND r.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY r.timestamp DESC")
    List<DeviceReading> findByPatientIdAndReadingTypeAndDateRange(
            @Param("patientId") Long patientId, 
            @Param("readingType") String readingType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
}