package com.mediconnect.service.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpeechToTextService {
    @Autowired
    private OpenAiService openAiService;
    
    public String transcribeSpeech(byte[] audioData) {
        return openAiService.transcribeSpeech(audioData);
    }
}