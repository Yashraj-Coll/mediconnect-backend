package com.mediconnect.controller;

import com.mediconnect.service.ai.TriageServiceAi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/triage")
public class TriageController {
    @Autowired
    private TriageServiceAi triageService;
    
    @PostMapping
    public ResponseEntity<?> performTriage(@RequestBody Map<String, String> request) {
        try {
            String symptoms = request.get("symptoms");
            String assessment = triageService.performTriage(symptoms);
            return ResponseEntity.ok(Map.of("assessment", assessment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}