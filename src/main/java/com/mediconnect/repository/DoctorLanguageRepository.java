package com.mediconnect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediconnect.model.DoctorLanguage;

@Repository
public interface DoctorLanguageRepository extends JpaRepository<DoctorLanguage, Long> {
    
    List<DoctorLanguage> findByDoctorId(Long doctorId);
    
    List<DoctorLanguage> findByLanguageIgnoreCase(String language);
    
    void deleteByDoctorId(Long doctorId);
}