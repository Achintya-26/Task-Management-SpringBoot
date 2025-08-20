package com.taskmanagement.service;

import com.taskmanagement.model.Notification;
import com.taskmanagement.model.User;
import com.taskmanagement.model.Activity;
import com.taskmanagement.model.Team;
import com.taskmanagement.repository.NotificationRepository;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.websocket.NotificationWebSocketHandler;
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

    @Autowired
    private NotificationWebSocketHandler webSocketHandler;

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
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Debug logging
        System.out.println("Created notification: " + title + " for user: " + userId);
        
        // Send real-time notification via WebSocket
        try {
            webSocketHandler.sendNotificationToUser(userId, savedNotification);
            System.out.println("WebSocket notification sent successfully to user: " + userId);
        } catch (Exception e) {
            System.err.println("Failed to send real-time notification to user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return savedNotification;
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

    // Activity-related notification helper methods

    /**
     * Notify when activity is created
     */
    @Transactional
    public void notifyActivityCreated(Activity activity, User creator) {
        System.out.println("notifyActivityCreated called for activity: " + activity.getName() + " by creator: " + creator.getName());
        
        if (activity.getAssignedMembers() != null) {
            System.out.println("Activity has " + activity.getAssignedMembers().size() + " assigned members");
            for (User member : activity.getAssignedMembers()) {
                if (!member.getId().equals(creator.getId())) { // Don't notify creator
                    System.out.println("Creating notification for member: " + member.getName() + " (ID: " + member.getId() + ")");
                    createNotification(
                        member.getId(),
                        "New Activity Assigned",
                        "You have been assigned to activity: " + activity.getName(),
                        "ACTIVITY_ASSIGNED",
                        null,
                        activity.getId()
                    );
                } else {
                    System.out.println("Skipping notification for creator: " + member.getName());
                }
            }
        } else {
            System.out.println("Activity has no assigned members");
        }
    }

    /**
     * Notify when activity is updated
     */
    @Transactional
    public void notifyActivityUpdated(Activity activity, User updater, String updateDetails) {
        if (activity.getAssignedMembers() != null) {
            for (User member : activity.getAssignedMembers()) {
                if (!member.getId().equals(updater.getId())) { // Don't notify updater
                    createNotification(
                        member.getId(),
                        "Activity Updated",
                        "Activity '" + activity.getName() + "' has been updated. " + updateDetails,
                        "ACTIVITY_UPDATED",
                        null,
                        activity.getId()
                    );
                }
            }
        }
    }

    /**
     * Notify when activity status changes
     */
    @Transactional
    public void notifyActivityStatusChanged(Activity activity, User updater) {
        if (activity.getAssignedMembers() != null) {
            for (User member : activity.getAssignedMembers()) {
                if (!member.getId().equals(updater.getId())) { // Don't notify updater
                    createNotification(
                        member.getId(),
                        "Activity Status Changed",
                        "Activity '" + activity.getName() + "' status changed to: " + activity.getStatus(),
                        "ACTIVITY_STATUS_CHANGED",
                        null,
                        activity.getId()
                    );
                }
            }
        }
    }

    /**
     * Notify when user is added to team
     */
    @Transactional
    public void notifyUserAddedToTeam(Team team, User addedUser, User addedBy) {
        createNotification(
            addedUser.getId(),
            "Added to Team",
            "You have been added to team: " + team.getName(),
            "TEAM_MEMBER_ADDED",
            team.getId(),
            null
        );
    }

    /**
     * Notify when user is removed from team
     */
    @Transactional
    public void notifyUserRemovedFromTeam(Team team, User removedUser, User removedBy) {
        createNotification(
            removedUser.getId(),
            "Removed from Team",
            "You have been removed from team: " + team.getName(),
            "TEAM_MEMBER_REMOVED",
            team.getId(),
            null
        );
    }

    /**
     * Notify when remark is added to activity
     */
    @Transactional
    public void notifyRemarkAdded(Activity activity, User remarkAuthor, String remarkText) {
        System.out.println("notifyRemarkAdded called for activity: " + activity.getName() + " by user: " + remarkAuthor.getName());
        
        if (activity.getAssignedMembers() != null) {
            System.out.println("Activity has " + activity.getAssignedMembers().size() + " assigned members");
            for (User member : activity.getAssignedMembers()) {
                if (!member.getId().equals(remarkAuthor.getId())) { // Don't notify remark author
                    System.out.println("Creating remark notification for member: " + member.getName() + " (ID: " + member.getId() + ")");
                    createNotification(
                        member.getId(),
                        "New Remark Added",
                        remarkAuthor.getName() + " added a remark to activity '" + activity.getName() + "': " + 
                        (remarkText.length() > 100 ? remarkText.substring(0, 100) + "..." : remarkText),
                        "ACTIVITY_REMARK_ADDED",
                        null,
                        activity.getId()
                    );
                } else {
                    System.out.println("Skipping remark notification for author: " + member.getName());
                }
            }
        } else {
            System.out.println("Activity has no assigned members for remark notification");
        }

        // Also notify the activity creator if they're not the remark author and not already in assigned members
        if (!activity.getCreatedBy().equals(remarkAuthor.getId())) {
            boolean creatorAlreadyNotified = false;
            if (activity.getAssignedMembers() != null) {
                creatorAlreadyNotified = activity.getAssignedMembers().stream()
                    .anyMatch(member -> member.getId().equals(activity.getCreatedBy()));
            }
            
            if (!creatorAlreadyNotified) {
                System.out.println("Creating remark notification for activity creator (ID: " + activity.getCreatedBy() + ")");
                createNotification(
                    activity.getCreatedBy(),
                    "New Remark Added",
                    remarkAuthor.getName() + " added a remark to your activity '" + activity.getName() + "': " + 
                    (remarkText.length() > 100 ? remarkText.substring(0, 100) + "..." : remarkText),
                    "ACTIVITY_REMARK_ADDED",
                    null,
                    activity.getId()
                );
            }
        }
    }

    /**
     * Notify when remark is updated on activity
     */
    @Transactional
    public void notifyRemarkUpdated(Activity activity, User remarkAuthor, String remarkText) {
        System.out.println("notifyRemarkUpdated called for activity: " + activity.getName() + " by user: " + remarkAuthor.getName());
        
        if (activity.getAssignedMembers() != null) {
            System.out.println("Activity has " + activity.getAssignedMembers().size() + " assigned members");
            for (User member : activity.getAssignedMembers()) {
                if (!member.getId().equals(remarkAuthor.getId())) { // Don't notify remark author
                    System.out.println("Creating remark update notification for member: " + member.getName() + " (ID: " + member.getId() + ")");
                    createNotification(
                        member.getId(),
                        "Remark Updated",
                        remarkAuthor.getName() + " updated a remark on activity '" + activity.getName() + "': " + 
                        (remarkText.length() > 100 ? remarkText.substring(0, 100) + "..." : remarkText),
                        "ACTIVITY_REMARK_UPDATED",
                        null,
                        activity.getId()
                    );
                } else {
                    System.out.println("Skipping remark update notification for author: " + member.getName());
                }
            }
        } else {
            System.out.println("Activity has no assigned members for remark update notification");
        }

        // Also notify the activity creator if they're not the remark author and not already in assigned members
        if (!activity.getCreatedBy().equals(remarkAuthor.getId())) {
            boolean creatorAlreadyNotified = false;
            if (activity.getAssignedMembers() != null) {
                creatorAlreadyNotified = activity.getAssignedMembers().stream()
                    .anyMatch(member -> member.getId().equals(activity.getCreatedBy()));
            }
            
            if (!creatorAlreadyNotified) {
                System.out.println("Creating remark update notification for activity creator (ID: " + activity.getCreatedBy() + ")");
                createNotification(
                    activity.getCreatedBy(),
                    "Remark Updated",
                    remarkAuthor.getName() + " updated a remark on your activity '" + activity.getName() + "': " + 
                    (remarkText.length() > 100 ? remarkText.substring(0, 100) + "..." : remarkText),
                    "ACTIVITY_REMARK_UPDATED",
                    null,
                    activity.getId()
                );
            }
        }
    }
}