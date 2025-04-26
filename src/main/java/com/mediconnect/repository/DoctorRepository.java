package com.mediconnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.Doctor;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    
    Optional<Doctor> findByUserId(Long userId);
    
    List<Doctor> findBySpecialization(String specialization);
    
    @Query("SELECT d FROM Doctor d WHERE d.isAvailableForEmergency = true")
    List<Doctor> findAvailableForEmergency();
    
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.specialization) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.user.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.user.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Doctor> searchDoctors(@Param("keyword") String keyword);
    
    @Query("SELECT d FROM Doctor d WHERE d.consultationFee <= :maxFee")
    List<Doctor> findByConsultationFeeLessThanEqual(@Param("maxFee") Double maxFee);
    
    @Query("SELECT d FROM Doctor d ORDER BY d.averageRating DESC")
    List<Doctor> findTopRatedDoctors();
    
    /**
     * Advanced doctor search with multiple criteria
     */
    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.user u WHERE " +
           "(:name IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:specialization IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))) AND " +
           "(:hospitalAffiliation IS NULL OR LOWER(d.hospitalAffiliation) LIKE LOWER(CONCAT('%', :hospitalAffiliation, '%'))) AND " +
           "(:minYearsExperience IS NULL OR d.yearsOfExperience >= :minYearsExperience) AND " +
           "(:maxFee IS NULL OR d.consultationFee <= :maxFee) AND " +
           "(:minRating IS NULL OR d.averageRating >= :minRating) AND " +
           "(:isEmergencyAvailable IS NULL OR d.isAvailableForEmergency = :isEmergencyAvailable)")
    Page<Doctor> advancedSearch(
            @Param("name") String name,
            @Param("specialization") String specialization,
            @Param("hospitalAffiliation") String hospitalAffiliation, 
            @Param("minYearsExperience") Integer minYearsExperience,
            @Param("maxFee") Double maxFee,
            @Param("minRating") Integer minRating,
            @Param("isEmergencyAvailable") Boolean isEmergencyAvailable,
            Pageable pageable);
    
    /**
     * Find doctors with specific availability pattern
     */
    @Query(value = "SELECT d.* FROM doctors d " +
                  "JOIN doctor_availability da ON d.id = da.doctor_id " +
                  "WHERE da.day_of_week = :dayOfWeek " +
                  "AND da.start_time <= :time " +
                  "AND da.end_time >= :time", 
           nativeQuery = true)
    List<Doctor> findByAvailability(@Param("dayOfWeek") String dayOfWeek, @Param("time") String time);
    
    /**
     * Find doctors by language spoken
     */
    @Query(value = "SELECT d.* FROM doctors d " +
                  "JOIN doctor_languages dl ON d.id = dl.doctor_id " +
                  "WHERE LOWER(dl.language) LIKE LOWER(CONCAT('%', :language, '%'))", 
           nativeQuery = true)
    List<Doctor> findByLanguage(@Param("language") String language);
    
    /**
     * Find doctors by insurance accepted
     */
    @Query(value = "SELECT d.* FROM doctors d " +
                  "JOIN doctor_insurance di ON d.id = di.doctor_id " +
                  "WHERE LOWER(di.insurance_provider) LIKE LOWER(CONCAT('%', :provider, '%'))", 
           nativeQuery = true)
    List<Doctor> findByInsuranceAccepted(@Param("provider") String provider);
}