package com.mediconnect.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.RazorpayPayment;
import com.mediconnect.model.RazorpayPayment.PaymentStatus;

@Repository
public interface RazorpayPaymentRepository extends JpaRepository<RazorpayPayment, Long> {
    
    Optional<RazorpayPayment> findByRazorpayOrderId(String razorpayOrderId);
    
    Optional<RazorpayPayment> findByRazorpayPaymentId(String razorpayPaymentId);
    
    List<RazorpayPayment> findByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);
    
    List<RazorpayPayment> findByStatus(PaymentStatus status);
    
    @Query("SELECT p FROM RazorpayPayment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<RazorpayPayment> findByDateRange(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p FROM RazorpayPayment p WHERE p.appointment.doctor.id = :doctorId")
    List<RazorpayPayment> findByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT p FROM RazorpayPayment p WHERE p.appointment.patient.id = :patientId")
    List<RazorpayPayment> findByPatientId(@Param("patientId") Long patientId);
}