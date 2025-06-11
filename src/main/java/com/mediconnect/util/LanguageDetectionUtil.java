package com.mediconnect.util;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class LanguageDetectionUtil {

    // Common Hinglish words and patterns
    private static final Set<String> HINGLISH_WORDS = Set.of(
        "hai", "hain", "kya", "aap", "mein", "main", "kar", "karo", "keh", "bol", "bolo",
        "dekh", "dekho", "sun", "suno", "jana", "karna", "hona", "tha", "thi", "the",
        "acha", "achha", "thik", "theek", "paani", "pani", "khana", "khaana", "ghar",
        "yahan", "yaha", "vahan", "vaha", "kaise", "kyun", "kyu", "kahan", "kab",
        "abhi", "phir", "sirf", "bas", "aur", "ya", "ki", "ke", "ko", "se", "mera",
        "tera", "uska", "humara", "tumhara", "unka", "wala", "wali", "vale"
    );

    // Bengali script detection pattern
    private static final Pattern BENGALI_PATTERN = Pattern.compile("[\\u0980-\\u09FF]+");
    
    // Hindi/Devanagari script detection pattern
    private static final Pattern HINDI_PATTERN = Pattern.compile("[\\u0900-\\u097F]+");
    
    // Telugu script pattern
    private static final Pattern TELUGU_PATTERN = Pattern.compile("[\\u0C00-\\u0C7F]+");
    
    // Tamil script pattern
    private static final Pattern TAMIL_PATTERN = Pattern.compile("[\\u0B80-\\u0BFF]+");

    public String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "en";
        }

        String cleanText = text.toLowerCase().trim();
        
        // Check for script-based languages first
        if (HINDI_PATTERN.matcher(text).find()) {
            return "hi";
        }
        if (BENGALI_PATTERN.matcher(text).find()) {
            return "bn";
        }
        if (TELUGU_PATTERN.matcher(text).find()) {
            return "te";
        }
        if (TAMIL_PATTERN.matcher(text).find()) {
            return "ta";
        }

        // Check for Hinglish patterns
        if (isHinglish(cleanText)) {
            return "hinglish";
        }

        // Default to English
        return "en";
    }

    private boolean isHinglish(String text) {
        String[] words = text.split("\\s+");
        int hinglishCount = 0;
        int englishCount = 0;
        int totalWords = words.length;

        if (totalWords == 0) return false;

        for (String word : words) {
            String cleanWord = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
            
            if (HINGLISH_WORDS.contains(cleanWord)) {
                hinglishCount++;
            } else if (isCommonEnglishWord(cleanWord)) {
                englishCount++;
            }
        }

        // Consider it Hinglish if:
        // 1. Has Hinglish words AND English words (code-mixing)
        // 2. At least 20% of words are recognizable Hinglish
        return (hinglishCount > 0 && englishCount > 0) || 
               (hinglishCount >= Math.max(1, totalWords * 0.2));
    }

    private boolean isCommonEnglishWord(String word) {
        Set<String> commonEnglish = Set.of(
            "the", "and", "is", "are", "was", "were", "have", "has", "had", "do", "does",
            "did", "will", "would", "can", "could", "should", "may", "might", "must",
            "i", "you", "he", "she", "it", "we", "they", "me", "him", "her", "us", "them",
            "my", "your", "his", "her", "its", "our", "their", "this", "that", "these", "those",
            "what", "where", "when", "why", "how", "who", "which", "pain", "doctor", "health",
            "medicine", "hospital", "fever", "cold", "headache", "stomach", "back", "chest"
        );
        return commonEnglish.contains(word);
    }

    public Map<String, Double> getLanguageConfidence(String text) {
        Map<String, Double> confidence = new HashMap<>();
        
        if (text == null || text.trim().isEmpty()) {
            confidence.put("en", 1.0);
            return confidence;
        }

        String cleanText = text.toLowerCase().trim();
        String[] words = cleanText.split("\\s+");
        int totalWords = words.length;

        // Initialize all supported languages with 0 confidence
        confidence.put("en", 0.0);
        confidence.put("hi", 0.0);
        confidence.put("hinglish", 0.0);
        confidence.put("bn", 0.0);

        // Script-based detection (high confidence)
        if (HINDI_PATTERN.matcher(text).find()) {
            confidence.put("hi", 0.9);
        }
        if (BENGALI_PATTERN.matcher(text).find()) {
            confidence.put("bn", 0.9);
        }

        // Word-based detection for Hinglish and English
        int hinglishWords = 0;
        int englishWords = 0;

        for (String word : words) {
            String cleanWord = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
            
            if (HINGLISH_WORDS.contains(cleanWord)) {
                hinglishWords++;
            } else if (isCommonEnglishWord(cleanWord)) {
                englishWords++;
            }
        }

        if (totalWords > 0) {
            double hinglishRatio = (double) hinglishWords / totalWords;
            double englishRatio = (double) englishWords / totalWords;

            // Hinglish confidence
            if (hinglishWords > 0 && englishWords > 0) {
                confidence.put("hinglish", Math.min(0.8, hinglishRatio + englishRatio * 0.5));
            } else if (hinglishWords > 0) {
                confidence.put("hinglish", Math.min(0.6, hinglishRatio));
            }

            // English confidence
            confidence.put("en", Math.min(0.8, englishRatio));
        }

        // Normalize confidence values
        double total = confidence.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total > 0) {
            confidence.replaceAll((k, v) -> v / total);
        } else {
            confidence.put("en", 1.0);
        }

        return confidence;
    }

    public boolean isMultiLingual(String text) {
        Map<String, Double> confidence = getLanguageConfidence(text);
        long highConfidenceCount = confidence.values().stream()
                .mapToLong(conf -> conf > 0.3 ? 1 : 0)
                .sum();
        
        return highConfidenceCount > 1;
    }

    public String getRecommendedLanguage(String text, String userPreference) {
        if (userPreference != null && !userPreference.isEmpty()) {
            return userPreference;
        }
        
        String detected = detectLanguage(text);
        
        // If detected is English but contains Hinglish patterns, suggest Hinglish
        if ("en".equals(detected) && text.toLowerCase().matches(".*\\b(hai|hain|kya|aap|mein|kar)\\b.*")) {
            return "hinglish";
        }
        
        return detected;
    }

    public List<String> getSuggestedLanguages(String text) {
        Map<String, Double> confidence = getLanguageConfidence(text);
        
        return confidence.entrySet().stream()
                .filter(entry -> entry.getValue() > 0.2)
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(3)
                .collect(java.util.stream.Collectors.toList());
    }

    // Medical terms in different languages for better detection
    private static final Map<String, Set<String>> MEDICAL_TERMS = Map.of(
        "en", Set.of("pain", "fever", "headache", "stomach", "doctor", "medicine", "hospital", 
                    "symptoms", "diagnosis", "treatment", "blood", "pressure", "diabetes"),
        "hi", Set.of("dard", "bukhar", "dawa", "aspatal", "doctor", "ilaj", "bimari", "sehat"),
        "hinglish", Set.of("pain", "dard", "fever", "bukhar", "doctor", "dawa", "medicine", 
                          "hospital", "aspatal", "health", "sehat", "problem", "samasya"),
        "bn", Set.of("byatha", "jor", "daktar", "hospital", "oshudh", "chikitsa", "shastho")
    );

    public boolean containsMedicalTerms(String text, String language) {
        if (text == null || language == null) return false;
        
        Set<String> terms = MEDICAL_TERMS.get(language);
        if (terms == null) return false;
        
        String lowerText = text.toLowerCase();
        return terms.stream().anyMatch(lowerText::contains);
    }

    public String getMedicalContextLanguage(String text) {
        String detected = detectLanguage(text);
        
        // Check if the detected language has medical context
        if (containsMedicalTerms(text, detected)) {
            return detected;
        }
        
        // Check other languages for medical terms
        for (String lang : MEDICAL_TERMS.keySet()) {
            if (containsMedicalTerms(text, lang)) {
                return lang;
            }
        }
        
        return detected;
    }
}