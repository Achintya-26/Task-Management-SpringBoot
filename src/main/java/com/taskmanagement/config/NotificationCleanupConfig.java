package com.taskmanagement.config;

import com.taskmanagement.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuration for scheduled notification cleanup tasks
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "notification.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationCleanupConfig {

    @Autowired
    private NotificationService notificationService;

    /**
     * Scheduled task to cleanup notifications for all users
     * Runs every hour to ensure no user exceeds the 50-notification limit
     */
    @Scheduled(fixedRate = 3600000) // Run every hour (3600000 milliseconds)
    public void scheduledNotificationCleanup() {
        try {
            System.out.println("Starting scheduled notification cleanup...");
            notificationService.cleanupNotificationsForAllUsers();
            System.out.println("Scheduled notification cleanup completed successfully");
        } catch (Exception e) {
            System.err.println("Error during scheduled notification cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Scheduled task to cleanup very old notifications (older than 30 days)
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2:00 AM
    public void scheduledOldNotificationCleanup() {
        try {
            System.out.println("Starting scheduled old notification cleanup...");
            int deletedCount = notificationService.deleteOldNotifications(30);
            System.out.println("Scheduled old notification cleanup completed. Deleted " + deletedCount + " notifications older than 30 days");
        } catch (Exception e) {
            System.err.println("Error during scheduled old notification cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
