package com.taskmanagement.controller;

import com.taskmanagement.dto.AddMembersRequest;
import com.taskmanagement.dto.CreateTeamRequest;
import com.taskmanagement.model.Team;
import com.taskmanagement.service.NotificationService;
import com.taskmanagement.service.TeamService;
import com.taskmanagement.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class TeamController {

    @Autowired
    private TeamService teamService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }
        
        // Check if user is admin
        if (isUserAdmin(request)) {
            // Admin can see all teams
            List<Team> teams = teamService.getAllTeams();
            return ResponseEntity.ok(teams);
        } else {
            // Regular user can only see their teams
            List<Team> userTeams = teamService.getUserTeams(currentUserId);
            return ResponseEntity.ok(userTeams);
        }
    }
    
    @GetMapping("/my-teams")
    public ResponseEntity<List<Team>> getMyTeams(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }
        
        List<Team> userTeams = teamService.getUserTeams(currentUserId);
        return ResponseEntity.ok(userTeams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable Long id) {
        Optional<Team> team = teamService.getTeamById(id);
        if (team.isPresent()) {
            return ResponseEntity.ok(team.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Team> createTeam(@RequestBody CreateTeamRequest request, HttpServletRequest httpRequest) {
        // Get current user ID from JWT token
        Long currentUserId = getCurrentUserId(httpRequest);
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }
        
        Team createdTeam = teamService.createTeamWithMembers(request, currentUserId);
        return ResponseEntity.status(201).body(createdTeam);
    }
    
    @PostMapping("/{teamId}/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> addMembersToTeam(
            @PathVariable Long teamId, 
            @RequestBody AddMembersRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            // Get current user ID from JWT token
            Long currentUserId = getCurrentUserId(httpRequest);
            if (currentUserId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Authentication required");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            Team team = teamService.findById(teamId);
            if (team == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Team not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            // Add users to team
            for (Long userId : request.getUserIds()) {
                try {
                    teamService.addMember(teamId, userId, currentUserId);
                    
                    // Create notification for user
                    notificationService.createNotification(
                        userId,
                        "Added to Team",
                        "You have been added to team \"" + team.getName() + "\"",
                        "info",
                        teamId,
                        null
                    );
                } catch (Exception error) {
                    if (error.getMessage().contains("already a member")) {
                        continue; // Skip if user is already a member
                    }
                    throw error;
                }
            }

            // Get updated team with members
            Team updatedTeam = teamService.getTeamWithMembers(teamId);

            // TODO: Emit socket event (implement WebSocket later)
            // socketService.emitToTeam(teamId, "team_updated", updatedTeam);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Members added successfully");
            response.put("team", updatedTeam);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception error) {
            System.err.println("Add team member error: " + error.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Internal server error");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Remove member from team (admin only)
    @DeleteMapping("/{teamId}/members/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> removeMemberFromTeam(
            @PathVariable Long teamId, 
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {
        
        try {
            // Get current user ID from JWT token
            Long currentUserId = getCurrentUserId(httpRequest);
            if (currentUserId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Authentication required");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            Team team = teamService.findById(teamId);
            if (team == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Team not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            boolean removed = teamService.removeMember(teamId, userId, currentUserId);
            if (!removed) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "User is not a member of this team");
                return ResponseEntity.status(404).body(errorResponse);
            }

            // Create notification for removed user
            notificationService.createNotification(
                userId,
                "Removed from Team",
                "You have been removed from team \"" + team.getName() + "\"",
                "warning",
                teamId,
                null
            );

            // Get updated team with members
            Team updatedTeam = teamService.getTeamWithMembers(teamId);

            // TODO: Emit socket event (implement WebSocket later)
            // socketService.emitToTeam(teamId, "team_updated", updatedTeam);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Member removed successfully");
            response.put("team", updatedTeam);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception error) {
            System.err.println("Remove team member error: " + error.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Internal server error");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Team> updateTeam(@PathVariable Long id, @RequestBody Team team) {
        Team updatedTeam = teamService.updateTeam(id, team);
        return ResponseEntity.ok(updatedTeam);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTeam(@PathVariable Long id) {
        try {
            teamService.deleteTeam(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Team deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException error) {
            Map<String, Object> errorResponse = new HashMap<>();
            if (error.getMessage().contains("not found")) {
                errorResponse.put("message", "Team not found");
                return ResponseEntity.status(404).body(errorResponse);
            } else if (error.getMessage().contains("dependencies")) {
                errorResponse.put("message", "Cannot delete team due to existing dependencies. Please remove all related data first.");
                return ResponseEntity.status(409).body(errorResponse);
            } else {
                errorResponse.put("message", error.getMessage());
                return ResponseEntity.status(400).body(errorResponse);
            }
        } catch (Exception error) {
            System.err.println("Delete team error: " + error.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Internal server error");
            return ResponseEntity.status(500).body(errorResponse);
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
     * Helper method to check if current user is admin
     */
    private boolean isUserAdmin(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.validateToken(token)) {
                    String role = jwtUtil.extractRole(token);
                    return "ADMIN".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(role);
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}