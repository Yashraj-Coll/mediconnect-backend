package com.mediconnect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.mediconnect.model.ERole;
import com.mediconnect.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(ERole name);
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Long countUsersByRole(@Param("roleName") ERole roleName);
    
    @Query("SELECT r FROM Role r WHERE EXISTS " +
           "(SELECT u FROM User u JOIN u.roles ur WHERE ur = r AND u.id = :userId)")
    List<Role> findRolesByUserId(@Param("userId") Long userId);
    
    @Query(value = "SELECT r.* FROM roles r " +
                  "LEFT JOIN user_roles ur ON r.id = ur.role_id " +
                  "GROUP BY r.id " +
                  "ORDER BY COUNT(ur.user_id) DESC", 
           nativeQuery = true)
    List<Role> findRolesByPopularity();
    
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Role r WHERE r.name = :roleName")
    boolean existsByName(@Param("roleName") ERole roleName);
}