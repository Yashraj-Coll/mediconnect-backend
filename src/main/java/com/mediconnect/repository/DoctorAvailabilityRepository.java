package com.mediconnect.repository;

import com.mediconnect.model.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    
    // Find all slots for a doctor on a given day (dayOfWeek e.g. "MONDAY", "TUESDAY" ...)
    List<DoctorAvailability> findByDoctorIdAndDayOfWeek(Long doctorId, String dayOfWeek);

    // Find slots for a doctor on a specific day and appointment type
    @Query("SELECT da FROM DoctorAvailability da WHERE da.doctor.id = :doctorId AND da.dayOfWeek = :dayOfWeek AND da.appointmentType.typeCode = :appointmentType")
    List<DoctorAvailability> findByDoctorIdAndDayOfWeekAndAppointmentType(
        @Param("doctorId") Long doctorId, 
        @Param("dayOfWeek") String dayOfWeek, 
        @Param("appointmentType") String appointmentType
    );

    // Find all slots for a doctor and appointment type (all days)
    @Query("SELECT da FROM DoctorAvailability da WHERE da.doctor.id = :doctorId AND da.appointmentType.typeCode = :appointmentType")
    List<DoctorAvailability> findByDoctorIdAndAppointmentType(
        @Param("doctorId") Long doctorId, 
        @Param("appointmentType") String appointmentType
    );

    // Find available slots for a doctor on a specific day and appointment type
    @Query("SELECT da FROM DoctorAvailability da WHERE da.doctor.id = :doctorId AND da.dayOfWeek = :dayOfWeek AND da.appointmentType.typeCode = :appointmentType AND da.isAvailable = true")
    List<DoctorAvailability> findAvailableSlotsByDoctorAndDayAndType(
        @Param("doctorId") Long doctorId, 
        @Param("dayOfWeek") String dayOfWeek, 
        @Param("appointmentType") String appointmentType
    );
}