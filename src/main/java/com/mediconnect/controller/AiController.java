package com.mediconnect.controller;

import com.mediconnect.service.ai.*;
import com.mediconnect.dto.ApiResponse;
import com.mediconnect.dto.request.ChatbotMessageDTO;
import com.mediconnect.dto.response.ChatbotResponseDTO;
import com.mediconnect.dto.request.TranslationRequestDTO;
import com.mediconnect.dto.response.TranslationResponseDTO;
import com.mediconnect.util.LanguageDetectionUtil;
import com.mediconnect.model.User;
import com.mediconnect.service.ai.MedicalContextService;
import com.mediconnect.service.ai.DocumentProcessorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AiController {
    
    private static final Logger logger = LoggerFactory.getLogger(AiController.class);
    
    @Autowired
    private MedicalContextService medicalContextService;
    
    @Autowired
    private OpenAIChatbotService chatbotService;
    
    @Autowired
    private AiModelService aiModelService;
    
    @Autowired
    private SpeechToTextService speechToTextService;
    
    @Autowired
    private TranslationAiService translationService;
    
    @Autowired
    private MedicalCodingService medicalCodingService;
    
    @Autowired
    private MedicationInteractionAiService medicationInteractionService;
    
    @Autowired
    private TreatmentRecommendationAiService treatmentRecommendationService;
    
    @Autowired
    private HealthPredictorAiService healthPredictorService;
    
    @Autowired
    private DocumentProcessorService documentProcessorService;
    
    @Autowired
    private LanguageDetectionUtil languageDetectionUtil;

    // Hugging Face Configuration
    @Value("${huggingface.api.key}")
    private String huggingFaceApiKey;
    
    @Value("${huggingface.endpoints.chat}")
    private String chatModelUrl;
    
    @Value("${huggingface.endpoints.ocr}")
    private String ocrModelUrl;
    
    @Value("${huggingface.endpoints.translation}")
    private String translationModelUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ============ DEBUG ENDPOINT ============
    
    /**
     * Debug authentication and authorization
     */
    @GetMapping("/debug-auth")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> debugAuth(Authentication authentication) {
        Map<String, Object> debugInfo = new HashMap<>();
        
        if (authentication == null) {
            debugInfo.put("status", "No authentication");
            debugInfo.put("authenticated", false);
        } else {
            debugInfo.put("status", "Authentication found");
            debugInfo.put("principal", authentication.getPrincipal().toString());
            debugInfo.put("authorities", authentication.getAuthorities());
            debugInfo.put("name", authentication.getName());
            debugInfo.put("authenticated", authentication.isAuthenticated());
        }
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Debug info retrieved", debugInfo));
    }

    // ============ PROFILE & CONTEXT ENDPOINTS ============

    /**
     * Get user profile with gender, profile image, date of birth, etc.
     * Works for both patients and doctors - fetches from correct entity
     */
    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('ROLE_PATIENT') or hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserProfile() {
        try {
            User currentUser = medicalContextService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not authenticated", null));
            }
            
            Map<String, Object> profile = medicalContextService.getUserProfile();
            
            if (profile.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "User profile not found", null));
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Profile retrieved successfully", profile));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error retrieving profile: " + e.getMessage(), null));
        }
    }

    /**
     * Get medical context for AI (patient only)
     */
    @GetMapping("/medical-context")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMedicalContext() {
        try {
            User currentUser = medicalContextService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not authenticated", null));
            }
            
            List<Map<String, Object>> medicalContext = medicalContextService.getMedicalContext();
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Medical context retrieved successfully", medicalContext));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error retrieving medical context: " + e.getMessage(), null));
        }
    }

    /**
     * Get medical summary for AI prompts (patient only)
     */
    @GetMapping("/medical-summary")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMedicalSummary() {
        try {
            User currentUser = medicalContextService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not authenticated", null));
            }
            
            String medicalSummary = medicalContextService.getMedicalSummary();
            List<String> currentMedications = medicalContextService.getCurrentMedications();
            Map<String, List<String>> allergiesAndConditions = medicalContextService.getAllergiesAndConditions();
            
            Map<String, Object> summaryData = new HashMap<>();
            summaryData.put("summary", medicalSummary);
            summaryData.put("currentMedications", currentMedications);
            summaryData.put("allergies", allergiesAndConditions.get("allergies"));
            summaryData.put("conditions", allergiesAndConditions.get("conditions"));
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Medical summary retrieved successfully", summaryData));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error retrieving medical summary: " + e.getMessage(), null));
        }
    }

    /**
     * Get vital signs history (patient only)
     */
    @GetMapping("/vital-signs")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getVitalSigns() {
        try {
            User currentUser = medicalContextService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not authenticated", null));
            }
            
            List<Map<String, Object>> vitalSigns = medicalContextService.getVitalSignsHistory();
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Vital signs retrieved successfully", vitalSigns));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error retrieving vital signs: " + e.getMessage(), null));
        }
    }

    /**
     * Get user type (PATIENT or DOCTOR)
     */
    @GetMapping("/user-type")
    @PreAuthorize("hasAuthority('ROLE_PATIENT') or hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserType() {
        try {
            User currentUser = medicalContextService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not authenticated", null));
            }
            
            String userType = medicalContextService.determineUserType(currentUser);
            
            Map<String, Object> result = new HashMap<>();
            result.put("userType", userType);
            result.put("userId", currentUser.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "User type retrieved successfully", result));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error retrieving user type: " + e.getMessage(), null));
        }
    }

    // ============ CHATBOT & AI ENDPOINTS ============

    /**
     * AI Chat with medical context using Hugging Face Falcon-7B
     */
    @PostMapping("/chat")
    @PreAuthorize("hasAuthority('ROLE_PATIENT') or hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<ChatbotResponseDTO>> chat(@RequestBody ChatbotMessageDTO request) {
        try {
            // Step 1: Language get karo, default english
            String language = request.getLanguage();
            if (language == null || language.trim().isEmpty()) {
                language = "english";
            }

            // Step 2: Prompt mein language instruction daalo
            String enhancedPrompt =
                "Reply in " + language + " language only, using roman script if Hinglish. " +
                medicalContextService.enhancePromptWithContext(request.getMessage());

            // Step 3: AI se response lo
            String response = chatbotService.generateChatResponse(enhancedPrompt);

            // Step 4: ChatbotResponseDTO banao
            ChatbotResponseDTO chatResponse = new ChatbotResponseDTO();
            chatResponse.setResponse(response);
            chatResponse.setTimestamp(System.currentTimeMillis());
            chatResponse.setSuccess(true);

            return ResponseEntity.ok(new ApiResponse<>(true, "Chat response generated successfully", chatResponse));

        } catch (Exception e) {
            ChatbotResponseDTO errorResponse = new ChatbotResponseDTO();
            errorResponse.setResponse("I'm experiencing technical difficulties. Please try again later or consult with a healthcare professional directly.");
            errorResponse.setSuccess(false);
            errorResponse.setError(e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error generating chat response: " + e.getMessage(), errorResponse));
        }
    }


    // ============ HEALTH CHECK ENDPOINTS ============

    /**
     * Check if user has specific condition (patient only)
     */
    @GetMapping("/has-condition")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> hasCondition(@RequestParam String condition) {
        try {
            User currentUser = medicalContextService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not authenticated", null));
            }
            
            boolean hasCondition = medicalContextService.hasCondition(condition);
            
            Map<String, Object> result = new HashMap<>();
            result.put("condition", condition);
            result.put("hasCondition", hasCondition);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Condition check completed", result));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error checking condition: " + e.getMessage(), null));
        }
    }

    /**
     * Check if user has specific allergy (patient only)
     */
    @GetMapping("/has-allergy")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> hasAllergy(@RequestParam String allergy) {
        try {
            User currentUser = medicalContextService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not authenticated", null));
            }
            
            boolean hasAllergy = medicalContextService.hasAllergy(allergy);
            
            Map<String, Object> result = new HashMap<>();
            result.put("allergy", allergy);
            result.put("hasAllergy", hasAllergy);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Allergy check completed", result));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error checking allergy: " + e.getMessage(), null));
        }
    }

    // ============ IMAGE & DOCUMENT ANALYSIS ============

    /**
     * Analyze medical images using Hugging Face Donut model
     */
    @PostMapping("/analyze-image")
    @PreAuthorize("hasAuthority('ROLE_PATIENT') or hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "No file uploaded", null));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Only image files are supported", null));
            }

            byte[] imageBytes = file.getBytes();
            String analysis = aiModelService.analyzeImage(imageBytes);
            
            Map<String, Object> result = new HashMap<>();
            result.put("analysis", analysis);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Image analyzed successfully", result));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to analyze image: " + e.getMessage(), null));
        }
    }

    /**
     * Analyze medical documents using Hugging Face Donut OCR
     */
    @PostMapping("/analyze-document")
    @PreAuthorize("hasAuthority('ROLE_PATIENT') or hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeDocument(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "No file uploaded", null));
            }

            if (!documentProcessorService.isSupportedDocument(file)) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Unsupported document type. Please upload PDF, Word, or text files.", null));
            }

            String extractedText = documentProcessorService.extractTextFromDocument(file);
            
            if (extractedText.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "No text could be extracted from the document", null));
            }

            String documentSummary = documentProcessorService.analyzeDocumentContent(extractedText);
            
            String aiPrompt = "Please analyze this medical document and provide insights, possible conditions, recommendations, and next steps. Document content: " + extractedText;
            String analysis = aiModelService.diagnoseFromDescription(aiPrompt);
            
            Map<String, Object> result = new HashMap<>();
            result.put("text", extractedText);
            result.put("analysis", analysis);
            result.put("summary", documentSummary);
            result.put("documentType", documentProcessorService.getDocumentType(file));
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Document analyzed successfully", result));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to analyze document: " + e.getMessage(), null));
        }
    }

    // ============ SPEECH & TRANSLATION ============

    /**
     * Speech to text conversion using Hugging Face Whisper-Large-v3
     */
 // In AiController.java - Update the speech-to-text endpoint
    @PostMapping("/speech-to-text")
    @PreAuthorize("hasAuthority('ROLE_PATIENT') or hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> speechToText(
        @RequestParam("audio") MultipartFile audioFile) { // ✅ Changed from "audio" to match frontend
        
        try {
            if (audioFile.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "No audio file uploaded", null));
            }

            byte[] audioBytes = audioFile.getBytes();
            String transcription = speechToTextService.transcribeSpeech(audioBytes);
            
            Map<String, Object> result = new HashMap<>();
            result.put("text", transcription);
            result.put("transcription", transcription);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Audio transcribed successfully", result));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to transcribe audio: " + e.getMessage(), null));
        }
    }

    /**
     * Translate medical content using Hugging Face NLLB-200
     */
    @PostMapping("/translate")
    @PreAuthorize("hasAuthority('ROLE_PATIENT') or hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<TranslationResponseDTO>> translate(@RequestBody TranslationRequestDTO request) {
        try {
            String translatedText = translationService.translateMedicalContent(
                request.getText(),
                request.getSourceLanguage(),
                request.getTargetLanguage()
            );
            
            TranslationResponseDTO response = new TranslationResponseDTO();
            response.setTranslatedText(translatedText);
            response.setTranslation(translatedText);
            response.setSourceLanguage(request.getSourceLanguage());
            response.setTargetLanguage(request.getTargetLanguage());
            response.setSuccess(true);
            response.setTimestamp(System.currentTimeMillis());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Translation completed successfully", response));
            
        } catch (Exception e) {
            TranslationResponseDTO errorResponse = new TranslationResponseDTO();
            errorResponse.setSuccess(false);
            errorResponse.setError(e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Translation failed: " + e.getMessage(), errorResponse));
        }
    }

    /**
     * Smart translate with auto-detection using Hugging Face
     */
    @PostMapping("/smart-translate")
    @PreAuthorize("hasAuthority('ROLE_PATIENT') or hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<TranslationResponseDTO>> smartTranslate(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            String targetLanguage = request.get("targetLanguage");
            
            // Auto-detect source language
            String sourceLanguage = languageDetectionUtil.getMedicalContextLanguage(text);
            
            String translatedText = translationService.translateMedicalContent(text, sourceLanguage, targetLanguage);
            
            TranslationResponseDTO response = new TranslationResponseDTO();
            response.setTranslatedText(translatedText);
            response.setTranslation(translatedText);
            response.setSourceLanguage(sourceLanguage);
            response.setTargetLanguage(targetLanguage);
            response.setSuccess(true);
            response.setTimestamp(System.currentTimeMillis());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Smart translation completed successfully", response));
            
        } catch (Exception e) {
            TranslationResponseDTO errorResponse = new TranslationResponseDTO();
            errorResponse.setSuccess(false);
            errorResponse.setError(e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Smart translation failed: " + e.getMessage(), errorResponse));
        }
    }

    // ============ MEDICAL SERVICES (DOCTOR ONLY) ============

    /**
     * Generate medical coding (ICD-10, CPT codes) using Hugging Face BiomedNLP-PubMedBERT
     */
    @PostMapping("/medical-coding")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMedicalCoding(@RequestBody Map<String, String> request) {
        try {
            String clinicalText = request.get("text");
            if (clinicalText == null || clinicalText.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Clinical text is required", null));
            }

            String codingResult = medicalCodingService.generateMedicalCoding(clinicalText);
            
            Map<String, Object> result = new HashMap<>();
            result.put("codes", codingResult);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Medical codes generated successfully", result));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to generate medical codes: " + e.getMessage(), null));
        }
    }

    /**
     * Treatment recommendations using Hugging Face BioGPT-Large
     */
    @PostMapping("/treatment-recommendation")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTreatmentRecommendation(@RequestBody Map<String, String> request) {
        try {
            String diagnosis = request.get("diagnosis");
            String patientInfo = request.get("patientInfo");
            
            if (diagnosis == null || diagnosis.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Diagnosis is required", null));
            }

            String recommendation = treatmentRecommendationService.recommendTreatment(diagnosis, patientInfo);
            
            Map<String, Object> result = new HashMap<>();
            result.put("recommendation", recommendation);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Treatment recommendation generated successfully", result));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to generate treatment recommendation: " + e.getMessage(), null));
        }
    }

    // ============ MEDICATION & HEALTH PREDICTION ============

    /**
     * Check medication interactions using Hugging Face BiomedNLP-PubMedBERT
     */
    @PostMapping("/medication-interaction")
    @PreAuthorize("hasAuthority('ROLE_PATIENT') or hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkMedicationInteractions(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> medications = (List<String>) request.get("medications");
            
            if (medications == null || medications.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Medications list is required", null));
            }

            String interactionResult = medicationInteractionService.checkMedicationInteractions(medications);
            
            Map<String, Object> result = new HashMap<>();
            result.put("interactions", interactionResult);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Medication interactions checked successfully", result));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to check medication interactions: " + e.getMessage(), null));
        }
    }

    /**
     * Health risk prediction using Hugging Face DialoGPT-medium
     */
    @PostMapping("/health-prediction")
    @PreAuthorize("hasAuthority('ROLE_PATIENT') or hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> predictHealthRisks(@RequestBody Map<String, String> request) {
        try {
            String patientData = request.get("patientData");
            
            if (patientData == null || patientData.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Patient data is required", null));
            }

            String prediction = healthPredictorService.predictHealthRisks(patientData);
            
            Map<String, Object> result = new HashMap<>();
            result.put("prediction", prediction);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Health risks predicted successfully", result));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to predict health risks: " + e.getMessage(), null));
        }
    }

    // ============ LANGUAGE UTILITIES ============

    /**
     * Get supported languages for translation
     */
    @GetMapping("/supported-languages")
    @PreAuthorize("hasAuthority('ROLE_PATIENT') or hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getSupportedLanguages() {
        try {
            List<Map<String, String>> languages = getAvailableLanguagesSafely();
            return ResponseEntity.ok(new ApiResponse<>(true, "Supported languages retrieved successfully", languages));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve supported languages: " + e.getMessage(), new ArrayList<>()));
        }
    }

    /**
     * Detect language from text
     */
    @PostMapping("/detect-language")
    @PreAuthorize("hasAuthority('ROLE_PATIENT') or hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> detectLanguage(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Text is required", null));
            }

            String detectedLanguage = languageDetectionUtil.detectLanguage(text);
            String languageName = getLanguageNameSafely(detectedLanguage);
            
            // Get confidence scores
            Map<String, Double> confidence = languageDetectionUtil.getLanguageConfidence(text);
            
            Map<String, Object> result = new HashMap<>();
            result.put("language", detectedLanguage);
            result.put("languageName", languageName);
            result.put("confidence", confidence.get(detectedLanguage));
            result.put("isMultiLingual", languageDetectionUtil.isMultiLingual(text));
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Language detected successfully", result));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to detect language: " + e.getMessage(), null));
        }
    }

    /**
     * Get language suggestions for text
     */
    @GetMapping("/language-suggestions/{text}")
    @PreAuthorize("hasAuthority('ROLE_PATIENT') or hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLanguageSuggestions(@PathVariable String text) {
        try {
            List<String> suggestions = languageDetectionUtil.getSuggestedLanguages(text);
            Map<String, Double> confidence = languageDetectionUtil.getLanguageConfidence(text);
            
            Map<String, Object> result = new HashMap<>();
            result.put("suggestions", suggestions);
            result.put("confidence", confidence);
            result.put("recommended", languageDetectionUtil.getRecommendedLanguage(text, null));
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Language suggestions retrieved successfully", result));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to get language suggestions: " + e.getMessage(), null));
        }
    }

    /**
     * Test AI services using Hugging Face
     */
    @GetMapping("/test-ai")
    @PreAuthorize("hasAuthority('ROLE_PATIENT') or hasAuthority('ROLE_DOCTOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testAI() {
        try {
            String testResponse = chatbotService.generateChatResponse("Hello, can you help me with a simple health question?");
            
            Map<String, Object> result = new HashMap<>();
            result.put("response", testResponse);
            result.put("provider", "Hugging Face");
            result.put("model", "tiiuae/falcon-7b-instruct");
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "AI test completed", result));
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            errorResult.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "AI test failed: " + e.getMessage(), errorResult));
        }
    }
    
    
    

    // ============ PRIVATE HELPER METHODS ============

    /**
     * Safe method to get available languages
     */
    private List<Map<String, String>> getAvailableLanguagesSafely() {
        try {
            if (translationService != null) {
                return translationService.getSupportedLanguages();
            }
        } catch (Exception e) {
            logger.error("Error getting supported languages: {}", e.getMessage());
        }
        
        // Fallback list of supported languages
        List<Map<String, String>> fallbackLanguages = new ArrayList<>();
        String[] languages = {"english", "hindi", "bengali", "tamil", "telugu", "marathi", "gujarati", "kannada", "malayalam"};
        
        for (String lang : languages) {
            Map<String, String> langMap = new HashMap<>();
            langMap.put("code", lang);
            langMap.put("name", capitalizeFirstLetter(lang));
            fallbackLanguages.add(langMap);
        }
        
        return fallbackLanguages;
    }

    /**
     * Safe method to get language name
     */
    private String getLanguageNameSafely(String languageCode) {
        try {
            if (translationService != null) {
                return translationService.getLanguageName(languageCode);
            }
        } catch (Exception e) {
            logger.error("Error getting language name: {}", e.getMessage());
        }
        
        return capitalizeFirstLetter(languageCode);
    }

    /**
     * Helper method to capitalize first letter
     */
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    // ============ DIRECT HUGGING FACE API HELPER METHODS ============

    /**
     * Direct Hugging Face chat API call
     */
    private String callHuggingFaceChat(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(huggingFaceApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", prompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", 500,
                "temperature", 0.7,
                "do_sample", true,
                "return_full_text", false
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(chatModelUrl, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                    return jsonResponse.get(0).get("generated_text").asText().replace(prompt, "").trim();
                }
            }
            
            throw new RuntimeException("Chat API call failed");
            
        } catch (Exception e) {
            logger.error("Error calling Hugging Face chat API: {}", e.getMessage());
            return "I'm experiencing technical difficulties. Please try again or consult with a healthcare professional.";
        }
    }

    /**
     * Direct Hugging Face OCR API call
     */
    private String callHuggingFaceOCR(byte[] imageBytes) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(huggingFaceApiKey);

            String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", Map.of(
                "image", "data:image/jpeg;base64," + base64Image,
                "question", "Analyze this medical document and extract all visible text, medical information, and key findings."
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(ocrModelUrl, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                
                if (jsonResponse.has("answer")) {
                    return jsonResponse.get("answer").asText();
                } else if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                    JsonNode firstResult = jsonResponse.get(0);
                    if (firstResult.has("answer")) {
                        return firstResult.get("answer").asText();
                    }
                }
            }
            
            return "Document analysis completed, but no clear text could be extracted. Please ensure the image is clear and contains readable text.";
            
        } catch (Exception e) {
            logger.error("Error calling Hugging Face OCR API: {}", e.getMessage());
            return "Document analysis failed. Please try with a clearer image or different document format.";
        }
    }

    /**
     * Direct Hugging Face translation API call
     */
    private String callHuggingFaceTranslation(String text, String sourceLanguage, String targetLanguage) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(huggingFaceApiKey);

            String translationPrompt = String.format(
                "Translate the following text from %s to %s. Provide only the translation:\n\n%s\n\nTranslation:",
                sourceLanguage != null ? sourceLanguage : "auto-detected language",
                targetLanguage,
                text
            );

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", translationPrompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", 300,
                "temperature", 0.3,
                "do_sample", false,
                "return_full_text", false
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(translationModelUrl, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                    String translatedText = jsonResponse.get(0).get("generated_text").asText();
                    return translatedText.replace(translationPrompt, "").trim();
                }
            }
            
            throw new RuntimeException("Translation API call failed");
            
        } catch (Exception e) {
            logger.error("Error calling Hugging Face translation API: {}", e.getMessage());
            return "Translation failed. Please try again or use a different translation service.";
        }
    }

    /**
     * Health check for Hugging Face services
     */
    @GetMapping("/health-check")
    @PreAuthorize("hasAuthority('ROLE_DOCTOR') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        try {
            Map<String, Object> healthStatus = new HashMap<>();
            
            // Test chatbot service
            try {
                String testChat = chatbotService.generateChatResponse("Test");
                healthStatus.put("chatService", Map.of("status", "UP", "test", "PASSED"));
            } catch (Exception e) {
                healthStatus.put("chatService", Map.of("status", "DOWN", "error", e.getMessage()));
            }
            
            // Test translation service
            try {
                String testTranslation = translationService.translateMedicalContent("Hello", "english", "hindi");
                healthStatus.put("translationService", Map.of("status", "UP", "test", "PASSED"));
            } catch (Exception e) {
                healthStatus.put("translationService", Map.of("status", "DOWN", "error", e.getMessage()));
            }
            
            // Test speech service
            try {
                Map<String, Object> speechHealth = speechToTextService.getServiceHealth();
                healthStatus.put("speechService", speechHealth);
            } catch (Exception e) {
                healthStatus.put("speechService", Map.of("status", "DOWN", "error", e.getMessage()));
            }
            
            // Overall status
            healthStatus.put("provider", "Hugging Face");
            healthStatus.put("timestamp", System.currentTimeMillis());
            healthStatus.put("overallStatus", "UP");
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Health check completed", healthStatus));
            
        } catch (Exception e) {
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("overallStatus", "DOWN");
            errorStatus.put("error", e.getMessage());
            errorStatus.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Health check failed: " + e.getMessage(), errorStatus));
        }
    }
}