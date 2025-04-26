package com.mediconnect.controller;

import com.mediconnect.service.ai.GenomicDataAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/genomic")
public class GenomicDataController {
    @Autowired
    private GenomicDataAiService genomicDataService;
    
    @PostMapping
    public ResponseEntity<?> analyzeGenomicData(@RequestBody Map<String, String> request) {
        try {
            String genomicData = request.get("data");
            String analysis = genomicDataService.analyzeGenomicData(genomicData);
            return ResponseEntity.ok(Map.of("analysis", analysis));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}