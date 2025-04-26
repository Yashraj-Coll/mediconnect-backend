package com.mediconnect.controller;

import com.mediconnect.service.ai.HealthPredictorAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/health-predictor")
public class HealthPredictorController {
    @Autowired
    private HealthPredictorAiService healthPredictorService;
    
    @PostMapping
    public ResponseEntity<?> predictHealthRisks(@RequestBody Map<String, String> request) {
        try {
            String patientData = request.get("patientData");
            String prediction = healthPredictorService.predictHealthRisks(patientData);
            return ResponseEntity.ok(Map.of("prediction", prediction));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}