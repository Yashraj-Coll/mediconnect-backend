package com.mediconnect.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mediconnect.model.Patient;
import com.mediconnect.repository.PatientRepository;

/**
 * Service for managing medication reminders
 */
@Service
public class ReminderService {

    public TaskScheduler getTaskScheduler() {
		return taskScheduler;
	}

	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	public NotificationService getNotificationService() {
		return notificationService;
	}

	public void setNotificationService(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	public PatientRepository getPatientRepository() {
		return patientRepository;
	}

	public void setPatientRepository(PatientRepository patientRepository) {
		this.patientRepository = patientRepository;
	}

	public Map<String, Map<String, Object>> getScheduledReminders() {
		return scheduledReminders;
	}

	public void setScheduledReminders(Map<String, Map<String, Object>> scheduledReminders) {
		this.scheduledReminders = scheduledReminders;
	}

	@Autowired
    private TaskScheduler taskScheduler;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private PatientRepository patientRepository;
    
    // In-memory store of scheduled reminders
    // In a production app, this would be stored in a database
    private Map<String, Map<String, Object>> scheduledReminders = new ConcurrentHashMap<>();
    
    /**
     * Schedule a reminder for a medication
     */
    public void scheduleReminderForMedication(
            Long patientId,
            Long medicationId,
            String medicationName,
            String dosage,
            String reminderTime,
            LocalDateTime validUntil) {
        
        try {
            @SuppressWarnings("unused")
			Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            
            // Parse the reminder time (e.g., "09:00 AM")
            LocalTime time = null;
            boolean isDailyReminder = false;
            
            if (reminderTime.contains(":")) {
                // It's a specific time of day
                String timeStr = reminderTime.split(",")[0].trim();
                time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("hh:mm a"));
                isDailyReminder = true;
            }
            
            // Create a unique ID for this reminder
            String reminderId = UUID.randomUUID().toString();
            
            // Store reminder info
            Map<String, Object> reminderInfo = new ConcurrentHashMap<>();
            reminderInfo.put("patientId", patientId);
            reminderInfo.put("medicationId", medicationId);
            reminderInfo.put("medicationName", medicationName);
            reminderInfo.put("dosage", dosage);
            reminderInfo.put("reminderTime", reminderTime);
            reminderInfo.put("validUntil", validUntil);
            reminderInfo.put("isDailyReminder", isDailyReminder);
            reminderInfo.put("timeOfDay", time);
            
            // Store the reminder
            scheduledReminders.put(reminderId, reminderInfo);
            
            // Return the reminder ID
            System.out.println("Scheduled reminder: " + reminderId + " for " + medicationName);
        } catch (Exception e) {
            System.err.println("Error scheduling reminder: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Cancel a reminder for a medication
     */
    public void cancelReminderForMedication(Long medicationId) {
        // Find and remove all reminders for this medication
        scheduledReminders.entrySet().removeIf(entry -> 
            medicationId.equals(entry.getValue().get("medicationId")));
    }
    
    /**
     * Check for and send daily medication reminders
     * Runs every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    public void processDailyMedicationReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        
        // Margin of error (15 minutes)
        LocalTime timeMin = currentTime.minusMinutes(15);
        LocalTime timeMax = currentTime.plusMinutes(45);
        
        // Check each reminder
        scheduledReminders.forEach((reminderId, reminderInfo) -> {
            try {
                Boolean isDailyReminder = (Boolean) reminderInfo.get("isDailyReminder");
                LocalDateTime validUntil = (LocalDateTime) reminderInfo.get("validUntil");
                
                // Skip if expired
                if (validUntil != null && validUntil.isBefore(now)) {
                    return;
                }
                
                if (isDailyReminder) {
                    LocalTime scheduledTime = (LocalTime) reminderInfo.get("timeOfDay");
                    
                    // If it's time to send the reminder (within the margin)
                    if (scheduledTime != null && 
                            !scheduledTime.isBefore(timeMin) && 
                            !scheduledTime.isAfter(timeMax)) {
                        
                        // Send notification
                        Long patientId = (Long) reminderInfo.get("patientId");
                        String medicationName = (String) reminderInfo.get("medicationName");
                        String dosage = (String) reminderInfo.get("dosage");
                        
                        notificationService.sendMedicationReminder(
                                patientId, 
                                medicationName, 
                                dosage, 
                                (String) reminderInfo.get("reminderTime"));
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing reminder " + reminderId + ": " + e.getMessage());
            }
        });
    }
}