package com.mediconnect.controller;

import com.mediconnect.service.ai.MedicalCodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/medical-coding")
public class MedicalCodingController {
    @Autowired
    private MedicalCodingService medicalCodingService;
    
    @PostMapping
    public ResponseEntity<?> getMedicalCoding(@RequestBody Map<String, String> request) {
        try {
            String clinicalText = request.get("text");
            String coding = medicalCodingService.generateMedicalCoding(clinicalText);
            return ResponseEntity.ok(Map.of("coding", coding));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}