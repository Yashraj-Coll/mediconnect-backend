package com.mediconnect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class to enable Spring's scheduled task execution capability
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
    // This class enables the @Scheduled annotation functionality
    // The PasswordResetScheduledTasks service will now run its scheduled methods
}