package com.mediconnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.ERole;
import com.mediconnect.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);

    Boolean existsByEmail(String email);
    Boolean existsByPhoneNumber(String phoneNumber);

    List<User> findByRolesName(ERole roleName);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findUsersByRole(@Param("roleName") ERole roleName);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchUsers(@Param("keyword") String keyword);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE " +
           "r.name = :roleName AND u.enabled = true")
    List<User> findActiveUsersByRole(@Param("roleName") ERole roleName);

    @Query("SELECT u FROM User u WHERE " +
           "u.enabled = :status")
    List<User> findByStatus(@Param("status") boolean status);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName ORDER BY u.createdAt DESC")
    List<User> findRecentUsersByRole(@Param("roleName") ERole roleName, Pageable pageable);
    
    // ADD THESE NEW METHODS FOR ROLE FETCHING:
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);
    
    /**
     * Check if user exists by email or phone number
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email OR u.phoneNumber = :phoneNumber")
    boolean existsByEmailOrPhoneNumber(@Param("email") String email, @Param("phoneNumber") String phoneNumber);
}