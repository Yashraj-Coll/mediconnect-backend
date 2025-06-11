package com.mediconnect.service;

import com.mediconnect.dto.VitalSignDTO;
import com.mediconnect.model.VitalSign;
import com.mediconnect.model.VitalSign.Status;
import com.mediconnect.repository.VitalSignRepository;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Date;

@Service
public class VitalSignService {

    @Autowired
    private VitalSignRepository vitalSignRepository;
    
    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    // Convert DTO to Entity - FIXED VERSION
    private VitalSign toEntity(VitalSignDTO dto) {
        VitalSign entity = new VitalSign();
        entity.setId(dto.getId());
        
        // Set patient entity instead of patientId
        if (dto.getPatientId() != null) {
            entity.setPatient(patientRepository.findById(dto.getPatientId()).orElse(null));
        }
        
        // Set doctor entity if provided
        if (dto.getDoctorId() != null) {
            entity.setDoctor(doctorRepository.findById(dto.getDoctorId()).orElse(null));
        }
        
        entity.setVitalType(dto.getVitalType());
        entity.setValue(dto.getValue());
        entity.setReadingDate(dto.getReadingDate());
        if (dto.getStatus() != null) {
            entity.setStatus(Status.valueOf(dto.getStatus().toLowerCase()));
        }
        entity.setNotes(dto.getNotes());
        return entity;
    }

    // Add new - SIMPLIFIED VERSION
    public VitalSign addVitalSign(VitalSignDTO dto) {
        VitalSign entity = toEntity(dto);
        return vitalSignRepository.save(entity);
    }

    // Update existing - FIXED VERSION
    public VitalSign updateVitalSign(Long id, VitalSignDTO dto) {
        Optional<VitalSign> opt = vitalSignRepository.findById(id);
        if (opt.isEmpty()) throw new RuntimeException("VitalSign not found");
        VitalSign entity = opt.get();

        // Update patient if provided
        if (dto.getPatientId() != null) {
            entity.setPatient(patientRepository.findById(dto.getPatientId()).orElse(null));
        }
        
        // Update doctor if provided
        if (dto.getDoctorId() != null) {
            entity.setDoctor(doctorRepository.findById(dto.getDoctorId()).orElse(null));
        }

        entity.setVitalType(dto.getVitalType());
        entity.setValue(dto.getValue());
        entity.setReadingDate(dto.getReadingDate());
        if (dto.getStatus() != null) {
            entity.setStatus(Status.valueOf(dto.getStatus().toLowerCase()));
        }
        entity.setNotes(dto.getNotes());

        return vitalSignRepository.save(entity);
    }

    // Delete
    public void deleteVitalSign(Long id) {
        vitalSignRepository.deleteById(id);
    }

    // Get all by patientId - NEEDS TO BE UPDATED FOR NEW ENTITY STRUCTURE
    public List<VitalSign> getVitalSignsByPatientId(Long patientId) {
        return vitalSignRepository.findByPatientIdOrderByReadingDateDesc(patientId);
    }

    // Get by patientId and vitalType - NEEDS TO BE UPDATED FOR NEW ENTITY STRUCTURE
    public List<VitalSign> getVitalSignsByPatientIdAndType(Long patientId, String vitalType) {
        return vitalSignRepository.findByPatientIdAndVitalTypeOrderByReadingDateDesc(patientId, vitalType);
    }

    // Get by patientId and date range - NEEDS TO BE UPDATED FOR NEW ENTITY STRUCTURE
    public List<VitalSign> getVitalSignsByPatientIdAndDateRange(Long patientId, Date startDate, Date endDate) {
        return vitalSignRepository.findByPatientIdAndReadingDateBetweenOrderByReadingDateDesc(patientId, startDate, endDate);
    }

    // Get latest vital signs for patient
    public List<VitalSign> getLatestVitalSignsByPatientId(Long patientId, Integer limit) {
        List<VitalSign> latest = vitalSignRepository.findLatestByPatientId(patientId);
        if (limit != null && latest.size() > limit) {
            return latest.subList(0, limit);
        }
        return latest;
    }

    // Get out-of-range vital signs for patient (status != normal) - NEEDS TO BE UPDATED
    public List<VitalSign> getOutOfRangeVitalSigns(Long patientId) {
        return vitalSignRepository.findByPatientIdAndStatusNot(patientId, Status.normal);
    }

    // Analytics/history for type+timeframe - NEEDS TO BE UPDATED
    public List<VitalSign> getVitalSignHistoryForAnalytics(Long patientId, String vitalType, String timeFrame) {
        return vitalSignRepository.findByPatientIdAndVitalTypeOrderByReadingDateDesc(patientId, vitalType);
    }
}