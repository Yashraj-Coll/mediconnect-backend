package com.mediconnect.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediconnect.dto.GenomicAnalysisDTO;
import com.mediconnect.dto.MedicationRecommendationDTO;
import com.mediconnect.exception.GenomicAnalysisException;
import com.mediconnect.model.GenomicData;
import com.mediconnect.model.GenomicRiskFactor;
import com.mediconnect.model.Patient;
import com.mediconnect.repository.GenomicDataRepository;
import com.mediconnect.repository.GenomicRiskFactorRepository;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.service.FileStorageService;

@SuppressWarnings("unused")
@Service
public class GenomicDataService {

    @Value("${mediconnect.genomic.api-url}")
    private String genomicApiUrl;
    
    @Value("${mediconnect.genomic.api-key}")
    private String genomicApiKey;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private GenomicDataRepository genomicDataRepository;
    
    @Autowired
    private GenomicRiskFactorRepository riskFactorRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    /**
     * Upload and process genomic data file
     */
    @Transactional
    public GenomicData uploadGenomicData(Long patientId, MultipartFile file, String dataSource, String dataType) {
        try {
            // Validate patient exists
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new GenomicAnalysisException("Patient not found"));
            
            // Check if genomic data already exists for this patient and type
            Optional<GenomicData> existingData = genomicDataRepository.findByPatientIdAndDataType(patientId, dataType);
            if (existingData.isPresent()) {
                // Delete old file if it exists
                if (existingData.get().getFilePath() != null) {
                    fileStorageService.deleteFile(existingData.get().getFilePath());
                }
                
                // Update existing record
                GenomicData genomicData = existingData.get();
                genomicData.setDataSource(dataSource);
                genomicData.setOriginalFileName(file.getOriginalFilename());
                genomicData.setFileSize(file.getSize());
                genomicData.setUploadedAt(LocalDateTime.now());
                genomicData.setProcessed(false);
                genomicData.setProcessedAt(null);
                
                // Store the file
                String filePath = fileStorageService.storeFile(file);
                genomicData.setFilePath(filePath);
                
                return genomicDataRepository.save(genomicData);
            } else {
                // Create new genomic data record
                GenomicData genomicData = new GenomicData();
                genomicData.setPatient(patient);
                genomicData.setDataType(dataType);
                genomicData.setDataSource(dataSource);
                genomicData.setOriginalFileName(file.getOriginalFilename());
                genomicData.setFileSize(file.getSize());
                genomicData.setUploadedAt(LocalDateTime.now());
                genomicData.setProcessed(false);
                
                // Store the file
                String filePath = fileStorageService.storeFile(file);
                genomicData.setFilePath(filePath);
                
                return genomicDataRepository.save(genomicData);
            }
        } catch (Exception e) {
            throw new GenomicAnalysisException("Failed to upload genomic data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Analyze genomic data
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public GenomicAnalysisDTO analyzeGenomicData(Long genomicDataId) {
        try {
            // Get genomic data record
            GenomicData genomicData = genomicDataRepository.findById(genomicDataId)
                    .orElseThrow(() -> new GenomicAnalysisException("Genomic data not found"));
            
            // Check if file exists
            if (genomicData.getFilePath() == null) {
                throw new GenomicAnalysisException("Genomic data file not found");
            }
            
            // Load the file
            byte[] fileData = fileStorageService.loadFileAsByteArray(genomicData.getFilePath());
            
            // Encode file as base64
            String base64Data = Base64.getEncoder().encodeToString(fileData);
            
            // Prepare request for genomic analysis API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("dataType", genomicData.getDataType());
            requestBody.put("dataSource", genomicData.getDataSource());
            requestBody.put("fileData", base64Data);
            requestBody.put("patientId", genomicData.getPatient().getId().toString());
            requestBody.put("patientAge", calculateAge(genomicData.getPatient().getDateOfBirth()));
            requestBody.put("patientGender", genomicData.getPatient().getGender().toString());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", genomicApiKey);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Call genomic analysis API
            Map<String, Object> apiResponse = restTemplate.postForObject(
                    genomicApiUrl + "/analyze", 
                    requestEntity, 
                    Map.class);
            
            // Process the results
            List<Map<String, Object>> riskFactors = (List<Map<String, Object>>) apiResponse.get("riskFactors");
            
            // Clear existing risk factors
            riskFactorRepository.deleteByGenomicDataId(genomicDataId);
            
            // Save new risk factors
            List<GenomicRiskFactor> savedRiskFactors = new ArrayList<>();
            
            for (Map<String, Object> riskFactor : riskFactors) {
                GenomicRiskFactor factor = new GenomicRiskFactor();
                factor.setGenomicData(genomicData);
                factor.setCondition((String) riskFactor.get("condition"));
                factor.setRiskLevel((String) riskFactor.get("riskLevel"));
                factor.setRiskScore((Double) riskFactor.get("riskScore"));
                factor.setMarkers((String) riskFactor.get("markers"));
                factor.setRecommendations((String) riskFactor.get("recommendations"));
                
                savedRiskFactors.add(riskFactorRepository.save(factor));
            }
            
            // Update genomic data record
            genomicData.setProcessed(true);
            genomicData.setProcessedAt(LocalDateTime.now());
            genomicData.setAnalysisResult(new ObjectMapper().writeValueAsString(apiResponse));
            genomicDataRepository.save(genomicData);
            
            // Prepare response DTO
            GenomicAnalysisDTO response = new GenomicAnalysisDTO();
            response.setGenomicDataId(genomicData.getId());
            response.setPatientId(genomicData.getPatient().getId());
            response.setDataType(genomicData.getDataType());
            response.setRiskFactors(riskFactors);
            response.setProcessedAt(genomicData.getProcessedAt());
            
            return response;
            
        } catch (Exception e) {
            throw new GenomicAnalysisException("Genomic analysis failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get personalized medication recommendations based on genomic data
     */
    @SuppressWarnings("unchecked")
    public List<MedicationRecommendationDTO> getPersonalizedMedicationRecommendations(
            Long patientId, String condition) {
        try {
            // Check if patient has genomic data
            List<GenomicData> genomicData = genomicDataRepository.findByPatientId(patientId);
            
            if (genomicData.isEmpty()) {
                throw new GenomicAnalysisException("No genomic data found for patient");
            }
            
            // Get patient info
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new GenomicAnalysisException("Patient not found"));
            
            // Prepare request for genomic API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("patientId", patientId.toString());
            requestBody.put("condition", condition);
            requestBody.put("age", calculateAge(patient.getDateOfBirth()));
            requestBody.put("gender", patient.getGender().toString());
            requestBody.put("allergies", patient.getAllergies());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", genomicApiKey);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Call medication recommendation API
            List<Map<String, Object>> apiResponse = restTemplate.postForObject(
                    genomicApiUrl + "/medication-recommendations", 
                    requestEntity, 
                    List.class);
            
            // Convert to DTOs
            List<MedicationRecommendationDTO> recommendations = new ArrayList<>();
            
            for (Map<String, Object> recommendation : apiResponse) {
                MedicationRecommendationDTO dto = new MedicationRecommendationDTO();
                dto.setMedicationName((String) recommendation.get("medicationName"));
                dto.setDosage((String) recommendation.get("dosage"));
                dto.setEffectiveness((Double) recommendation.get("effectiveness"));
                dto.setGeneticCompatibility((Double) recommendation.get("geneticCompatibility"));
                dto.setRationale((String) recommendation.get("rationale"));
                dto.setSideEffectRisk((String) recommendation.get("sideEffectRisk"));
                dto.setAlternatives((List<String>) recommendation.get("alternatives"));
                
                recommendations.add(dto);
            }
            
            return recommendations;
            
        } catch (Exception e) {
            throw new GenomicAnalysisException("Failed to get medication recommendations: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get genetic risk analysis for patient
     */
    public List<GenomicRiskFactor> getGeneticRiskAnalysis(Long patientId) {
        List<GenomicData> genomicData = genomicDataRepository.findByPatientId(patientId);
        
        if (genomicData.isEmpty()) {
            throw new GenomicAnalysisException("No genomic data found for patient");
        }
        
        List<GenomicRiskFactor> allRiskFactors = new ArrayList<>();
        
        for (GenomicData data : genomicData) {
            if (data.isProcessed()) {
                List<GenomicRiskFactor> factors = riskFactorRepository.findByGenomicDataId(data.getId());
                allRiskFactors.addAll(factors);
            }
        }
        
        return allRiskFactors;
    }
    
    /**
     * Calculate patient age in years
     */
    private int calculateAge(java.time.LocalDate dateOfBirth) {
        return java.time.Period.between(dateOfBirth, java.time.LocalDate.now()).getYears();
    }
}