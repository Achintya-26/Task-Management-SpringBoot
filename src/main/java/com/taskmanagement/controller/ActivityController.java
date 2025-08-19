package com.taskmanagement.controller;

import com.taskmanagement.dto.*;
import com.taskmanagement.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> createActivityWithFiles(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("priority") String priority,
            @RequestParam("team_id") Long teamId,
            @RequestParam(value = "targetDate", required = false) String targetDate,
            @RequestParam(value = "assignedUsers", required = false) String assignedUsersJson,
            @RequestParam(value = "links", required = false) String linksJson,
            @RequestParam(value = "attachments", required = false) MultipartFile[] attachments,
            HttpServletRequest httpRequest) {
        try {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Authentication required");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            String token = authHeader.substring(7);
            
            // Create request object
            CreateActivityWithFilesRequest request = new CreateActivityWithFilesRequest();
            request.setName(name);
            request.setDescription(description);
            request.setPriority(priority);
            request.setTeamId(teamId);
            request.setTargetDate(targetDate);
            request.setAssignedUsersJson(assignedUsersJson);
            request.setLinksJson(linksJson);
            request.setAttachments(attachments);
            
            ActivityDTO activity = activityService.createActivityWithFiles(request, token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Activity created successfully with attachments");
            response.put("activity", activity);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception error) {
            System.err.println("Create activity with files error: " + error.getMessage());
            error.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error creating activity with files: " + error.getMessage());
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

    // Update remark
    @PutMapping("/remarks/{remarkId}")
    public ResponseEntity<Map<String, Object>> updateRemark(
            @PathVariable Long remarkId,
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
            RemarkDTO remark = activityService.updateRemark(remarkId, request, token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Remark updated successfully");
            response.put("remark", remark);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException error) {
            Map<String, Object> errorResponse = new HashMap<>();
            if (error.getMessage().contains("not found")) {
                errorResponse.put("message", "Remark not found");
                return ResponseEntity.status(404).body(errorResponse);
            } else if (error.getMessage().contains("not authorized")) {
                errorResponse.put("message", "You are not authorized to edit this remark");
                return ResponseEntity.status(403).body(errorResponse);
            } else {
                errorResponse.put("message", error.getMessage());
                return ResponseEntity.status(400).body(errorResponse);
            }
        } catch (Exception error) {
            System.err.println("Update remark error: " + error.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Internal server error");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Delete remark
    @DeleteMapping("/remarks/{remarkId}")
    public ResponseEntity<Map<String, Object>> deleteRemark(
            @PathVariable Long remarkId,
            HttpServletRequest httpRequest) {
        try {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Authentication required");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            String token = authHeader.substring(7);
            activityService.deleteRemark(remarkId, token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Remark deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException error) {
            Map<String, Object> errorResponse = new HashMap<>();
            if (error.getMessage().contains("not found")) {
                errorResponse.put("message", "Remark not found");
                return ResponseEntity.status(404).body(errorResponse);
            } else if (error.getMessage().contains("not authorized")) {
                errorResponse.put("message", "You are not authorized to delete this remark");
                return ResponseEntity.status(403).body(errorResponse);
            } else {
                errorResponse.put("message", error.getMessage());
                return ResponseEntity.status(400).body(errorResponse);
            }
        } catch (Exception error) {
            System.err.println("Delete remark error: " + error.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Internal server error");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

//    @PutMapping("/{activityId}")
//    public ResponseEntity<Map<String, Object>> updateActivity(
//            @PathVariable Long activityId,
//            @RequestBody CreateActivityRequest request,
//            HttpServletRequest httpRequest) {
//        try {
//            String authHeader = httpRequest.getHeader("Authorization");
//            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                Map<String, Object> errorResponse = new HashMap<>();
//                errorResponse.put("message", "Authentication required");
//                return ResponseEntity.status(401).body(errorResponse);
//            }
//            
//            String token = authHeader.substring(7);
//            ActivityDTO activity = activityService.updateActivity(activityId, request, token);
//            
//            Map<String, Object> response = new HashMap<>();
//            response.put("message", "Activity updated successfully");
//            response.put("activity", activity);
//            
//            return ResponseEntity.ok(response);
//            
//        } catch (RuntimeException error) {
//            Map<String, Object> errorResponse = new HashMap<>();
//            if (error.getMessage().contains("not found")) {
//                errorResponse.put("message", "Activity not found");
//                return ResponseEntity.status(404).body(errorResponse);
//            } else if (error.getMessage().contains("permission") || error.getMessage().contains("not authorized")) {
//                errorResponse.put("message", error.getMessage());
//                return ResponseEntity.status(403).body(errorResponse);
//            } else {
//                System.err.println("Update activity error: " + error.getMessage());
//                errorResponse.put("message", "Error updating activity: " + error.getMessage());
//                return ResponseEntity.status(500).body(errorResponse);
//            }
//        } catch (Exception error) {
//            System.err.println("Update activity error: " + error.getMessage());
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("message", "Error updating activity: " + error.getMessage());
//            return ResponseEntity.status(500).body(errorResponse);
//        }
//    }

    @PutMapping(value = "/{activityId}", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> updateActivityWithFiles(
            @PathVariable Long activityId,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("priority") String priority,
            @RequestParam(value = "targetDate", required = false) String targetDate,
            @RequestParam(value = "assignedUsers", required = false) String assignedUsersJson,
            @RequestParam(value = "newLinks", required = false) String newLinksJson,
            @RequestParam(value = "attachments", required = false) MultipartFile[] attachments,
            @RequestParam(value = "attachmentsToDelete", required = false) String attachmentsToDeleteJson,
            @RequestParam(value = "linksToDelete", required = false) String linksToDeleteJson,
            HttpServletRequest httpRequest) {
        try {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Authentication required");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            String token = authHeader.substring(7);
            
            // Debug logging
            System.out.println("=== UPDATE ACTIVITY WITH FILES DEBUG ===");
            System.out.println("attachmentsToDeleteJson received: " + attachmentsToDeleteJson);
            System.out.println("linksToDeleteJson received: " + linksToDeleteJson);
            System.out.println("========================================");
            
            // Create request object
            UpdateActivityWithFilesRequest request = new UpdateActivityWithFilesRequest();
            request.setName(name);
            request.setDescription(description);
            request.setPriority(priority);
            request.setTargetDate(targetDate);
            request.setAssignedUsersJson(assignedUsersJson);
            request.setNewLinksJson(newLinksJson);
            request.setAttachments(attachments);
            request.setAttachmentsToDeleteJson(attachmentsToDeleteJson);
            request.setLinksToDeleteJson(linksToDeleteJson);
            
            ActivityDTO activity = activityService.updateActivityWithFiles(activityId, request, token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Activity updated successfully");
            response.put("activity", activity);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException error) {
            Map<String, Object> errorResponse = new HashMap<>();
            if (error.getMessage().contains("not found")) {
                errorResponse.put("message", "Activity not found");
                return ResponseEntity.status(404).body(errorResponse);
            } else if (error.getMessage().contains("permission") || error.getMessage().contains("not authorized")) {
                errorResponse.put("message", error.getMessage());
                return ResponseEntity.status(403).body(errorResponse);
            } else {
                System.err.println("Update activity with files error: " + error.getMessage());
                error.printStackTrace();
                errorResponse.put("message", "Error updating activity: " + error.getMessage());
                return ResponseEntity.status(500).body(errorResponse);
            }
        } catch (Exception error) {
            System.err.println("Update activity with files error: " + error.getMessage());
            error.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error updating activity: " + error.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping("/files/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error downloading file: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}