package com.mediconnect.controller;

import com.mediconnect.dto.MedicalRecordDTO;
import com.mediconnect.model.MedicalRecord;
import com.mediconnect.service.MedicalRecordService;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService service;

    public MedicalRecordController(MedicalRecordService service) {
        this.service = service;
    }

    @PostMapping("/upload")
    public ResponseEntity<MedicalRecord> upload(@RequestParam Long patientId,
                                                @RequestParam Long doctorId,
                                                @RequestParam String title,
                                                @RequestParam String type,
                                                @RequestParam(required = false) String hospital,
                                                @RequestParam(required = false) String notes,
                                                @RequestParam MultipartFile file) throws IOException {
        MedicalRecord saved = service.uploadRecord(patientId, doctorId, file, title, type, hospital, notes);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) throws IOException {
        Resource file = service.downloadRecord(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getFilename())
                .body(file);
    }

    // --- Updated endpoint: Returns List<MedicalRecordDTO> instead of entity ---
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecordDTO>> getRecordsByPatient(@PathVariable Long patientId) {
        List<MedicalRecordDTO> records = service.getRecordsByPatientId(patientId);
        return ResponseEntity.ok(records);
    }
}
