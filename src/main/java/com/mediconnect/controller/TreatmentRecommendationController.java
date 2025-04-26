package com.mediconnect.controller;

import com.mediconnect.service.ai.TreatmentRecommendationAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/treatment")
public class TreatmentRecommendationController {
    @Autowired
    private TreatmentRecommendationAiService treatmentService;
    
    @PostMapping
    public ResponseEntity<?> recommendTreatment(@RequestBody Map<String, String> request) {
        try {
            String diagnosis = request.get("diagnosis");
            String patientInfo = request.get("patientInfo");
            String recommendation = treatmentService.recommendTreatment(diagnosis, patientInfo);
            return ResponseEntity.ok(Map.of("recommendation", recommendation));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}