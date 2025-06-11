package com.mediconnect.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.Appointment;
import com.mediconnect.model.Appointment.AppointmentStatus;
import com.mediconnect.model.Appointment.AppointmentType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    List<Appointment> findByDoctorId(Long doctorId);
    
    List<Appointment> findByPatientId(Long patientId);
    
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);
    
    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND " +
           "a.appointmentDateTime BETWEEN :startDate AND :endDate")
    List<Appointment> findByDoctorIdAndDateRange(
            @Param("doctorId") Long doctorId, 
            @Param("startDate") OffsetDateTime startDate, 
            @Param("endDate") OffsetDateTime endDate);
    
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND " +
           "a.appointmentDateTime BETWEEN :startDate AND :endDate")
    List<Appointment> findByPatientIdAndDateRange(
            @Param("patientId") Long patientId, 
            @Param("startDate") OffsetDateTime startDate, 
            @Param("endDate") OffsetDateTime endDate);
    
    List<Appointment> findByAppointmentType(AppointmentType appointmentType);
    
    @Query("SELECT a FROM Appointment a WHERE a.status = :status AND " +
           "a.appointmentDateTime > :currentTime ORDER BY a.appointmentDateTime")
    List<Appointment> findUpcomingAppointmentsByStatus(
            @Param("status") AppointmentStatus status, 
            @Param("currentTime") OffsetDateTime currentTime);
    
    @Query("SELECT a FROM Appointment a WHERE a.isPaid = false")
    List<Appointment> findUnpaidAppointments();
    
    /**
     * Find appointments between two dates
     */
    List<Appointment> findByAppointmentDateTimeBetween(OffsetDateTime startDate, OffsetDateTime endDate);
    
    /**
     * NEW: Find appointments by status
     */
    List<Appointment> findByStatus(AppointmentStatus status);
    
    /**
     * NEW: Find today's appointments
     */
    @Query("SELECT a FROM Appointment a WHERE DATE(a.appointmentDateTime) = CURRENT_DATE")
    List<Appointment> findTodaysAppointments();
    
    /**
     * NEW: Find appointments coming up in the next X hours for reminders
     */
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDateTime BETWEEN CURRENT_TIMESTAMP AND :futureTime AND a.status = 'upcoming'")
    List<Appointment> findUpcomingAppointmentsInNextHours(@Param("futureTime") OffsetDateTime futureTime);
    
    /**
     * NEW: Find unpaid appointments that are coming up (for payment reminders)
     */
    @Query("SELECT a FROM Appointment a WHERE a.isPaid = false AND a.appointmentDateTime > CURRENT_TIMESTAMP AND a.appointmentDateTime < :tomorrow")
    List<Appointment> findUnpaidUpcomingAppointments(@Param("tomorrow") OffsetDateTime tomorrow);
    
    /**
     * Advanced appointment search with multiple criteria
     */
    @Query("SELECT a FROM Appointment a " +
           "JOIN a.doctor d JOIN a.patient p " +
           "JOIN d.user du JOIN p.user pu WHERE " +
           "(:doctorId IS NULL OR a.doctor.id = :doctorId) AND " +
           "(:patientId IS NULL OR a.patient.id = :patientId) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:type IS NULL OR a.appointmentType = :type) AND " +
           "(:isPaid IS NULL OR a.isPaid = :isPaid) AND " +
           "(:startDate IS NULL OR a.appointmentDateTime >= :startDate) AND " +
           "(:endDate IS NULL OR a.appointmentDateTime <= :endDate) AND " +
           "(:doctorName IS NULL OR LOWER(du.firstName) LIKE LOWER(CONCAT('%', :doctorName, '%')) OR " +
           "LOWER(du.lastName) LIKE LOWER(CONCAT('%', :doctorName, '%'))) AND " +
           "(:patientName IS NULL OR LOWER(pu.firstName) LIKE LOWER(CONCAT('%', :patientName, '%')) OR " +
           "LOWER(pu.lastName) LIKE LOWER(CONCAT('%', :patientName, '%'))) AND " +
           "(:specialization IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%')))")
    Page<Appointment> advancedSearch(
            @Param("doctorId") Long doctorId,
            @Param("patientId") Long patientId,
            @Param("status") AppointmentStatus status,
            @Param("type") AppointmentType type,
            @Param("isPaid") Boolean isPaid,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            @Param("doctorName") String doctorName,
            @Param("patientName") String patientName,
            @Param("specialization") String specialization,
            Pageable pageable);
    
    /**
     * Find overlapping appointments for a doctor
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND " +
           "a.status <> 'CANCELLED' AND " +
           "((a.appointmentDateTime <= :endTime AND " +
           "a.appointmentDateTime >= :startTime) OR " +
           "(DATEADD(MINUTE, a.durationMinutes, a.appointmentDateTime) >= :startTime AND " +
           "DATEADD(MINUTE, a.durationMinutes, a.appointmentDateTime) <= :endTime) OR " +
           "(a.appointmentDateTime <= :startTime AND " +
           "DATEADD(MINUTE, a.durationMinutes, a.appointmentDateTime) >= :endTime))")
    List<Appointment> findOverlappingAppointments(
            @Param("doctorId") Long doctorId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime);
    
    /**
     * Find appointments with specific duration
     */
    @Query("SELECT a FROM Appointment a WHERE " +
           "a.durationMinutes >= :minDuration AND a.durationMinutes <= :maxDuration")
    List<Appointment> findByDurationRange(
            @Param("minDuration") Integer minDuration,
            @Param("maxDuration") Integer maxDuration);
    
    /**
     * Find available doctors for the given time slot and specialty
     */
    @Query("SELECT d.id, u.firstName, u.lastName, d.specialization, d.averageRating FROM Doctor d " +
           "JOIN d.user u " +
           "WHERE d.specialization = :specialty " +
           "AND d.id NOT IN (SELECT a.doctor.id FROM Appointment a WHERE a.appointmentDateTime BETWEEN :startTime AND :endTime)")
    List<Object[]> findAvailableDoctors(
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime,
            @Param("specialty") String specialty);
    
    
 // Add this method to your AppointmentRepository interface
    @Modifying
    @Transactional
    @Query("DELETE FROM Appointment a WHERE a.patient.id = :patientId")
    void deleteByPatientId(@Param("patientId") Long patientId);
}