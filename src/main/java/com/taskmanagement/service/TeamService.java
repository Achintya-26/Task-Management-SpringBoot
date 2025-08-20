package com.taskmanagement.service;

import com.taskmanagement.model.Team;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.TeamRepository;
import com.taskmanagement.repository.UserRepository;

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
    private NotificationService notificationService;

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Optional<Team> getTeamById(Long id) {
        return teamRepository.findById(id);
    }

    public Team createTeam(Team team) {
        return teamRepository.save(team);
    }

    public Team updateTeam(Long id, Team teamDetails) {
        Team team = teamRepository.findById(id).orElseThrow(() -> new RuntimeException("Team not found"));
        team.setName(teamDetails.getName());
        // Update other fields as necessary
        return teamRepository.save(team);
    }

    public void deleteTeam(Long id) {
        teamRepository.deleteById(id);
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