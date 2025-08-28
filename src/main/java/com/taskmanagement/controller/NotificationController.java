package com.taskmanagement.controller;

import com.taskmanagement.dto.CreateNotificationRequest;
import com.taskmanagement.model.Notification;
import com.taskmanagement.service.NotificationService;
import com.taskmanagement.service.AuthService;
import com.taskmanagement.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Get all notifications for current user
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            List<Notification> notifications = notificationService.getNotificationsByUserId(userId);
            return ResponseEntity.ok(notifications);
            
        } catch (Exception error) {
            System.err.println("Get notifications error: " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get unread notifications for current user
     */
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            List<Notification> notifications = notificationService.getUnreadNotificationsByUserId(userId);
            return ResponseEntity.ok(notifications);
            
        } catch (Exception error) {
            System.err.println("Get unread notifications error: " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get notification counts
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getNotificationCounts(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Map<String, Long> counts = new HashMap<>();
            counts.put("total", notificationService.getNotificationCount(userId));
            counts.put("unread", notificationService.getUnreadNotificationCount(userId));
            
            return ResponseEntity.ok(counts);
            
        } catch (Exception error) {
            System.err.println("Get notification counts error: " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long notificationId, 
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Notification notification = notificationService.markAsRead(notificationId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Notification marked as read");
            response.put("notification", notification);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException error) {
            Map<String, Object> errorResponse = new HashMap<>();
            if (error.getMessage().contains("not found")) {
                errorResponse.put("message", "Notification not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            } else if (error.getMessage().contains("Access denied")) {
                errorResponse.put("message", "Access denied");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            } else {
                errorResponse.put("message", "Internal server error");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }
    }

    /**
     * Mark all notifications as read for current user
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            notificationService.markAllAsReadForUser(userId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "All notifications marked as read");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception error) {
            System.err.println("Mark all as read error: " + error.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete notification
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @PathVariable Long notificationId, 
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            boolean deleted = notificationService.deleteNotification(notificationId, userId);
            
            Map<String, String> response = new HashMap<>();
            if (deleted) {
                response.put("message", "Notification deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Notification not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (RuntimeException error) {
            Map<String, String> errorResponse = new HashMap<>();
            if (error.getMessage().contains("Access denied")) {
                errorResponse.put("message", "Access denied");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            } else {
                errorResponse.put("message", "Internal server error");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }
    }

    /**
     * Get notifications by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Notification>> getNotificationsByType(
            @PathVariable String type, 
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            List<Notification> notifications = notificationService.getNotificationsByType(userId, type);
            return ResponseEntity.ok(notifications);
            
        } catch (Exception error) {
            System.err.println("Get notifications by type error: " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get notifications related to a specific team
     */
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<Notification>> getNotificationsByTeam(
            @PathVariable Long teamId, 
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            List<Notification> notifications = notificationService.getNotificationsByTeam(userId, teamId);
            return ResponseEntity.ok(notifications);
            
        } catch (Exception error) {
            System.err.println("Get team notifications error: " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get notifications related to a specific activity
     */
    @GetMapping("/activity/{activityId}")
    public ResponseEntity<List<Notification>> getNotificationsByActivity(
            @PathVariable Long activityId, 
            HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            List<Notification> notifications = notificationService.getNotificationsByActivity(userId, activityId);
            return ResponseEntity.ok(notifications);
            
        } catch (Exception error) {
            System.err.println("Get activity notifications error: " + error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Admin endpoint: Create notification for specific user
     */
    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createNotification(@RequestBody CreateNotificationRequest request) {
        try {
            Notification notification = notificationService.createNotification(
                request.getUserId(),
                request.getTitle(),
                request.getMessage(),
                request.getType(),
                request.getRelatedTeamId(),
                request.getRelatedActivityId()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Notification created successfully");
            response.put("notification", notification);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception error) {
            System.err.println("Create notification error: " + error.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Admin endpoint: Clean up notifications for specific user
     */
    @DeleteMapping("/admin/cleanup/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cleanupUserNotifications(@PathVariable Long userId) {
        try {
            int deletedCount = notificationService.cleanupNotificationsForUser(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User notification cleanup completed");
            response.put("userId", userId);
            response.put("deletedCount", deletedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception error) {
            System.err.println("User notification cleanup error: " + error.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Admin endpoint: Clean up notifications for all users (enforce 50-notification limit)
     */
    @DeleteMapping("/admin/cleanup/all-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cleanupAllUsersNotifications() {
        try {
            notificationService.cleanupNotificationsForAllUsers();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Notification cleanup completed for all users. Each user now has maximum " + 
                        notificationService.getMaxNotificationsPerUser() + " notifications.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception error) {
            System.err.println("All users notification cleanup error: " + error.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Admin endpoint: Clean up old notifications
     */
    @DeleteMapping("/admin/cleanup/{daysOld}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cleanupOldNotifications(@PathVariable int daysOld) {
        try {
            int deletedCount = notificationService.deleteOldNotifications(daysOld);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cleanup completed");
            response.put("deletedCount", deletedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception error) {
            System.err.println("Cleanup notifications error: " + error.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Helper method to extract current user ID from JWT token
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.validateToken(token)) {
                    return jwtUtil.extractUserId(token);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Create a test notification (for testing WebSocket real-time notifications)
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> createTestNotification(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            Notification notification = notificationService.createNotification(
                userId,
                "Test Notification",
                "This is a test notification to verify the real-time notification system is working correctly.",
                "TEST",
                null, // relatedTeamId
                null  // relatedActivityId
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Test notification created and sent via WebSocket");
            response.put("notification", notification);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Create test notification error: " + e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to create test notification");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}