package com.taskmanagement.service;

import com.taskmanagement.model.Notification;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.NotificationRepository;
import com.taskmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new notification
     */
    @Transactional
    public Notification createNotification(Long userId, String title, String message, 
                                         String type, Long relatedTeamId, Long relatedActivityId) {
        // Validate user exists
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedTeamId(relatedTeamId);
        notification.setRelatedActivityId(relatedActivityId);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }

    /**
     * Create notification with minimal parameters
     */
    @Transactional
    public Notification createNotification(Long userId, String title, String message, String type) {
        return createNotification(userId, title, message, type, null, null);
    }

    /**
     * Get all notifications for a user
     */
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get unread notifications for a user
     */
    public List<Notification> getUnreadNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public Notification markAsRead(Long notificationId, Long userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        
        if (!notificationOpt.isPresent()) {
            throw new RuntimeException("Notification not found with ID: " + notificationId);
        }
        
        Notification notification = notificationOpt.get();
        
        // Verify the notification belongs to the user
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied: Notification does not belong to user");
        }
        
        notification.setIsRead(true);
        notification.setUpdatedAt(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsReadForUser(Long userId) {
        List<Notification> unreadNotifications = getUnreadNotificationsByUserId(userId);
        
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setUpdatedAt(LocalDateTime.now());
        }
        
        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * Delete a notification
     */
    @Transactional
    public boolean deleteNotification(Long notificationId, Long userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        
        if (!notificationOpt.isPresent()) {
            return false;
        }
        
        Notification notification = notificationOpt.get();
        
        // Verify the notification belongs to the user
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied: Notification does not belong to user");
        }
        
        notificationRepository.delete(notification);
        return true;
    }

    /**
     * Get notification count for a user
     */
    public long getNotificationCount(Long userId) {
        return notificationRepository.countByUserId(userId);
    }

    /**
     * Get unread notification count for a user
     */
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Delete old notifications (older than specified days)
     */
    @Transactional
    public int deleteOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        return notificationRepository.deleteByCreatedAtBefore(cutoffDate);
    }

    /**
     * Get notifications by type for a user
     */
    public List<Notification> getNotificationsByType(Long userId, String type) {
        return notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type);
    }

    /**
     * Get notifications related to a specific team
     */
    public List<Notification> getNotificationsByTeam(Long userId, Long teamId) {
        return notificationRepository.findByUserIdAndRelatedTeamIdOrderByCreatedAtDesc(userId, teamId);
    }

    /**
     * Get notifications related to a specific activity
     */
    public List<Notification> getNotificationsByActivity(Long userId, Long activityId) {
        return notificationRepository.findByUserIdAndRelatedActivityIdOrderByCreatedAtDesc(userId, activityId);
    }

    /**
     * Create team-related notification (helper method)
     */
    @Transactional
    public Notification createTeamNotification(Long userId, String title, String message, 
                                             String type, Long teamId) {
        return createNotification(userId, title, message, type, teamId, null);
    }

    /**
     * Create activity-related notification (helper method)
     */
    @Transactional
    public Notification createActivityNotification(Long userId, String title, String message, 
                                                  String type, Long activityId) {
        return createNotification(userId, title, message, type, null, activityId);
    }

    /**
     * Create bulk notifications for multiple users
     */
    @Transactional
    public void createBulkNotifications(List<Long> userIds, String title, String message, 
                                      String type, Long relatedTeamId, Long relatedActivityId) {
        for (Long userId : userIds) {
            try {
                createNotification(userId, title, message, type, relatedTeamId, relatedActivityId);
            } catch (Exception e) {
                // Log error but continue with other users
                System.err.println("Failed to create notification for user " + userId + ": " + e.getMessage());
            }
        }
    }

    /**
     * Get paginated notifications
     */
    public List<Notification> getNotificationsPaginated(Long userId, int page, int size) {
        // This would require implementing Pageable - for now returning all
        return getNotificationsByUserId(userId);
    }
}