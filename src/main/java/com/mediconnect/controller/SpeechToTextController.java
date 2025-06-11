package com.mediconnect.controller;

import com.mediconnect.service.ai.SpeechToTextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/mediconnect/api/ai")
public class SpeechToTextController {

    @Autowired
    private SpeechToTextService speechToTextService;

    // Maximum file size (25MB)
    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024;
    
    // Supported audio formats - WebM ADDED! ✅
    private static final Set<String> SUPPORTED_FORMATS = Set.of(
        "audio/wav", "audio/wave", "audio/x-wav",
        "audio/mp3", "audio/mpeg", "audio/mp4a-latm",
        "audio/mp4", "audio/m4a", "audio/x-m4a",
        "audio/flac", "audio/x-flac",
        "audio/ogg", "audio/ogg;codecs=opus",
        "audio/webm", "audio/webm;codecs=opus"  // ✅ WebM support added!
    );

    @PostMapping("/speech-to-text")
    public ResponseEntity<?> speechToText(@RequestParam("audio") MultipartFile audioFile) {
    	System.out.println("🔥 NEW CONTROLLER VERSION - WebM Enabled: " + new java.util.Date());
        try {
            // Basic validation
            if (audioFile == null || audioFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false, 
                    "message", "No audio file uploaded."
                ));
            }

            // File size validation
            if (audioFile.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false, 
                    "message", "File too large. Maximum size is 25MB, received: " + 
                              (audioFile.getSize() / (1024 * 1024)) + "MB"
                ));
            }

            // Content type validation with fallback
            String contentType = audioFile.getContentType();
            String filename = audioFile.getOriginalFilename();
            
            System.out.println("🎤 Received file: " + filename + " with content-type: " + contentType);
            
            boolean isValidFormat = false;
            
            // Check content type first
            if (contentType != null && SUPPORTED_FORMATS.contains(contentType.toLowerCase())) {
                isValidFormat = true;
            }
            // Fallback: check file extension
            else if (filename != null && isValidAudioExtension(filename)) {
                isValidFormat = true;
                System.out.println("✅ Accepted based on file extension: " + filename);
            }
            
            if (!isValidFormat) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false, 
                    "message", "Invalid audio format or file size. Please provide a valid audio file (WAV, MP3, M4A, FLAC, OGG, WebM) under 25MB."
                ));
            }

            // Process the audio file
            byte[] audioData = audioFile.getBytes();
            String text = speechToTextService.transcribeSpeech(audioData);
            
            System.out.println("📝 Transcription result: " + (text != null ? text.substring(0, Math.min(50, text.length())) : "null"));
            
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Audio transcribed successfully",
                "data", Map.of("text", text != null ? text : ""),
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false, 
                "message", "Speech-to-text error: " + e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * Check if file extension is valid audio format
     */
    private boolean isValidAudioExtension(String filename) {
        if (filename == null) return false;
        
        String extension = filename.toLowerCase();
        return extension.endsWith(".wav") || 
               extension.endsWith(".mp3") || 
               extension.endsWith(".m4a") || 
               extension.endsWith(".flac") || 
               extension.endsWith(".ogg") ||
               extension.endsWith(".webm");  // ✅ WebM extension support
    }
}