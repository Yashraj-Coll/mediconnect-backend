package com.mediconnect.controller;

import com.mediconnect.service.ai.TranslationAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/translation")
public class TranslationController {
    @Autowired
    private TranslationAiService translationService;
    
    @PostMapping
    public ResponseEntity<?> translateText(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            String sourceLanguage = request.get("sourceLanguage");
            String targetLanguage = request.get("targetLanguage");
            String translation = translationService.translateMedicalContent(text, sourceLanguage, targetLanguage);
            return ResponseEntity.ok(Map.of("translation", translation));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}