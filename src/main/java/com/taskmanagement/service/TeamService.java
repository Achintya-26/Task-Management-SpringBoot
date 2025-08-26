package com.taskmanagement.service;

import com.taskmanagement.model.Team;
import com.taskmanagement.model.User;
import com.taskmanagement.model.Activity;
import com.taskmanagement.repository.TeamRepository;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.repository.ActivityRepository;
import com.taskmanagement.repository.NotificationRepository;
import com.taskmanagement.dto.CreateTeamRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    // Note: We'll use a different approach to avoid circular dependency
    // private ActivityService activityService;

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }
    
    public List<Team> getUserTeams(Long userId) {
        return teamRepository.findTeamsByUserId(userId);
    }

    public Optional<Team> getTeamById(Long id) {
        return teamRepository.findById(id);
    }

    public Team createTeam(Team team) {
        return teamRepository.save(team);
    }
    
    @Transactional
    public Team createTeamWithMembers(CreateTeamRequest request, Long createdBy) {
        // Create the team
        Team team = new Team();
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setDomainId(request.getDomainId());
        team.setCreatedBy(createdBy);
        
        // Save the team first
        team = teamRepository.save(team);
        
        // Add initial members if provided
        if (request.getInitialMembers() != null && !request.getInitialMembers().isEmpty()) {
            for (Long userId : request.getInitialMembers()) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    team.getMembers().add(user);
                }
            }
            // Save again with members
            team = teamRepository.save(team);
        }
        
        return team;
    }

    public Team updateTeam(Long id, Team teamDetails) {
        Team team = teamRepository.findById(id).orElseThrow(() -> new RuntimeException("Team not found"));
        team.setName(teamDetails.getName());
        // Update other fields as necessary
        return teamRepository.save(team);
    }

    @Transactional
    public void deleteTeam(Long id) {
        try {
            // Get the team first to check if it exists
            Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
            
            // Step 1: Delete all notifications related to this team
            notificationRepository.deleteByRelatedTeamId(id);
            
            // Step 2: Handle activities - Get all activities for this team
            List<Activity> teamActivities = activityRepository.findByTeamId(id);
            
            // For each activity, we need to clean up its dependencies manually
            for (Activity activity : teamActivities) {
                // Delete notifications related to this activity
                notificationRepository.deleteByRelatedActivityId(activity.getId());
                
                // Clear assigned members relationship (many-to-many)
                activity.getAssignedMembers().clear();
                activityRepository.save(activity);
            }
            
            // Step 3: Now delete all activities (cascade should handle remarks, attachments, links)
            activityRepository.deleteByTeamId(id);
            
            // Step 4: Clear all team member relationships (many-to-many)
            team.getMembers().clear();
            teamRepository.save(team);
            
            // Step 5: Finally delete the team
            teamRepository.delete(team);
            
        } catch (Exception e) {
            System.err.println("Error deleting team: " + e.getMessage());
            e.printStackTrace();
            if (e.getMessage().contains("constraint") || 
                e.getMessage().contains("ConstraintViolationException")) {
                throw new RuntimeException("Cannot delete team due to existing dependencies. All related items have been cleaned up, please try again.");
            }
            throw new RuntimeException("Failed to delete team: " + e.getMessage());
        }
    }
    
    @Transactional
    public void addMember(Long teamId, Long userId, Long addedById) {
        Team team = teamRepository.findById(teamId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);
        User addedBy = userRepository.findById(addedById).orElse(null);
        
        if (team == null || user == null || addedBy == null) {
            throw new RuntimeException("Team, User, or AddedBy user not found");
        }

        // Check if user is already a member
        if (team.getMembers().contains(user)) {
            throw new RuntimeException("User is already a member of this team");
        }

        team.getMembers().add(user);
        teamRepository.save(team);
        
        // Send notification to the added user
        try {
            notificationService.notifyUserAddedToTeam(team, user, addedBy);
        } catch (Exception e) {
            System.err.println("Failed to send team member addition notification: " + e.getMessage());
        }
    }

    @Transactional
    public boolean removeMember(Long teamId, Long userId, Long removedById) {
        Team team = teamRepository.findById(teamId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);
        User removedBy = userRepository.findById(removedById).orElse(null);
        
        if (team == null || user == null || removedBy == null) {
            return false;
        }

        boolean removed = team.getMembers().remove(user);
        if (removed) {
            teamRepository.save(team);
            
            // Send notification to the removed user
            try {
                notificationService.notifyUserRemovedFromTeam(team, user, removedBy);
            } catch (Exception e) {
                System.err.println("Failed to send team member removal notification: " + e.getMessage());
            }
        }
        
        return removed;
    }

    public Team getTeamWithMembers(Long teamId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team != null) {
            // Ensure members are loaded (lazy loading)
            team.getMembers().size(); // This triggers the lazy loading
        }
        return team;
    }

    public Team findById(Long teamId) {
        return teamRepository.findById(teamId).orElse(null);
    }
}