package com.taskmanagement.service;

import com.taskmanagement.dto.ActivityDTO;
import com.taskmanagement.dto.CreateActivityRequest;
import com.taskmanagement.dto.UpdateActivityStatusRequest;
import com.taskmanagement.dto.AddRemarkRequest;
import com.taskmanagement.dto.RemarkDTO;
import com.taskmanagement.model.Activity;
import com.taskmanagement.model.Team;
import com.taskmanagement.model.User;
import com.taskmanagement.model.Remark;
import com.taskmanagement.repository.ActivityRepository;
import com.taskmanagement.repository.TeamRepository;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.repository.RemarkRepository;
import com.taskmanagement.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RemarkRepository remarkRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // Convert Remark entity to DTO
    private RemarkDTO convertRemarkToDTO(Remark remark) {
        RemarkDTO dto = new RemarkDTO();
        dto.setId(remark.getId());
        dto.setText(remark.getText());
        dto.setUserId(remark.getUserId());
        dto.setActivityId(remark.getActivityId());
        dto.setType(remark.getType().getValue());
        dto.setCreatedAt(remark.getCreatedAt());
        
        // Safely access user information
        try {
            if (remark.getUser() != null) {
                dto.setUserName(remark.getUser().getName());
                dto.setUserEmpId(remark.getUser().getEmpId());
            }
        } catch (Exception e) {
            System.err.println("Could not load user info for remark: " + e.getMessage());
        }
        
        return dto;
    }

    // Convert Activity entity to DTO
    private ActivityDTO convertToDTO(Activity activity) {
        ActivityDTO dto = new ActivityDTO();
        dto.setId(activity.getId());
        dto.setName(activity.getName());
        dto.setDescription(activity.getDescription());
        dto.setPriority(activity.getPriority());
        dto.setStatus(activity.getStatus() != null ? activity.getStatus().getValue() : "pending"); // Added status
        dto.setTargetDate(activity.getTargetDate());
        dto.setCreatedBy(activity.getCreatedBy());
        dto.setCreatedAt(activity.getCreatedAt());
        dto.setUpdatedAt(activity.getUpdatedAt());
        
        // Safely access team information
        if (activity.getTeam() != null) {
            dto.setTeamId(activity.getTeam().getId());
            dto.setTeamName(activity.getTeam().getName());
        }
        
        // Safely access creator information
        try {
            if (activity.getCreator() != null) {
                dto.setCreatorName(activity.getCreator().getName());
                dto.setCreatorEmpId(activity.getCreator().getEmpId());
            }
        } catch (Exception e) {
            System.err.println("Could not load creator info: " + e.getMessage());
        }
        
        // Convert assigned members
        try {
            if (activity.getAssignedMembers() != null && !activity.getAssignedMembers().isEmpty()) {
                List<ActivityDTO.AssignedMemberDTO> members = activity.getAssignedMembers()
                    .stream()
                    .map(user -> new ActivityDTO.AssignedMemberDTO(
                        user.getId(), 
                        user.getName(), 
                        user.getEmpId()
                    ))
                    .collect(Collectors.toList());
                dto.setAssignedMembers(members);
            }
        } catch (Exception e) {
            System.err.println("Could not load assigned members: " + e.getMessage());
        }
        
        // Convert remarks
        try {
            List<Remark> remarks = remarkRepository.findByActivityIdOrderByCreatedAtDesc(activity.getId());
            if (remarks != null && !remarks.isEmpty()) {
                List<RemarkDTO> remarkDTOs = remarks.stream()
                    .map(this::convertRemarkToDTO)
                    .collect(Collectors.toList());
                dto.setRemarks(remarkDTOs);
            }
        } catch (Exception e) {
            System.err.println("Could not load remarks: " + e.getMessage());
        }
        
        return dto;
    }

    public List<ActivityDTO> getAllActivities() {
        return activityRepository.findAllWithCreator()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ActivityDTO getActivityById(Long id) {
        Activity activity = activityRepository.findByIdWithCreator(id)
                .orElseThrow(() -> new RuntimeException("Activity not found with ID: " + id));
        return convertToDTO(activity);
    }

    public List<ActivityDTO> getActivitiesForTeam(Long teamId) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with ID: " + teamId));
        
        List<Activity> activities = activityRepository.findByTeamIdWithCreator(teamId);
        return activities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ActivityDTO createActivity(CreateActivityRequest request, String token) {
        Long currentUserId = jwtUtil.extractUserId(token);
        if (currentUserId == null) {
            throw new RuntimeException("Invalid authentication token");
        }

        Team team = teamRepository.findById(request.getTeam_id())
                .orElseThrow(() -> new RuntimeException("Team not found with ID: " + request.getTeam_id()));

        Activity activity = new Activity();
        activity.setName(request.getName());
        activity.setDescription(request.getDescription());
        activity.setPriority(request.getPriority());
        activity.setTeam(team);
        activity.setCreatedBy(currentUserId);
        activity.setStatus(Activity.ActivityStatus.PENDING); // Set default status
        
        LocalDateTime now = LocalDateTime.now();
        activity.setCreatedAt(now);
        activity.setUpdatedAt(now);
        
        if (request.getTargetDate() != null && !request.getTargetDate().isEmpty()) {
            try {
                LocalDateTime targetDate = LocalDateTime.parse(request.getTargetDate(), 
                    DateTimeFormatter.ISO_DATE_TIME);
                activity.setTargetDate(targetDate);
            } catch (Exception e) {
                System.err.println("Error parsing target date: " + e.getMessage());
            }
        }

        if (request.getAssignedUsers() != null && !request.getAssignedUsers().isEmpty()) {
            Set<User> assignedUsers = new HashSet<>();
            for (Long userId : request.getAssignedUsers()) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    assignedUsers.add(user);
                }
            }
            activity.setAssignedMembers(assignedUsers);
        }

        Activity savedActivity = activityRepository.save(activity);
        return convertToDTO(savedActivity);
    }

    @Transactional
    public ActivityDTO updateActivityStatus(Long activityId, UpdateActivityStatusRequest request, String token) {
        Long currentUserId = jwtUtil.extractUserId(token);
        if (currentUserId == null) {
            throw new RuntimeException("Invalid authentication token");
        }

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found with ID: " + activityId));

        // Check if user has permission to update (admin, creator, or assigned member)
        boolean canUpdate = isUserAdmin(currentUserId) || 
                           activity.getCreatedBy().equals(currentUserId) ||
                           isUserAssignedToActivity(activity, currentUserId);
        
        if (!canUpdate) {
            throw new RuntimeException("You do not have permission to update this activity");
        }

        try {
            Activity.ActivityStatus newStatus = Activity.ActivityStatus.fromValue(request.getStatus());
            activity.setStatus(newStatus);
            activity.setUpdatedAt(LocalDateTime.now());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status value: " + request.getStatus());
        }

        // TODO: Add remark to activity history if remarks provided
        if (request.getRemarks() != null && !request.getRemarks().trim().isEmpty()) {
            // Save remark to remarks table with status update type
            Remark statusRemark = new Remark(
                request.getRemarks().trim(), 
                currentUserId, 
                activityId, 
                Remark.RemarkType.STATUS_UPDATE
            );
            remarkRepository.save(statusRemark);
        }

        Activity savedActivity = activityRepository.save(activity);
        return convertToDTO(savedActivity);
    }

    @Transactional
    public ActivityDTO addRemarkToActivity(Long activityId, AddRemarkRequest request, String token) {
        Long currentUserId = jwtUtil.extractUserId(token);
        if (currentUserId == null) {
            throw new RuntimeException("Invalid authentication token");
        }

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found with ID: " + activityId));

        // Check if user has permission (admin, creator, or assigned member)
        boolean canAddRemark = isUserAdmin(currentUserId) || 
                              activity.getCreatedBy().equals(currentUserId) ||
                              isUserAssignedToActivity(activity, currentUserId);
        
        if (!canAddRemark) {
            throw new RuntimeException("You do not have permission to add remarks to this activity");
        }

        // TODO: Save remark to remarks table
        // Save remark with general type
        Remark newRemark = new Remark(
            request.getText().trim(), 
            currentUserId, 
            activityId, 
            Remark.RemarkType.GENERAL
        );
        remarkRepository.save(newRemark);
        
        // Update activity timestamp
        activity.setUpdatedAt(LocalDateTime.now());

        Activity savedActivity = activityRepository.save(activity);
        return convertToDTO(savedActivity);
    }

    public List<RemarkDTO> getRemarksForActivity(Long activityId) {
        // Verify activity exists
        activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found with ID: " + activityId));
        
        List<Remark> remarks = remarkRepository.findByActivityIdOrderByCreatedAtDesc(activityId);
        return remarks.stream()
                .map(this::convertRemarkToDTO)
                .collect(Collectors.toList());
    }

    private boolean isUserAdmin(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null && "ADMIN".equals(user.getRole());
    }

    private boolean isUserAssignedToActivity(Activity activity, Long userId) {
        if (activity.getAssignedMembers() == null) return false;
        
        try {
            return activity.getAssignedMembers().stream()
                    .anyMatch(user -> user.getId().equals(userId));
        } catch (Exception e) {
            // Handle lazy loading exception
            return false;
        }
    }
}