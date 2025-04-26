package com.mediconnect.service;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mediconnect.dto.InteractionWarningDTO;
import com.mediconnect.exception.MedicationInteractionException;
import com.mediconnect.model.Patient;
import com.mediconnect.repository.PatientRepository;

@Service
public class MedicationInteractionService {

 @Value("${mediconnect.ai.medication.api-url}")
 private String medicationApiUrl;
 
 @Value("${mediconnect.ai.medication.api-key}")
 private String medicationApiKey;
 
 @Autowired
 private RestTemplate restTemplate;
 
 @Autowired
 private PatientRepository patientRepository;
 
 /**
  * Check for potential interactions between medications
  */
 public List<InteractionWarningDTO> checkInteractions(List<String> medications, Long patientId) {
     try {
         // Retrieve patient information
         Patient patient = patientRepository.findById(patientId)
                 .orElseThrow(() -> new MedicationInteractionException("Patient not found"));
         
         // Prepare request to medication interaction service
         Map<String, Object> requestBody = new HashMap<>();
         requestBody.put("medications", medications);
         requestBody.put("patientAge", calculateAge(patient.getDateOfBirth()));
         requestBody.put("patientGender", patient.getGender().toString());
         requestBody.put("allergies", patient.getAllergies());
         requestBody.put("chronicDiseases", patient.getChronicDiseases());
         
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);
         headers.set("X-API-Key", medicationApiKey);
         
         HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
         
         // Call medication interaction API
         @SuppressWarnings("unchecked")
		 Map<String, Object> apiResponse = restTemplate.postForObject(
                 medicationApiUrl + "/interactions", 
                 requestEntity, 
                 Map.class);
         
         // Process interaction warnings from API response
         @SuppressWarnings("unchecked")
		 List<Map<String, Object>> apiInteractions = (List<Map<String, Object>>) apiResponse.get("interactions");
         List<InteractionWarningDTO> interactionWarnings = new ArrayList<>();
         
         for (Map<String, Object> interaction : apiInteractions) {
             InteractionWarningDTO warning = new InteractionWarningDTO();
             warning.setMedication1((String) interaction.get("medication1"));
             warning.setMedication2((String) interaction.get("medication2"));
             warning.setSeverity((String) interaction.get("severity"));
             warning.setDescription((String) interaction.get("description"));
             warning.setRecommendation((String) interaction.get("recommendation"));
             warning.setSourceReference((String) interaction.get("sourceReference"));
             warning.setEvidence((String) interaction.get("evidence"));
             
             interactionWarnings.add(warning);
         }
         
         return interactionWarnings;
         
     } catch (Exception e) {
         throw new MedicationInteractionException("Failed to check medication interactions: " + e.getMessage(), e);
     }
 }
 
 public String getMedicationApiUrl() {
	return medicationApiUrl;
}

public void setMedicationApiUrl(String medicationApiUrl) {
	this.medicationApiUrl = medicationApiUrl;
}

public String getMedicationApiKey() {
	return medicationApiKey;
}

public void setMedicationApiKey(String medicationApiKey) {
	this.medicationApiKey = medicationApiKey;
}

public RestTemplate getRestTemplate() {
	return restTemplate;
}

public void setRestTemplate(RestTemplate restTemplate) {
	this.restTemplate = restTemplate;
}

public PatientRepository getPatientRepository() {
	return patientRepository;
}

public void setPatientRepository(PatientRepository patientRepository) {
	this.patientRepository = patientRepository;
}

/**
  * Check interactions of a prescription with existing medications
  */
 public List<InteractionWarningDTO> checkPrescriptionInteractions(List<String> newMedications, Long patientId) {
     try {
         // Get current medications for the patient from existing prescriptions
         List<String> currentMedications = getCurrentMedications(patientId);
         
         // Add new medications to the list
         List<String> allMedications = new ArrayList<>(currentMedications);
         allMedications.addAll(newMedications);
         
         // Check interactions for the combined list
         return checkInteractions(allMedications, patientId);
         
     } catch (Exception e) {
         throw new MedicationInteractionException("Failed to check prescription interactions: " + e.getMessage(), e);
     }
 }
 
 /**
  * Get current medications for a patient from active prescriptions
  */
 private List<String> getCurrentMedications(Long patientId) {
     // Implementation would depend on your prescription repository structure
     // This is a placeholder - you would retrieve active medications from the patient's prescriptions
     return new ArrayList<>(); // Replace with actual implementation
 }
 
 /**
  * Calculate patient age in years
  */
 private int calculateAge(java.time.LocalDate dateOfBirth) {
     return java.time.Period.between(dateOfBirth, java.time.LocalDate.now()).getYears();
 }
}