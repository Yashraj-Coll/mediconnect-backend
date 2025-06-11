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
    
    // Basic finder methods
    Optional<Doctor> findByUserId(Long userId);
    
    List<Doctor> findBySpecialization(String specialization);
    
    List<Doctor> findByClinicCity(String city);
    
    List<Doctor> findByClinicState(String state);
    
    List<Doctor> findByHospitalAffiliation(String hospitalAffiliation);
    
    // Emergency availability
    @Query("SELECT d FROM Doctor d WHERE d.isAvailableForEmergency = true")
    List<Doctor> findAvailableForEmergency();
    
    // Online consultation availability
    @Query("SELECT d FROM Doctor d WHERE d.onlineConsultation = true")
    List<Doctor> findOfferingOnlineConsultation();
    
    // Search functionality
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.specialization) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.user.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.user.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.hospitalAffiliation) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.clinicName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.clinicCity) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Doctor> searchDoctors(@Param("keyword") String keyword);
    
    // Fee-based queries
    @Query("SELECT d FROM Doctor d WHERE d.consultationFee <= :maxFee")
    List<Doctor> findByConsultationFeeLessThanEqual(@Param("maxFee") Double maxFee);
    
    @Query("SELECT d FROM Doctor d WHERE d.consultationFee BETWEEN :minFee AND :maxFee")
    List<Doctor> findByConsultationFeeBetween(@Param("minFee") Double minFee, @Param("maxFee") Double maxFee);
    
    // Rating-based queries
    @Query("SELECT d FROM Doctor d WHERE d.averageRating >= :minRating ORDER BY d.averageRating DESC")
    List<Doctor> findByMinimumRating(@Param("minRating") Integer minRating);
    
    @Query("SELECT d FROM Doctor d ORDER BY d.averageRating DESC")
    List<Doctor> findTopRatedDoctors();
    
    // Experience-based queries
    @Query("SELECT d FROM Doctor d WHERE d.yearsOfExperience >= :minExperience ORDER BY d.yearsOfExperience DESC")
    List<Doctor> findByMinimumExperience(@Param("minExperience") Integer minExperience);
    
    /**
     * Advanced doctor search with multiple criteria
     */
    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.user u WHERE " +
           "(:name IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:specialization IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))) AND " +
           "(:hospitalAffiliation IS NULL OR LOWER(d.hospitalAffiliation) LIKE LOWER(CONCAT('%', :hospitalAffiliation, '%'))) AND " +
           "(:city IS NULL OR LOWER(d.clinicCity) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:state IS NULL OR LOWER(d.clinicState) LIKE LOWER(CONCAT('%', :state, '%'))) AND " +
           "(:minYearsExperience IS NULL OR d.yearsOfExperience >= :minYearsExperience) AND " +
           "(:maxFee IS NULL OR d.consultationFee <= :maxFee) AND " +
           "(:minRating IS NULL OR d.averageRating >= :minRating) AND " +
           "(:isEmergencyAvailable IS NULL OR d.isAvailableForEmergency = :isEmergencyAvailable) AND " +
           "(:isOnlineConsultation IS NULL OR d.onlineConsultation = :isOnlineConsultation)")
    Page<Doctor> advancedSearch(
            @Param("name") String name,
            @Param("specialization") String specialization,
            @Param("hospitalAffiliation") String hospitalAffiliation,
            @Param("city") String city,
            @Param("state") String state,
            @Param("minYearsExperience") Integer minYearsExperience,
            @Param("maxFee") Double maxFee,
            @Param("minRating") Integer minRating,
            @Param("isEmergencyAvailable") Boolean isEmergencyAvailable,
            @Param("isOnlineConsultation") Boolean isOnlineConsultation,
            Pageable pageable);
    
    /**
     * Find doctors available on specific days based on clinic timings
     */
    @Query("SELECT d FROM Doctor d WHERE " +
           "(:day = 'MONDAY' AND d.mondayTiming IS NOT NULL AND LOWER(d.mondayTiming) != 'closed') OR " +
           "(:day = 'TUESDAY' AND d.tuesdayTiming IS NOT NULL AND LOWER(d.tuesdayTiming) != 'closed') OR " +
           "(:day = 'WEDNESDAY' AND d.wednesdayTiming IS NOT NULL AND LOWER(d.wednesdayTiming) != 'closed') OR " +
           "(:day = 'THURSDAY' AND d.thursdayTiming IS NOT NULL AND LOWER(d.thursdayTiming) != 'closed') OR " +
           "(:day = 'FRIDAY' AND d.fridayTiming IS NOT NULL AND LOWER(d.fridayTiming) != 'closed') OR " +
           "(:day = 'SATURDAY' AND d.saturdayTiming IS NOT NULL AND LOWER(d.saturdayTiming) != 'closed') OR " +
           "(:day = 'SUNDAY' AND d.sundayTiming IS NOT NULL AND LOWER(d.sundayTiming) != 'closed')")
    List<Doctor> findAvailableOnDay(@Param("day") String day);
    
    /**
     * Find doctors by language spoken
     */
    @Query(value = "SELECT d.* FROM doctors d " +
                  "JOIN doctor_languages dl ON d.id = dl.doctor_id " +
                  "WHERE LOWER(dl.language) LIKE LOWER(CONCAT('%', :language, '%'))", 
           nativeQuery = true)
    List<Doctor> findByLanguage(@Param("language") String language);
    
    /**
     * Find doctors by gender
     */
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.gender) = LOWER(:gender)")
    List<Doctor> findByGender(@Param("gender") String gender);
    
    /**
     * Find doctors with specific expertise
     */
    @Query("SELECT d FROM Doctor d WHERE d.expertise IS NOT NULL AND " +
           "LOWER(d.expertise) LIKE LOWER(CONCAT('%', :expertise, '%'))")
    List<Doctor> findByExpertise(@Param("expertise") String expertise);
    
    /**
     * Find doctors offering specific services
     */
    @Query("SELECT d FROM Doctor d WHERE d.services IS NOT NULL AND " +
           "LOWER(d.services) LIKE LOWER(CONCAT('%', :service, '%'))")
    List<Doctor> findByService(@Param("service") String service);
    
    /**
     * Statistics queries
     */
    @Query("SELECT COUNT(d) FROM Doctor d")
    Long countTotalDoctors();
    
    @Query("SELECT COUNT(d) FROM Doctor d WHERE d.isAvailableForEmergency = true")
    Long countEmergencyAvailableDoctors();
    
    @Query("SELECT COUNT(d) FROM Doctor d WHERE d.onlineConsultation = true")
    Long countOnlineConsultationDoctors();
    
    @Query("SELECT d.specialization, COUNT(d) FROM Doctor d WHERE d.specialization IS NOT NULL " +
           "GROUP BY d.specialization ORDER BY COUNT(d) DESC")
    List<Object[]> getSpecializationDistribution();
    
    @Query("SELECT d.clinicCity, COUNT(d) FROM Doctor d WHERE d.clinicCity IS NOT NULL " +
           "GROUP BY d.clinicCity ORDER BY COUNT(d) DESC")
    List<Object[]> getCityDistribution();
    
    @Query("SELECT AVG(d.consultationFee) FROM Doctor d WHERE d.consultationFee IS NOT NULL")
    Double getAverageConsultationFee();
    
    @Query("SELECT AVG(d.averageRating) FROM Doctor d WHERE d.averageRating IS NOT NULL")
    Double getAverageRating();
    
    /**
     * Find top doctors by various criteria
     */
    @Query("SELECT d FROM Doctor d WHERE d.averageRating IS NOT NULL " +
           "ORDER BY d.averageRating DESC, d.yearsOfExperience DESC")
    List<Doctor> findTopDoctorsByRating(Pageable pageable);
    
    @Query("SELECT d FROM Doctor d WHERE d.yearsOfExperience IS NOT NULL " +
           "ORDER BY d.yearsOfExperience DESC, d.averageRating DESC")
    List<Doctor> findTopDoctorsByExperience(Pageable pageable);
    
    @Query("SELECT d FROM Doctor d WHERE d.specialization = :specialization " +
           "ORDER BY d.averageRating DESC, d.yearsOfExperience DESC")
    List<Doctor> findTopDoctorsBySpecialization(@Param("specialization") String specialization, Pageable pageable);
    
    /**
     * Find nearby doctors (this would typically use spatial queries, 
     * but for simplicity we're using city/state matching)
     */
    @Query("SELECT d FROM Doctor d WHERE " +
           "(d.clinicCity = :city OR d.clinicState = :state) " +
           "ORDER BY d.averageRating DESC")
    List<Doctor> findNearbyDoctors(@Param("city") String city, @Param("state") String state);
    
    /**
     * Custom queries for dashboard analytics
     */
    @Query("SELECT COUNT(d) FROM Doctor d WHERE d.user.enabled = true")
    Long countActiveDoctors();
    
    @Query("SELECT COUNT(d) FROM Doctor d WHERE d.user.enabled = false")
    Long countInactiveDoctors();
    
    @Query("SELECT d FROM Doctor d WHERE d.user.enabled = true " +
           "AND d.averageRating >= 4 " +
           "ORDER BY d.averageRating DESC, d.yearsOfExperience DESC")
    List<Doctor> findFeaturedDoctors(Pageable pageable);
    
    /**
     * Find doctors with complete profiles
     */
    @Query("SELECT d FROM Doctor d WHERE " +
           "d.specialization IS NOT NULL AND " +
           "d.education IS NOT NULL AND " +
           "d.licenseNumber IS NOT NULL AND " +
           "d.consultationFee IS NOT NULL AND " +
           "d.yearsOfExperience IS NOT NULL")
    List<Doctor> findDoctorsWithCompleteProfiles();
    
    /**
     * Find recently joined doctors
     */
    @Query("SELECT d FROM Doctor d WHERE d.createdAt >= :startDate ORDER BY d.createdAt DESC")
    List<Doctor> findRecentlyJoinedDoctors(@Param("startDate") java.time.LocalDateTime startDate);
    
    /**
     * Clinic-specific queries
     */
    @Query("SELECT d FROM Doctor d WHERE d.clinicName IS NOT NULL AND " +
           "LOWER(d.clinicName) LIKE LOWER(CONCAT('%', :clinicName, '%'))")
    List<Doctor> findByClinicName(@Param("clinicName") String clinicName);
    
    @Query("SELECT d FROM Doctor d WHERE d.clinicAddress IS NOT NULL AND " +
           "LOWER(d.clinicAddress) LIKE LOWER(CONCAT('%', :address, '%'))")
    List<Doctor> findByClinicAddress(@Param("address") String address);
    
    @Query("SELECT d FROM Doctor d WHERE d.clinicPincode = :pincode")
    List<Doctor> findByClinicPincode(@Param("pincode") String pincode);
    
    /**
     * Complex availability queries
     */
    @Query("SELECT d FROM Doctor d WHERE " +
           "d.onlineConsultation = true AND " +
           "d.isAvailableForEmergency = true AND " +
           "d.user.enabled = true")
    List<Doctor> findAvailableForEmergencyOnlineConsultation();
    
    /**
     * Search with text matching
     */
    @Query("SELECT d FROM Doctor d WHERE " +
           "LOWER(CONCAT(d.user.firstName, ' ', d.user.lastName)) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(d.hospitalAffiliation) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(d.clinicName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(d.clinicCity) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(d.about) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<Doctor> fullTextSearch(@Param("searchText") String searchText);
}