package com.mediconnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.MonitoringDevice;

@Repository
public interface MonitoringDeviceRepository extends JpaRepository<MonitoringDevice, Long> {
    
    List<MonitoringDevice> findByPatientId(Long patientId);
    
    Optional<MonitoringDevice> findByDeviceId(String deviceId);
    
}