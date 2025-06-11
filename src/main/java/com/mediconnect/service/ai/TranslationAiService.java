package com.mediconnect.service.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class TranslationAiService {

    private static final Logger logger = LoggerFactory.getLogger(TranslationAiService.class);

    // Hugging Face Configuration (CHANGED from OpenAI)
    @Value("${huggingface.endpoints.translation}")
    private String translationApiUrl;

    @Value("${huggingface.api.key}")
    private String translationApiKey;

    @Value("${huggingface.translation.max-tokens:300}")
    private int maxTokens;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Supported languages map (KEPT YOUR EXISTING STRUCTURE)
    private static final Map<String, String> LANGUAGE_NAMES = new HashMap<>();
    private static final Map<String, String> LANGUAGE_CODES = new HashMap<>();
    
    static {
        // Language display names (YOUR EXISTING MAPPING)
        LANGUAGE_NAMES.put("en", "English");
        LANGUAGE_NAMES.put("hi", "हिंदी (Hindi)");
        LANGUAGE_NAMES.put("hinglish", "Hinglish");
        LANGUAGE_NAMES.put("bn", "বাংলা (Bengali)");
        LANGUAGE_NAMES.put("te", "తెలుగు (Telugu)");
        LANGUAGE_NAMES.put("ta", "தமிழ் (Tamil)");
        LANGUAGE_NAMES.put("mr", "मराठी (Marathi)");
        LANGUAGE_NAMES.put("gu", "ગુજરાતી (Gujarati)");
        LANGUAGE_NAMES.put("kn", "ಕನ್ನಡ (Kannada)");
        LANGUAGE_NAMES.put("ml", "മലയാളം (Malayalam)");
        LANGUAGE_NAMES.put("or", "ଓଡ଼ିଆ (Odia)");
        LANGUAGE_NAMES.put("pa", "ਪੰਜਾਬੀ (Punjabi)");
        LANGUAGE_NAMES.put("as", "অসমীয়া (Assamese)");
        
        // Language codes for API (YOUR EXISTING MAPPING)
        LANGUAGE_CODES.put("en", "English");
        LANGUAGE_CODES.put("hi", "Hindi");
        LANGUAGE_CODES.put("hinglish", "Hinglish (Hindi-English mix)");
        LANGUAGE_CODES.put("bn", "Bengali");
        LANGUAGE_CODES.put("te", "Telugu");
        LANGUAGE_CODES.put("ta", "Tamil");
        LANGUAGE_CODES.put("mr", "Marathi");
        LANGUAGE_CODES.put("gu", "Gujarati");
        LANGUAGE_CODES.put("kn", "Kannada");
        LANGUAGE_CODES.put("ml", "Malayalam");
        LANGUAGE_CODES.put("or", "Odia");
        LANGUAGE_CODES.put("pa", "Punjabi");
        LANGUAGE_CODES.put("as", "Assamese");
    }

    // YOUR MAIN METHOD - UPDATED TO USE HUGGING FACE
    public String translateMedicalContent(String text, String sourceLanguage, String targetLanguage) {
        try {
            // Handle Hinglish specially (YOUR EXISTING LOGIC)
            if ("hinglish".equals(targetLanguage.toLowerCase())) {
                return translateToHinglish(text, sourceLanguage);
            }
            
            if ("hinglish".equals(sourceLanguage.toLowerCase())) {
                return translateFromHinglish(text, targetLanguage);
            }

            // For other languages, use Hugging Face translation (CHANGED FROM OPENAI)
            return translateWithHuggingFace(text, sourceLanguage, targetLanguage);
            
        } catch (Exception e) {
            logger.error("Translation error: {}", e.getMessage());
            // Fallback translation (YOUR EXISTING LOGIC)
            return getFallbackTranslation(text, sourceLanguage, targetLanguage);
        }
    }

    // YOUR EXISTING METHOD - UPDATED TO USE HUGGING FACE
    private String translateToHinglish(String text, String sourceLanguage) {
        String prompt = String.format(
            "Translate the following %s text to Hinglish (a mix of Hindi and English commonly used in India). " +
            "Keep medical terms in English but use Hindi words for common expressions. " +
            "Make it natural and conversational like how Indians speak:\n\n%s",
            LANGUAGE_CODES.getOrDefault(sourceLanguage, sourceLanguage), text
        );
        
        return callHuggingFaceForTranslation(prompt);
    }

    // YOUR EXISTING METHOD - UPDATED TO USE HUGGING FACE
    private String translateFromHinglish(String text, String targetLanguage) {
        String prompt = String.format(
            "Translate the following Hinglish text (Hindi-English mix) to %s. " +
            "Maintain the medical context and meaning:\n\n%s",
            LANGUAGE_CODES.getOrDefault(targetLanguage, targetLanguage), text
        );
        
        return callHuggingFaceForTranslation(prompt);
    }

    // NEW METHOD - HUGGING FACE TRANSLATION (REPLACES translateWithOpenAI)
    private String translateWithHuggingFace(String text, String sourceLanguage, String targetLanguage) {
        String sourceLang = LANGUAGE_CODES.getOrDefault(sourceLanguage, sourceLanguage);
        String targetLang = LANGUAGE_CODES.getOrDefault(targetLanguage, targetLanguage);
        
        String prompt = String.format(
            "Translate the following medical/health text from %s to %s. " +
            "Maintain medical accuracy and context. Keep technical medical terms clear:\n\n%s",
            sourceLang, targetLang, text
        );
        
        return callHuggingFaceForTranslation(prompt);
    }

    // NEW METHOD - HUGGING FACE API CALL (REPLACES callOpenAIForTranslation)
    private String callHuggingFaceForTranslation(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(translationApiKey);
            headers.set("User-Agent", "MediConnect-Translation/1.0");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", prompt);
            requestBody.put("parameters", Map.of(
                "max_new_tokens", maxTokens,
                "temperature", 0.3,
                "do_sample", false,
                "return_full_text", false
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(translationApiUrl, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                
                if (jsonResponse.isArray() && jsonResponse.size() > 0) {
                    JsonNode firstResult = jsonResponse.get(0);
                    String generatedText = firstResult.get("generated_text").asText();
                    
                    // Clean the response by removing the original prompt
                    String translation = generatedText.replace(prompt, "").trim();
                    
                    if (translation.isEmpty()) {
                        throw new RuntimeException("Empty translation received");
                    }
                    
                    return translation;
                }
            }

            throw new RuntimeException("Failed to get translation from Hugging Face");

        } catch (Exception e) {
            logger.error("Hugging Face translation error: {}", e.getMessage());
            throw new RuntimeException("Translation service error: " + e.getMessage());
        }
    }

    // YOUR EXISTING METHOD - NO CHANGES
    private String getFallbackTranslation(String text, String sourceLanguage, String targetLanguage) {
        // Basic fallback translations for common medical phrases
        Map<String, Map<String, String>> fallbackMap = getFallbackTranslations();
        
        String lowerText = text.toLowerCase().trim();
        
        if (fallbackMap.containsKey(lowerText)) {
            Map<String, String> translations = fallbackMap.get(lowerText);
            return translations.getOrDefault(targetLanguage, text);
        }
        
        // If no fallback available, return original with note
        return text + " (Translation unavailable - " + LANGUAGE_NAMES.getOrDefault(targetLanguage, targetLanguage) + ")";
    }

    // YOUR EXISTING METHOD - NO CHANGES
    private Map<String, Map<String, String>> getFallbackTranslations() {
        Map<String, Map<String, String>> fallbacks = new HashMap<>();
        
        // Common medical phrases with translations
        Map<String, String> headache = new HashMap<>();
        headache.put("hi", "सिर दर्द");
        headache.put("hinglish", "Sir mein dard");
        headache.put("bn", "মাথা ব্যথা");
        fallbacks.put("headache", headache);
        fallbacks.put("head pain", headache);
        
        Map<String, String> fever = new HashMap<>();
        fever.put("hi", "बुखार");
        fever.put("hinglish", "Bukhar");
        fever.put("bn", "জ্বর");
        fallbacks.put("fever", fever);
        
        Map<String, String> cough = new HashMap<>();
        cough.put("hi", "खांसी");
        cough.put("hinglish", "Khansi");
        cough.put("bn", "কাশি");
        fallbacks.put("cough", cough);
        
        Map<String, String> cold = new HashMap<>();
        cold.put("hi", "सर्दी");
        cold.put("hinglish", "Sardi");
        cold.put("bn", "ঠান্ডা লাগা");
        fallbacks.put("cold", cold);
        
        Map<String, String> stomachPain = new HashMap<>();
        stomachPain.put("hi", "पेट दर्द");
        stomachPain.put("hinglish", "Pet mein dard");
        stomachPain.put("bn", "পেট ব্যথা");
        fallbacks.put("stomach pain", stomachPain);
        fallbacks.put("abdominal pain", stomachPain);
        
        Map<String, String> doctor = new HashMap<>();
        doctor.put("hi", "डॉक्टर");
        doctor.put("hinglish", "Doctor");
        doctor.put("bn", "ডাক্তার");
        fallbacks.put("doctor", doctor);
        
        Map<String, String> medicine = new HashMap<>();
        medicine.put("hi", "दवा");
        medicine.put("hinglish", "Dawa");
        medicine.put("bn", "ওষুধ");
        fallbacks.put("medicine", medicine);
        fallbacks.put("medication", medicine);
        
        Map<String, String> hospital = new HashMap<>();
        hospital.put("hi", "अस्पताल");
        hospital.put("hinglish", "Hospital");
        hospital.put("bn", "হাসপাতাল");
        fallbacks.put("hospital", hospital);
        
        return fallbacks;
    }

    // YOUR EXISTING METHOD - NO CHANGES
    public List<Map<String, String>> getSupportedLanguages() {
        List<Map<String, String>> languages = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : LANGUAGE_NAMES.entrySet()) {
            Map<String, String> lang = new HashMap<>();
            lang.put("code", entry.getKey());
            lang.put("name", entry.getValue());
            languages.add(lang);
        }
        
        return languages;
    }

    // YOUR EXISTING METHOD - NO CHANGES
    public String getLanguageName(String code) {
        return LANGUAGE_NAMES.getOrDefault(code, code);
    }

    // YOUR EXISTING METHOD - NO CHANGES
    public boolean isLanguageSupported(String languageCode) {
        return LANGUAGE_CODES.containsKey(languageCode);
    }

    // YOUR EXISTING METHOD - NO CHANGES
    public String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "en";
        }
        
        String lowerText = text.toLowerCase();
        
        // Check for Hinglish patterns (English + Hindi words/transliteration)
        String[] hinglishWords = {"hai", "hain", "kya", "aap", "mein", "kar", "keh", "bol", "dekh", "sun", "jana", "karna", "hona", "tha", "thi", "the"};
        String[] englishWords = {"is", "are", "the", "and", "you", "me", "can", "will", "have", "this", "that"};
        
        int hinglishCount = 0;
        int englishCount = 0;
        
        for (String word : hinglishWords) {
            if (lowerText.contains(" " + word + " ") || lowerText.startsWith(word + " ") || lowerText.endsWith(" " + word)) {
                hinglishCount++;
            }
        }
        
        for (String word : englishWords) {
            if (lowerText.contains(" " + word + " ") || lowerText.startsWith(word + " ") || lowerText.endsWith(" " + word)) {
                englishCount++;
            }
        }
        
        // If both Hindi transliteration and English words are present, it's likely Hinglish
        if (hinglishCount > 0 && englishCount > 0) {
            return "hinglish";
        }
        
        // Check for Devanagari script (Hindi)
        if (text.matches(".*[\\u0900-\\u097F].*")) {
            return "hi";
        }
        
        // Check for Bengali script
        if (text.matches(".*[\\u0980-\\u09FF].*")) {
            return "bn";
        }
        
        // Default to English
        return "en";
    }

    // ADDITIONAL METHODS FOR CONTROLLER COMPATIBILITY
    
    /**
     * Simple translate method for basic translation calls
     */
    public String translateText(String text, String sourceLanguage, String targetLanguage) {
        return translateMedicalContent(text, sourceLanguage, targetLanguage);
    }

    /**
     * Get available languages as simple list
     */
    public List<String> getAvailableLanguages() {
        return new ArrayList<>(LANGUAGE_CODES.keySet());
    }

    /**
     * Auto-detect and translate
     */
    public String autoTranslate(String text, String targetLanguage) {
        String detectedLanguage = detectLanguage(text);
        return translateMedicalContent(text, detectedLanguage, targetLanguage);
    }
}