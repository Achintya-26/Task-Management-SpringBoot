package com.taskmanagement.controller;

import com.taskmanagement.dto.AddMembersRequest;
import com.taskmanagement.model.Team;
import com.taskmanagement.service.NotificationService;
import com.taskmanagement.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams() {
        List<Team> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Team>> getTeamById(@PathVariable Long id) {
        Optional<Team> team = teamService.getTeamById(id);
        return ResponseEntity.ok(team);
    }

    @PostMapping
    public ResponseEntity<Team> createTeam(@RequestBody Team team) {
        Team createdTeam = teamService.createTeam(team);
        return ResponseEntity.status(201).body(createdTeam);
    }
    
    @PostMapping("/{teamId}/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> addMembersToTeam(
            @PathVariable Long teamId, 
            @RequestBody AddMembersRequest request) {
        
        try {
            Team team = teamService.findById(teamId);
            if (team == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Team not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            // Add users to team
            for (Long userId : request.getUserIds()) {
                try {
                    teamService.addMember(teamId, userId);
                    
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
            @PathVariable Long userId) {
        
        try {
            Team team = teamService.findById(teamId);
            if (team == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Team not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            boolean removed = teamService.removeMember(teamId, userId);
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
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }
}