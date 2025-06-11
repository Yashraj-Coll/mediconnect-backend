package com.mediconnect.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.LabTestBooking;
import com.mediconnect.model.LabTestBooking.BookingStatus;

@Repository
public interface LabTestBookingRepository extends JpaRepository<LabTestBooking, Long> {
    
    List<LabTestBooking> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    
    List<LabTestBooking> findAllByOrderByCreatedAtDesc();
    
    List<LabTestBooking> findByStatusOrderByCreatedAtDesc(BookingStatus status);
    
    List<LabTestBooking> findByIsPaidOrderByCreatedAtDesc(Boolean isPaid);
    
    @Query("SELECT ltb FROM LabTestBooking ltb WHERE ltb.createdAt BETWEEN :startDate AND :endDate ORDER BY ltb.createdAt DESC")
    List<LabTestBooking> findByDateRange(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT ltb FROM LabTestBooking ltb WHERE ltb.patient.id = :patientId AND ltb.status = :status ORDER BY ltb.createdAt DESC")
    List<LabTestBooking> findByPatientIdAndStatus(
            @Param("patientId") Long patientId, 
            @Param("status") BookingStatus status);
    
    @Query("SELECT ltb FROM LabTestBooking ltb WHERE ltb.testName LIKE %:testName% ORDER BY ltb.createdAt DESC")
    List<LabTestBooking> findByTestNameContaining(@Param("testName") String testName);
    
    @Query("SELECT ltb FROM LabTestBooking ltb WHERE ltb.homeCollection = :homeCollection ORDER BY ltb.createdAt DESC")
    List<LabTestBooking> findByHomeCollection(@Param("homeCollection") Boolean homeCollection);
    
    @Query("SELECT COUNT(ltb) FROM LabTestBooking ltb WHERE ltb.status = :status")
    Long countByStatus(@Param("status") BookingStatus status);
    
    @Query("SELECT COUNT(ltb) FROM LabTestBooking ltb WHERE ltb.isPaid = :isPaid")
    Long countByIsPaid(@Param("isPaid") Boolean isPaid);
}