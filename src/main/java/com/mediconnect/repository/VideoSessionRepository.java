package com.mediconnect.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.VideoSession;
import com.mediconnect.model.VideoSession.SessionStatus;

@Repository
public interface VideoSessionRepository extends JpaRepository<VideoSession, Long> {
    
    Optional<VideoSession> findByAppointmentId(Long appointmentId);
    
    List<VideoSession> findByStatus(SessionStatus status);
    
    @Query("SELECT v FROM VideoSession v WHERE v.scheduledStartTime BETWEEN :startDate AND :endDate")
    List<VideoSession> findByScheduledDateRange(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT v FROM VideoSession v WHERE v.appointment.doctor.id = :doctorId " +
           "AND v.status = :status")
    List<VideoSession> findByDoctorIdAndStatus(
            @Param("doctorId") Long doctorId, 
            @Param("status") SessionStatus status);
    
    @Query("SELECT v FROM VideoSession v WHERE v.appointment.patient.id = :patientId " +
           "AND v.status = :status")
    List<VideoSession> findByPatientIdAndStatus(
            @Param("patientId") Long patientId, 
            @Param("status") SessionStatus status);
    
    @Query("SELECT v FROM VideoSession v WHERE v.scheduledStartTime > :now " +
           "AND v.scheduledStartTime < :timeLimit " +
           "AND v.status = 'SCHEDULED' " +
           "ORDER BY v.scheduledStartTime")
    List<VideoSession> findUpcomingSessions(
            @Param("now") LocalDateTime now, 
            @Param("timeLimit") LocalDateTime timeLimit);
    
    @Query("SELECT v FROM VideoSession v WHERE v.recordingEnabled = true")
    List<VideoSession> findSessionsWithRecording();
    
    Optional<VideoSession> findBySessionId(String sessionId);
}