package com.mediconnect.controller;

import com.mediconnect.service.ai.AiModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/diagnose")
public class DiagnosisController {
    @Autowired
    private AiModelService aiModelService;
    
    @PostMapping
    public ResponseEntity<?> getDiagnosis(@RequestBody Map<String, String> request) {
        try {
            String symptoms = request.get("symptoms");
            String diagnosis = aiModelService.diagnoseFromDescription(symptoms);
            return ResponseEntity.ok(Map.of("diagnosis", diagnosis));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}