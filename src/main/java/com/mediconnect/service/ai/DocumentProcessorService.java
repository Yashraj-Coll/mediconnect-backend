package com.mediconnect.service.ai;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Enhanced Document Processor Service
 * Handles text extraction from various document formats including PDF, Word, and images with OCR
 */
@Service
public class DocumentProcessorService {

    private final Tika tika = new Tika();

    /**
     * Extract text from uploaded document based on file type
     */
    public String extractTextFromDocument(MultipartFile file) {
        try {
            String contentType = file.getContentType();
            String fileName = file.getOriginalFilename();
            
            if (fileName == null) {
                throw new IllegalArgumentException("File name cannot be null");
            }

            try (InputStream inputStream = file.getInputStream()) {
                if ("application/pdf".equals(contentType) || fileName.toLowerCase().endsWith(".pdf")) {
                    return extractTextFromPDF(inputStream);
                } else if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType) 
                        || fileName.toLowerCase().endsWith(".docx")) {
                    return extractTextFromDocx(inputStream);
                } else if ("application/msword".equals(contentType) || fileName.toLowerCase().endsWith(".doc")) {
                    return extractTextFromDoc(inputStream);
                } else if (contentType != null && contentType.startsWith("text/")) {
                    return new String(file.getBytes());
                } else {
                    return tika.parseToString(inputStream);
                }
            }
        } catch (Exception e) {
            // Log the error and return empty string
            System.err.println("Error extracting text from document: " + e.getMessage());
            return "";
        }
    }

    /**
     * Extract text from PDF files using PDFBox
     */
    private String extractTextFromPDF(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            // Clean up extracted text
            return cleanExtractedText(text);
        }
    }

    /**
     * Extract text from .docx files using Apache POI
     */
    private String extractTextFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            
            String text = extractor.getText();
            return cleanExtractedText(text);
        }
    }

    /**
     * Extract text from .doc files using Apache POI
     */
    private String extractTextFromDoc(InputStream inputStream) throws IOException {
        try (HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            
            String text = extractor.getText();
            return cleanExtractedText(text);
        }
    }

    /**
     * Clean and normalize extracted text
     */
    private String cleanExtractedText(String text) {
        if (text == null) {
            return "";
        }
        
        return text
                .replaceAll("\\s+", " ") // Multiple spaces to single space
                .replaceAll("\\n+", "\n") // Multiple newlines to single newline
                .trim();
    }

    /**
     * Validate if file is a supported document type
     */
    public boolean isSupportedDocument(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        
        if (fileName == null) return false;
        
        String lowerFileName = fileName.toLowerCase();
        
        return (contentType != null && (
                contentType.equals("application/pdf") ||
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.startsWith("text/")
        )) || (
                lowerFileName.endsWith(".pdf") ||
                lowerFileName.endsWith(".doc") ||
                lowerFileName.endsWith(".docx") ||
                lowerFileName.endsWith(".txt")
        );
    }

    /**
     * Get document type description for logging/UI purposes
     */
    public String getDocumentType(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        
        if (fileName == null) return "Unknown";
        
        String lowerFileName = fileName.toLowerCase();
        
        if ("application/pdf".equals(contentType) || lowerFileName.endsWith(".pdf")) {
            return "PDF Document";
        } else if ("application/msword".equals(contentType) || lowerFileName.endsWith(".doc")) {
            return "Word Document (.doc)";
        } else if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType) 
                || lowerFileName.endsWith(".docx")) {
            return "Word Document (.docx)";
        } else if (contentType != null && contentType.startsWith("text/")) {
            return "Text Document";
        } else {
            return "Document";
        }
    }

    /**
     * Extract key medical information from text using pattern matching
     */
    public String extractMedicalKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        String lowerText = text.toLowerCase();
        StringBuilder keywords = new StringBuilder();
        
        // Common medical terms to highlight
        String[] medicalTerms = {
            "diagnosis", "symptoms", "medication", "prescription", "dosage",
            "blood pressure", "heart rate", "temperature", "weight", "height",
            "diabetes", "hypertension", "cholesterol", "infection", "allergy",
            "pain", "fever", "headache", "nausea", "dizziness"
        };
        
        for (String term : medicalTerms) {
            if (lowerText.contains(term)) {
                if (keywords.length() > 0) {
                    keywords.append(", ");
                }
                keywords.append(term);
            }
        }
        
        return keywords.toString();
    }

    /**
     * Analyze document content and provide summary
     */
    public String analyzeDocumentContent(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Document appears to be empty or unreadable.";
        }
        
        StringBuilder analysis = new StringBuilder();
        String lowerText = text.toLowerCase();
        
        // Document length analysis
        int wordCount = text.split("\\s+").length;
        analysis.append("Document contains approximately ").append(wordCount).append(" words. ");
        
        // Check for medical document indicators
        if (lowerText.contains("prescription") || lowerText.contains("rx")) {
            analysis.append("This appears to be a prescription document. ");
        }
        if (lowerText.contains("lab result") || lowerText.contains("test result")) {
            analysis.append("This appears to contain laboratory or test results. ");
        }
        if (lowerText.contains("diagnosis") || lowerText.contains("condition")) {
            analysis.append("This document contains diagnostic information. ");
        }
        if (lowerText.contains("medication") || lowerText.contains("drug")) {
            analysis.append("This document mentions medications or drugs. ");
        }
        
        // Extract key medical terms
        String keywords = extractMedicalKeywords(text);
        if (!keywords.isEmpty()) {
            analysis.append("Key medical terms found: ").append(keywords).append(". ");
        }
        
        return analysis.toString();
    }
}