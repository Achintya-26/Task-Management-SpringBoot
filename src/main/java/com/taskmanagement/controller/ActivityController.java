package com.taskmanagement.controller;

import com.taskmanagement.dto.*;
import com.taskmanagement.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "http://localhost:4200")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @GetMapping
    public ResponseEntity<List<ActivityDTO>> getAllActivities() {
        try {
            List<ActivityDTO> activities = activityService.getAllActivities();
            return ResponseEntity.ok(activities);
        } catch (Exception error) {
            System.err.println("Get activities error: " + error.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityDTO> getActivityById(@PathVariable Long activityId) {
        try {
            ActivityDTO activity = activityService.getActivityById(activityId);
            return ResponseEntity.ok(activity);
        } catch (RuntimeException error) {
            if (error.getMessage().contains("not found")) {
                return ResponseEntity.status(404).build();
            }
            System.err.println("Get activity by ID error: " + error.getMessage());
            return ResponseEntity.status(500).build();
        } catch (Exception error) {
            System.err.println("Get activity by ID error: " + error.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<ActivityDTO>> getActivitiesForTeam(@PathVariable Long teamId) {
        try {
            List<ActivityDTO> activities = activityService.getActivitiesForTeam(teamId);
            return ResponseEntity.ok(activities);
        } catch (Exception error) {
            System.err.println("Get activities for team error: " + error.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createActivity(
            @RequestBody CreateActivityRequest request,
            HttpServletRequest httpRequest) {
        try {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Authentication required");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            String token = authHeader.substring(7);
            ActivityDTO activity = activityService.createActivity(request, token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Activity created successfully");
            response.put("activity", activity);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception error) {
            System.err.println("Create activity error: " + error.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error creating activity: " + error.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Update activity status
    @PatchMapping("/{activityId}/status")
    public ResponseEntity<Map<String, Object>> updateActivityStatus(
            @PathVariable Long activityId,
            @RequestBody UpdateActivityStatusRequest request,
            HttpServletRequest httpRequest) {
        try {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Authentication required");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            String token = authHeader.substring(7);
            ActivityDTO activity = activityService.updateActivityStatus(activityId, request, token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Activity status updated successfully");
            response.put("activity", activity);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException error) {
            Map<String, Object> errorResponse = new HashMap<>();
            if (error.getMessage().contains("not found")) {
                errorResponse.put("message", "Activity not found");
                return ResponseEntity.status(404).body(errorResponse);
            } else if (error.getMessage().contains("permission")) {
                errorResponse.put("message", error.getMessage());
                return ResponseEntity.status(403).body(errorResponse);
            } else if (error.getMessage().contains("Invalid status")) {
                errorResponse.put("message", error.getMessage());
                return ResponseEntity.status(400).body(errorResponse);
            } else {
                errorResponse.put("message", "Internal server error");
                return ResponseEntity.status(500).body(errorResponse);
            }
        } catch (Exception error) {
            System.err.println("Update activity status error: " + error.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Internal server error");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Add remark to activity
    @PostMapping("/{activityId}/remarks")
    public ResponseEntity<Map<String, Object>> addRemarkToActivity(
            @PathVariable Long activityId,
            @RequestBody AddRemarkRequest request,
            HttpServletRequest httpRequest) {
        try {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Authentication required");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            String token = authHeader.substring(7);
            ActivityDTO activity = activityService.addRemarkToActivity(activityId, request, token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Remark added successfully");
            response.put("activity", activity);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException error) {
            Map<String, Object> errorResponse = new HashMap<>();
            if (error.getMessage().contains("not found")) {
                errorResponse.put("message", "Activity not found");
                return ResponseEntity.status(404).body(errorResponse);
            } else if (error.getMessage().contains("permission")) {
                errorResponse.put("message", error.getMessage());
                return ResponseEntity.status(403).body(errorResponse);
            } else {
                errorResponse.put("message", "Internal server error");
                return ResponseEntity.status(500).body(errorResponse);
            }
        } catch (Exception error) {
            System.err.println("Add remark error: " + error.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Internal server error");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Get remarks for activity
    @GetMapping("/{activityId}/remarks")
    public ResponseEntity<List<RemarkDTO>> getRemarksForActivity(@PathVariable Long activityId) {
        try {
            List<RemarkDTO> remarks = activityService.getRemarksForActivity(activityId);
            return ResponseEntity.ok(remarks);
        } catch (RuntimeException error) {
            if (error.getMessage().contains("not found")) {
                return ResponseEntity.status(404).build();
            }
            System.err.println("Get remarks error: " + error.getMessage());
            return ResponseEntity.status(500).build();
        } catch (Exception error) {
            System.err.println("Get remarks error: " + error.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}