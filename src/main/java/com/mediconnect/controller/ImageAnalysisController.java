package com.mediconnect.controller;

import com.mediconnect.service.ai.AiModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/analyze-image")
public class ImageAnalysisController {
    @Autowired
    private AiModelService aiModelService;
    
    @PostMapping
    public ResponseEntity<?> analyzeImage(@RequestParam("file") MultipartFile file) {
        try {
            String analysis = aiModelService.analyzeImage(file.getBytes());
            return ResponseEntity.ok(Map.of("analysis", analysis));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}