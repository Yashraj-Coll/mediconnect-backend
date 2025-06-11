package com.mediconnect.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.Patient;
import com.mediconnect.model.Patient.Gender;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    Optional<Patient> findByUserId(Long userId);
    
    @Query("SELECT p FROM Patient p WHERE LOWER(p.user.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.user.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Patient> searchPatients(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Patient p WHERE p.chronicDiseases LIKE %:condition% OR p.allergies LIKE %:condition%")
    List<Patient> findByMedicalCondition(@Param("condition") String condition);
    
    @Query("SELECT DISTINCT p FROM Patient p JOIN FETCH p.user JOIN p.appointments a WHERE a.doctor.id = :doctorId")
    List<Patient> findPatientsByDoctor(@Param("doctorId") Long doctorId);
	@Query("SELECT p FROM Patient p JOIN FETCH p.user")
List<Patient> findAllWithUser();
    
    @Query("SELECT p FROM Patient p WHERE p.insuranceProvider = :provider")
    List<Patient> findByInsuranceProvider(@Param("provider") String provider);
    
    /**
     * Advanced search with multiple criteria
     */
    @Query("SELECT DISTINCT p FROM Patient p JOIN p.user u WHERE " +
           "(:name IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:gender IS NULL OR p.gender = :gender) AND " +
           "(:bloodGroup IS NULL OR p.bloodGroup = :bloodGroup) AND " +
           "(:condition IS NULL OR LOWER(p.chronicDiseases) LIKE LOWER(CONCAT('%', :condition, '%')) OR " +
           "LOWER(p.allergies) LIKE LOWER(CONCAT('%', :condition, '%'))) AND " +
           "(:insuranceProvider IS NULL OR LOWER(p.insuranceProvider) LIKE LOWER(CONCAT('%', :insuranceProvider, '%'))) AND " +
           "(:minAge IS NULL OR FUNCTION('TIMESTAMPDIFF', YEAR, p.dateOfBirth, CURRENT_DATE) >= :minAge) AND " +
           "(:maxAge IS NULL OR FUNCTION('TIMESTAMPDIFF', YEAR, p.dateOfBirth, CURRENT_DATE) <= :maxAge) AND " +
           "(:minWeight IS NULL OR p.weight >= :minWeight) AND " +
           "(:maxWeight IS NULL OR p.weight <= :maxWeight) AND " +
           "(:minHeight IS NULL OR p.height >= :minHeight) AND " +
           "(:maxHeight IS NULL OR p.height <= :maxHeight)")
    Page<Patient> advancedSearch(
            @Param("name") String name,
            @Param("email") String email,
            @Param("gender") Gender gender,
            @Param("bloodGroup") String bloodGroup,
            @Param("condition") String condition,
            @Param("insuranceProvider") String insuranceProvider,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("minWeight") Double minWeight,
            @Param("maxWeight") Double maxWeight,
            @Param("minHeight") Double minHeight,
            @Param("maxHeight") Double maxHeight,
            Pageable pageable);
    
    /**
     * Find patients with appointments in date range
     */
    @Query("SELECT DISTINCT p FROM Patient p JOIN p.appointments a WHERE " +
           "a.appointmentDateTime BETWEEN :startDate AND :endDate")
    List<Patient> findPatientsWithAppointmentsInDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find patients with specific vital signs range
     */
    @Query("SELECT DISTINCT p FROM Patient p JOIN p.medicalRecords mr WHERE " +
           "(:minHR IS NULL OR mr.heartRate >= :minHR) AND " +
           "(:maxHR IS NULL OR mr.heartRate <= :maxHR) AND " +
           "(:minBP IS NULL OR CAST(SUBSTRING_INDEX(mr.bloodPressure, '/', 1) AS int) >= :minBP) AND " +
           "(:maxBP IS NULL OR CAST(SUBSTRING_INDEX(mr.bloodPressure, '/', 1) AS int) <= :maxBP) AND " +
           "(:minTemp IS NULL OR mr.temperature >= :minTemp) AND " +
           "(:maxTemp IS NULL OR mr.temperature <= :maxTemp) AND " +
           "(:minOxygen IS NULL OR mr.oxygenSaturation >= :minOxygen) AND " +
           "(:maxOxygen IS NULL OR mr.oxygenSaturation <= :maxOxygen)")
    List<Patient> findPatientsByVitalSigns(
            @Param("minHR") Integer minHeartRate,
            @Param("maxHR") Integer maxHeartRate,
            @Param("minBP") Integer minSystolicBP,
            @Param("maxBP") Integer maxSystolicBP,
            @Param("minTemp") Double minTemperature,
            @Param("maxTemp") Double maxTemperature,
            @Param("minOxygen") Double minOxygenSaturation,
            @Param("maxOxygen") Double maxOxygenSaturation);
    
    /**
     * Find patients by age range
     */
    @Query("SELECT p FROM Patient p WHERE " +
           "FUNCTION('TIMESTAMPDIFF', YEAR, p.dateOfBirth, CURRENT_DATE) BETWEEN :minAge AND :maxAge")
    List<Patient> findByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);
    
    /**
     * Find patients by BMI range (calculated field)
     */
    @Query("SELECT p FROM Patient p WHERE " +
           "p.weight IS NOT NULL AND p.height IS NOT NULL AND " +
           "(p.weight / ((p.height/100) * (p.height/100))) BETWEEN :minBMI AND :maxBMI")
    List<Patient> findByBMIRange(@Param("minBMI") Double minBMI, @Param("maxBMI") Double maxBMI);
}