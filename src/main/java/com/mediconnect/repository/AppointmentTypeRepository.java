package com.mediconnect.repository;

import com.mediconnect.model.AppointmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentTypeRepository extends JpaRepository<AppointmentType, Long> {
    
    // Find by type code
    Optional<AppointmentType> findByTypeCode(String typeCode);
    
    // Find by type name
    Optional<AppointmentType> findByTypeName(String typeName);
    
    // Find all active appointment types
    @Query("SELECT at FROM AppointmentType at WHERE at.isActive = true ORDER BY at.id")
    List<AppointmentType> findAllActive();
    
    // Check if type code exists
    boolean existsByTypeCode(String typeCode);
    
    // Check if type name exists
    boolean existsByTypeName(String typeName);
}