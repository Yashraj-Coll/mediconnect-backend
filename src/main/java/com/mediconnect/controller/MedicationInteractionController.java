package com.mediconnect.controller;

import com.mediconnect.service.ai.MedicationInteractionAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medication-interaction")
public class MedicationInteractionController {
    @Autowired
    private MedicationInteractionAiService medicationInteractionService;
    
    @PostMapping
    public ResponseEntity<?> checkInteractions(@RequestBody Map<String, List<String>> request) {
        try {
            List<String> medications = request.get("medications");
            String analysis = medicationInteractionService.checkMedicationInteractions(medications);
            return ResponseEntity.ok(Map.of("analysis", analysis));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}