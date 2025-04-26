package com.mediconnect.controller;

import com.mediconnect.service.ai.SpeechToTextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/transcribe")
public class SpeechToTextController {
    @Autowired
    private SpeechToTextService speechToTextService;
    
    @PostMapping
    public ResponseEntity<?> transcribeSpeech(@RequestParam("file") MultipartFile file) {
        try {
            String transcript = speechToTextService.transcribeSpeech(file.getBytes());
            return ResponseEntity.ok(Map.of("transcript", transcript));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}