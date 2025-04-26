package com.mediconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mediconnect.dto.VoiceTranscriptionDTO;
import com.mediconnect.model.MedicalRecord;
import com.mediconnect.service.VoiceTranscriptionService;

@RestController
@RequestMapping("/api/voice-transcription")
public class VoiceTranscriptionController {

    @Autowired
    private VoiceTranscriptionService transcriptionService;
    
    /**
     * Transcribe audio file to text
     */
    @PostMapping("/transcribe")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<VoiceTranscriptionDTO> transcribeAudio(
            @RequestParam("audio") MultipartFile audioFile) {
        
        VoiceTranscriptionDTO result = transcriptionService.transcribeAudio(audioFile);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    /**
     * Apply transcription to medical record
     */
    @PostMapping("/apply/{medicalRecordId}")
    @PreAuthorize("hasRole('DOCTOR') or @securityService.canAccessMedicalRecord(#medicalRecordId)")
    public ResponseEntity<MedicalRecord> applyTranscription(
            @PathVariable Long medicalRecordId,
            @RequestBody VoiceTranscriptionDTO transcription) {
        
        MedicalRecord updatedRecord = transcriptionService.applyTranscriptionToMedicalRecord(
                medicalRecordId, transcription);
        
        return new ResponseEntity<>(updatedRecord, HttpStatus.OK);
    }
}