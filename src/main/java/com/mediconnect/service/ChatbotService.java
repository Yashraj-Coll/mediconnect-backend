package com.mediconnect.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mediconnect.dto.ChatbotMessageDTO;
import com.mediconnect.dto.ChatbotResponseDTO;
import com.mediconnect.exception.ChatbotException;
import com.mediconnect.model.ChatSession;
import com.mediconnect.model.Patient;
import com.mediconnect.model.Prescription;
import com.mediconnect.repository.AppointmentRepository;
import com.mediconnect.repository.ChatSessionRepository;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.repository.PrescriptionRepository;

@Service
public class ChatbotService {

    @Value("${mediconnect.chatbot.api-url}")
    private String chatbotApiUrl;
    
    @Value("${mediconnect.chatbot.api-key}")
    private String chatbotApiKey;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private PrescriptionRepository prescriptionRepository;
    
    @Autowired
    private ChatSessionRepository chatSessionRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Process a user message and generate response
     */
    public ChatbotResponseDTO processMessage(Long patientId, ChatbotMessageDTO message) {
        try {
            // Get patient info
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new ChatbotException("Patient not found"));
            
            // Get or create chat session
            ChatSession session = getOrCreateChatSession(patientId, message.getSessionId());
            
            // Prepare request for chatbot API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", message.getMessage());
            requestBody.put("sessionId", session.getSessionId());
            requestBody.put("patientId", patientId.toString());
            requestBody.put("patientName", patient.getUser().getFirstName() + " " + patient.getUser().getLastName());
            
            // Add conversation history
            requestBody.put("conversation", session.getConversationHistory());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-Key", chatbotApiKey);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Call chatbot API
            @SuppressWarnings("unchecked")
			Map<String, Object> apiResponse = restTemplate.postForObject(
                    chatbotApiUrl, 
                    requestEntity, 
                    Map.class);
            
            // Process response
            String responseText = (String) apiResponse.get("response");
            String intent = (String) apiResponse.get("intent");
            
            // Update conversation history
            List<Map<String, String>> history = session.getConversationHistory();
            history.add(Map.of("role", "user", "content", message.getMessage()));
            history.add(Map.of("role", "assistant", "content", responseText));
            
            if (history.size() > 20) {
                // Keep conversation history to a reasonable size
                history = history.subList(history.size() - 20, history.size());
            }
            
            session.setConversationHistory(history);
            session.setLastInteractionAt(LocalDateTime.now());
            chatSessionRepository.save(session);
            
            // Handle specific intents
            Map<String, Object> actionData = new HashMap<>();
            
            if ("schedule_appointment".equals(intent)) {
                actionData = handleAppointmentScheduling(apiResponse, patient);
            } else if ("medication_reminder".equals(intent)) {
                actionData = handleMedicationReminder(apiResponse, patient);
            } else if ("medical_question".equals(intent)) {
                actionData = handleMedicalQuestion(apiResponse);
            }
            
            // Prepare response
            ChatbotResponseDTO response = new ChatbotResponseDTO();
            response.setSessionId(session.getSessionId());
            response.setMessage(responseText);
            response.setIntent(intent);
            response.setActionRequired("schedule_appointment".equals(intent) || 
                                    "medication_reminder".equals(intent));
            response.setActionData(actionData);
            
            return response;
            
        } catch (Exception e) {
            throw new ChatbotException("Chatbot processing failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get or create a chat session
     */
    private ChatSession getOrCreateChatSession(Long patientId, String sessionId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            Optional<ChatSession> existingSession = chatSessionRepository.findBySessionId(sessionId);
            if (existingSession.isPresent()) {
                return existingSession.get();
            }
        }
        
        // Create new session
        ChatSession session = new ChatSession();
        session.setPatient(patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found")));
        session.setSessionId(UUID.randomUUID().toString());
        session.setStartedAt(LocalDateTime.now());
        session.setLastInteractionAt(LocalDateTime.now());
        session.setConversationHistory(new ArrayList<>());
        
        return chatSessionRepository.save(session);
    }
    
 // Continue ChatbotService.java

    /**
     * Handle appointment scheduling intent
     */
    private Map<String, Object> handleAppointmentScheduling(Map<String, Object> apiResponse, Patient patient) {
        Map<String, Object> actionData = new HashMap<>();
        
        // Extract appointment details from API response
        @SuppressWarnings("unchecked")
        Map<String, Object> entities = (Map<String, Object>) apiResponse.get("entities");
        
        if (entities != null) {
            // Format: 2023-04-15T14:30:00
            String dateTimeStr = (String) entities.get("appointmentDateTime");
            String doctorSpecialty = (String) entities.get("doctorSpecialty");
            String appointmentType = (String) entities.get("appointmentType");
            
            if (dateTimeStr != null) {
                try {
                    LocalDateTime appointmentDateTime = LocalDateTime.parse(dateTimeStr);
                    
                    // Find available doctors
                    List<Map<String, Object>> availableDoctors = findAvailableDoctors(
                            appointmentDateTime, doctorSpecialty);
                    
                    actionData.put("appointmentDateTime", appointmentDateTime.format(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    actionData.put("doctorSpecialty", doctorSpecialty);
                    actionData.put("appointmentType", appointmentType);
                    actionData.put("availableDoctors", availableDoctors);
                    actionData.put("patientId", patient.getId());
                    
                } catch (Exception e) {
                    actionData.put("error", "Could not parse appointment date/time");
                }
            } else {
                actionData.put("error", "No appointment date/time provided");
            }
        }
        
        return actionData;
    }
    
    /**
     * Handle medication reminder intent
     */
    private Map<String, Object> handleMedicationReminder(Map<String, Object> apiResponse, Patient patient) {
        Map<String, Object> actionData = new HashMap<>();
        
        // Get active prescriptions for patient
        List<Prescription> activePrescriptions = prescriptionRepository.findActiveByPatientId(
                patient.getId(), LocalDateTime.now());
        
        if (activePrescriptions.isEmpty()) {
            actionData.put("hasPrescriptions", false);
        } else {
            actionData.put("hasPrescriptions", true);
            
            List<Map<String, Object>> medicationList = new ArrayList<>();
            for (Prescription prescription : activePrescriptions) {
                List<Object> medications = new ArrayList<>();
                
                prescription.getPrescriptionItems().forEach(item -> {
                    Map<String, Object> medicationInfo = new HashMap<>();
                    medicationInfo.put("name", item.getMedicationName());
                    medicationInfo.put("dosage", item.getDosage());
                    medicationInfo.put("frequency", item.getFrequency());
                    medicationInfo.put("instructions", item.getInstructions());
                    medicationInfo.put("beforeMeal", item.isBeforeMeal());
                    medications.add(medicationInfo);
                });
                
                Map<String, Object> prescriptionInfo = new HashMap<>();
                prescriptionInfo.put("id", prescription.getId());
                prescriptionInfo.put("doctorName", "Dr. " + prescription.getDoctor().getUser().getLastName());
                prescriptionInfo.put("medications", medications);
                prescriptionInfo.put("validUntil", prescription.getValidUntil().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                
                medicationList.add(prescriptionInfo);
            }
            
            actionData.put("prescriptions", medicationList);
            
            // Set up medication reminder if requested
            @SuppressWarnings("unchecked")
            Map<String, Object> entities = (Map<String, Object>) apiResponse.get("entities");
            
            if (entities != null && entities.containsKey("reminderTime")) {
                String reminderTime = (String) entities.get("reminderTime");
                String medicationName = (String) entities.get("medicationName");
                
                // Save reminder
                notificationService.schedulePatientMedicationReminder(
                        patient.getId(), 
                        medicationName, 
                        reminderTime);
                
                actionData.put("reminderSet", true);
                actionData.put("reminderTime", reminderTime);
                actionData.put("medicationName", medicationName);
            }
        }
        
        return actionData;
    }
    
    /**
     * Handle medical question intent
     */
    private Map<String, Object> handleMedicalQuestion(Map<String, Object> apiResponse) {
        Map<String, Object> actionData = new HashMap<>();
        
        // Get the medical information from the API response
        @SuppressWarnings("unchecked")
        Map<String, Object> medicalInfo = (Map<String, Object>) apiResponse.get("medicalInfo");
        
        if (medicalInfo != null) {
            actionData.putAll(medicalInfo);
            
            // Check if this should be escalated to a doctor
            Boolean shouldEscalate = (Boolean) apiResponse.get("shouldEscalate");
            if (shouldEscalate != null && shouldEscalate) {
                actionData.put("shouldEscalate", true);
                actionData.put("escalationReason", apiResponse.get("escalationReason"));
            } else {
                actionData.put("shouldEscalate", false);
            }
            
            // Include references if available
            @SuppressWarnings("unchecked")
            List<String> references = (List<String>) apiResponse.get("references");
            if (references != null && !references.isEmpty()) {
                actionData.put("references", references);
            }
        }
        
        return actionData;
    }
    
    /**
     * Find available doctors for appointment
     */
    private List<Map<String, Object>> findAvailableDoctors(LocalDateTime appointmentDateTime, String specialty) {
        // Query for available doctors
        List<Object[]> availableDoctors = appointmentRepository.findAvailableDoctors(
                appointmentDateTime, 
                appointmentDateTime.plusMinutes(30),
                specialty);
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Object[] doctor : availableDoctors) {
            Map<String, Object> doctorInfo = new HashMap<>();
            doctorInfo.put("id", doctor[0]);
            doctorInfo.put("firstName", doctor[1]);
            doctorInfo.put("lastName", doctor[2]);
            doctorInfo.put("specialization", doctor[3]);
            doctorInfo.put("rating", doctor[4]);
            result.add(doctorInfo);
        }
        
        return result;
    }
}