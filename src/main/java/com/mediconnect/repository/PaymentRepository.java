package com.mediconnect.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.Payment;
import com.mediconnect.model.Payment.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByPaymentIntentId(String paymentIntentId);
    
    Optional<Payment> findByCheckoutSessionId(String checkoutSessionId);
    
    List<Payment> findByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByDateRange(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p FROM Payment p WHERE p.appointment.doctor.id = :doctorId")
    List<Payment> findByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT p FROM Payment p WHERE p.appointment.patient.id = :patientId")
    List<Payment> findByPatientId(@Param("patientId") Long patientId);
}