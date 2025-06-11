package com.mediconnect.service;

import com.mediconnect.dto.MedicalRecordDTO;
import com.mediconnect.model.MedicalRecord;
import com.mediconnect.model.Patient;
import com.mediconnect.model.Doctor;
import com.mediconnect.repository.MedicalRecordRepository;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.repository.DoctorRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MedicalRecordService {

    @Value("${app.upload.medical-records-path}")
    private String uploadDir;

    private final MedicalRecordRepository repository;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;

    public MedicalRecordService(MedicalRecordRepository repository,
                                PatientRepository patientRepo,
                                DoctorRepository doctorRepo) {
        this.repository = repository;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
    }

    public MedicalRecord uploadRecord(Long patientId, Long doctorId, MultipartFile file, String title, String type, String hospital, String notes) throws IOException {
        Patient patient = patientRepo.findById(patientId).orElseThrow();
        Doctor doctor = doctorRepo.findById(doctorId).orElseThrow();

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, filename);
        Files.createDirectories(filePath.getParent());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setDoctor(doctor);
        record.setDocumentPath("/files/medical-records/" + filename);
        record.setRecordDate(LocalDate.now());
        record.setHospital(hospital);
        record.setNotes(notes);
        record.setTitle(title);
        record.setType(type);

        return repository.save(record);
    }

    public Resource downloadRecord(String filename) throws IOException {
        Path path = Paths.get(uploadDir).resolve(filename);
        return new UrlResource(path.toUri());
    }

    // --- NEW: Return DTO list instead of entity list ---
    public List<MedicalRecordDTO> getRecordsByPatientId(Long patientId) {
        return repository.findByPatientIdOrderByRecordDateDesc(patientId)
                .stream()
                .map(MedicalRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
