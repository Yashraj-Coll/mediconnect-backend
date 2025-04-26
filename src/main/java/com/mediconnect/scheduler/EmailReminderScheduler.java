package com.mediconnect.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mediconnect.model.Appointment;
import com.mediconnect.repository.AppointmentRepository;
import com.mediconnect.service.EmailService;

@Component
public class EmailReminderScheduler {
    
    private static final Logger log = LoggerFactory.getLogger(EmailReminderScheduler.class);

    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Send reminders for appointments happening in the next 24 hours
     * Runs every day at 8:00 AM
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendDailyReminders() {
        log.info("Starting daily appointment reminder job");
        
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(24);
        
        List<Appointment> upcomingAppointments = appointmentRepository.findByAppointmentDateTimeBetween(start, end);
        
        log.info("Found {} upcoming appointments for reminder", upcomingAppointments.size());
        
        for (Appointment appointment : upcomingAppointments) {
            try {
                emailService.sendAppointmentReminder(appointment);
                log.info("Sent reminder for appointment ID: {}", appointment.getId());
            } catch (Exception e) {
                log.error("Failed to send reminder for appointment ID: {}", appointment.getId(), e);
            }
        }
        
        log.info("Completed daily appointment reminder job");
    }
}