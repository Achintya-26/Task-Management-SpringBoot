package com.taskmanagement.service;

import com.taskmanagement.dto.ActivityDTO;
import com.taskmanagement.dto.CreateActivityRequest;
import com.taskmanagement.dto.CreateActivityWithFilesRequest;
import com.taskmanagement.dto.UpdateActivityWithFilesRequest;
import com.taskmanagement.dto.UpdateActivityStatusRequest;
import com.taskmanagement.dto.AddRemarkRequest;
import com.taskmanagement.dto.RemarkDTO;
import com.taskmanagement.model.Activity;
import com.taskmanagement.model.Team;
import com.taskmanagement.model.User;
import com.taskmanagement.model.Remark;
import com.taskmanagement.model.Attachment;
import com.taskmanagement.model.ActivityLink;
import com.taskmanagement.repository.ActivityRepository;
import com.taskmanagement.repository.TeamRepository;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.repository.RemarkRepository;
import com.taskmanagement.repository.AttachmentRepository;
import com.taskmanagement.repository.ActivityLinkRepository;
import com.taskmanagement.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.transaction.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
    private AttachmentRepository attachmentRepository;

    @Autowired
    private ActivityLinkRepository activityLinkRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${upload.dir:uploads}")
    private String uploadDir;

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
        
        // Convert attachments
        try {
            if (activity.getAttachments() != null && !activity.getAttachments().isEmpty()) {
                List<ActivityDTO.AttachmentDTO> attachmentDTOs = activity.getAttachments()
                    .stream()
                    .map(attachment -> new ActivityDTO.AttachmentDTO(
                        attachment.getId(),
                        attachment.getFilename(),
                        attachment.getOriginalName(),
                        attachment.getFilePath(),
                        attachment.getFileSize(),
                        attachment.getContentType(),
                        attachment.getUploadedAt()
                    ))
                    .collect(Collectors.toList());
                dto.setAttachments(attachmentDTOs);
            }
        } catch (Exception e) {
            System.err.println("Could not load attachments: " + e.getMessage());
        }
        
        // Convert links
        try {
            if (activity.getLinks() != null && !activity.getLinks().isEmpty()) {
                List<ActivityDTO.ActivityLinkDTO> linkDTOs = activity.getLinks()
                    .stream()
                    .map(link -> new ActivityDTO.ActivityLinkDTO(
                        link.getId(),
                        link.getUrl(),
                        link.getTitle(),
                        link.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
                dto.setLinks(linkDTOs);
            }
        } catch (Exception e) {
            System.err.println("Could not load links: " + e.getMessage());
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

        // Always add the creator to the assigned members list
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Creator user not found"));
        
        Set<User> assignedMembers = activity.getAssignedMembers();
        if (assignedMembers == null) {
            assignedMembers = new HashSet<>();
        }
        assignedMembers.add(creator);
        activity.setAssignedMembers(assignedMembers);

        Activity savedActivity = activityRepository.save(activity);
        return convertToDTO(savedActivity);
    }

    @Transactional
    public ActivityDTO createActivityWithFiles(CreateActivityWithFilesRequest request, String token) {
        Long currentUserId = jwtUtil.extractUserId(token);
        if (currentUserId == null) {
            throw new RuntimeException("Invalid authentication token");
        }

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found with ID: " + request.getTeamId()));

        // Create the activity
        Activity activity = new Activity();
        activity.setName(request.getName());
        activity.setDescription(request.getDescription());
        activity.setPriority(request.getPriority());
        activity.setTeam(team);
        activity.setCreatedBy(currentUserId);
        activity.setStatus(Activity.ActivityStatus.PENDING);
        
        LocalDateTime now = LocalDateTime.now();
        activity.setCreatedAt(now);
        activity.setUpdatedAt(now);
        
        // Parse and set target date
        if (request.getTargetDate() != null && !request.getTargetDate().isEmpty()) {
            try {
                LocalDateTime targetDate = LocalDateTime.parse(request.getTargetDate(), 
                    DateTimeFormatter.ISO_DATE_TIME);
                activity.setTargetDate(targetDate);
            } catch (Exception e) {
                System.err.println("Error parsing target date: " + e.getMessage());
            }
        }

        // Parse and set assigned users
        if (request.getAssignedUsersJson() != null && !request.getAssignedUsersJson().isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<Long> assignedUserIds = objectMapper.readValue(
                    request.getAssignedUsersJson(), 
                    new TypeReference<List<Long>>(){}
                );
                
                Set<User> assignedUsers = new HashSet<>();
                for (Long userId : assignedUserIds) {
                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        assignedUsers.add(user);
                    }
                }
                activity.setAssignedMembers(assignedUsers);
            } catch (Exception e) {
                System.err.println("Error parsing assigned users: " + e.getMessage());
            }
        }

        // Always add the creator to the assigned members list
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Creator user not found"));
        
        Set<User> assignedMembers = activity.getAssignedMembers();
        if (assignedMembers == null) {
            assignedMembers = new HashSet<>();
        }
        assignedMembers.add(creator);
        activity.setAssignedMembers(assignedMembers);

        // Save the activity first to get its ID
        Activity savedActivity = activityRepository.save(activity);

        // Handle file uploads
        if (request.getAttachments() != null && request.getAttachments().length > 0) {
            for (MultipartFile file : request.getAttachments()) {
                if (!file.isEmpty()) {
                    try {
                        String savedFile = saveFile(file);
                        if (savedFile != null) {
                            Attachment attachment = new Attachment();
                            attachment.setFilename(savedFile);
                            attachment.setOriginalName(file.getOriginalFilename());
                            attachment.setFilePath(uploadDir + "/" + savedFile);
                            attachment.setFileSize(file.getSize());
                            attachment.setContentType(file.getContentType());
                            attachment.setActivity(savedActivity);
                            
                            attachmentRepository.save(attachment);
                        }
                    } catch (IOException e) {
                        System.err.println("Error saving file " + file.getOriginalFilename() + ": " + e.getMessage());
                    }
                }
            }
        }

        // Handle links
        if (request.getLinksJson() != null && !request.getLinksJson().isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<String> links = objectMapper.readValue(
                    request.getLinksJson(), 
                    new TypeReference<List<String>>(){}
                );
                
                for (String url : links) {
                    if (url != null && !url.trim().isEmpty()) {
                        ActivityLink link = new ActivityLink();
                        link.setUrl(url);
                        link.setActivity(savedActivity);
                        
                        activityLinkRepository.save(link);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error parsing links: " + e.getMessage());
            }
        }

        // Return the activity with all its relationships loaded
        return convertToDTO(activityRepository.findById(savedActivity.getId()).orElse(savedActivity));
    }

    private String saveFile(MultipartFile file) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFilename;
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

        // Get the current user for the remark
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Store the old status for the automatic remark
        String oldStatus = activity.getStatus().getValue();
        String newStatusValue = request.getStatus();

        try {
            Activity.ActivityStatus newStatus = Activity.ActivityStatus.fromValue(newStatusValue);
            activity.setStatus(newStatus);
            activity.setUpdatedAt(LocalDateTime.now());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status value: " + request.getStatus());
        }

        // Create automatic status change remark
        String statusChangeMessage = String.format("Status changed from '%s' to '%s' by %s (%s)", 
            getStatusDisplayName(oldStatus), 
            getStatusDisplayName(newStatusValue), 
            currentUser.getName(), 
            currentUser.getEmpId());
        
        Remark statusChangeRemark = new Remark(
            statusChangeMessage, 
            currentUserId, 
            activityId, 
            Remark.RemarkType.STATUS_UPDATE
        );
        remarkRepository.save(statusChangeRemark);

        // Add user-provided remark if available (this should be editable, so keep as GENERAL)
        if (request.getRemarks() != null && !request.getRemarks().trim().isEmpty()) {
            Remark userRemark = new Remark(
                request.getRemarks().trim(), 
                currentUserId, 
                activityId, 
                Remark.RemarkType.GENERAL
            );
            remarkRepository.save(userRemark);
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

    @Transactional
    public RemarkDTO updateRemark(Long remarkId, AddRemarkRequest request, String token) {
        Long currentUserId = jwtUtil.extractUserId(token);
        if (currentUserId == null) {
            throw new RuntimeException("Invalid token");
        }

        // Get the existing remark
        Remark remark = remarkRepository.findById(remarkId)
            .orElseThrow(() -> new RuntimeException("Remark not found"));

        // Check if user is the creator or admin
        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!currentUser.getRole().equals("admin") && !remark.getUserId().equals(currentUserId)) {
            throw new RuntimeException("You are not authorized to edit this remark");
        }

        // Prevent editing of status update remarks
        if (remark.getType() == Remark.RemarkType.STATUS_UPDATE) {
            throw new RuntimeException("Status update remarks cannot be edited");
        }

        // Update the remark
        remark.setText(request.getText());
        Remark savedRemark = remarkRepository.save(remark);
        
        return convertRemarkToDTO(savedRemark);
    }

    @Transactional
    public void deleteRemark(Long remarkId, String token) {
        Long currentUserId = jwtUtil.extractUserId(token);
        if (currentUserId == null) {
            throw new RuntimeException("Invalid token");
        }

        // Get the existing remark
        Remark remark = remarkRepository.findById(remarkId)
            .orElseThrow(() -> new RuntimeException("Remark not found"));

        // Check if user is the creator or admin
        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!currentUser.getRole().equals("admin") && !remark.getUserId().equals(currentUserId)) {
            throw new RuntimeException("You are not authorized to delete this remark");
        }

        // Prevent deletion of status update remarks
        if (remark.getType() == Remark.RemarkType.STATUS_UPDATE) {
            throw new RuntimeException("Status update remarks cannot be deleted");
        }

        // Delete the remark
        remarkRepository.delete(remark);
    }

    @Transactional
    public ActivityDTO updateActivityWithFiles(Long activityId, UpdateActivityWithFilesRequest request, String token) {
        Long currentUserId = jwtUtil.extractUserId(token);
        if (currentUserId == null) {
            throw new RuntimeException("Invalid token");
        }

        // Get the existing activity
        Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new RuntimeException("Activity not found"));

        // Check if user is the creator or admin
        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!currentUser.getRole().equals("admin") && !activity.getCreatedBy().equals(currentUserId)) {
            throw new RuntimeException("You are not authorized to edit this activity");
        }

        // Update basic fields
        activity.setName(request.getName());
        activity.setDescription(request.getDescription());
        activity.setPriority(request.getPriority());
        activity.setUpdatedAt(LocalDateTime.now());

        if (request.getTargetDate() != null && !request.getTargetDate().isEmpty()) {
            try {
                LocalDateTime targetDate = LocalDateTime.parse(request.getTargetDate(), DateTimeFormatter.ISO_DATE_TIME);
                activity.setTargetDate(targetDate);
            } catch (Exception e) {
                System.err.println("Error parsing target date: " + e.getMessage());
            }
        }

        // Update assigned members
        if (request.getAssignedUsersJson() != null && !request.getAssignedUsersJson().isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<Long> assignedUserIds = objectMapper.readValue(
                    request.getAssignedUsersJson(), 
                    new TypeReference<List<Long>>(){}
                );
                
                Set<User> assignedUsers = new HashSet<>();
                for (Long userId : assignedUserIds) {
                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        assignedUsers.add(user);
                    }
                }
                activity.setAssignedMembers(assignedUsers);
            } catch (Exception e) {
                System.err.println("Error parsing assigned users: " + e.getMessage());
            }
        }

        // Always ensure the creator is in the assigned members list (for both new assignments and updates)
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Creator user not found"));
        
        Set<User> assignedMembers = activity.getAssignedMembers();
        if (assignedMembers == null) {
            assignedMembers = new HashSet<>();
        }
        assignedMembers.add(creator);
        activity.setAssignedMembers(assignedMembers);
        
        if (request.getAttachmentsToDeleteJson() != null && !request.getAttachmentsToDeleteJson().isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<Long> attachmentIds = objectMapper.readValue(
                    request.getAttachmentsToDeleteJson(), 
                    new TypeReference<List<Long>>(){}
                );
                
                
                
                for (Long attachmentId : attachmentIds) {
                    
                    
                    // First check if the attachment exists
                    boolean exists = attachmentRepository.existsById(attachmentId);
                    
                    
                    if (exists) {
                        // Try to find the attachment first to get more info
                        Attachment attachment = attachmentRepository.findById(attachmentId).orElse(null);
                        if (attachment != null) {
                            System.out.println("Found attachment: " + attachment.getFilename() + " for activity: " + attachment.getActivity().getId());
                            
                            // Remove the attachment from the activity's collection first
                            Activity attachmentActivity = attachment.getActivity();
                            if (attachmentActivity != null && attachmentActivity.getAttachments() != null) {
                                attachmentActivity.getAttachments().remove(attachment);
                                System.out.println("Removed attachment from activity's collection");
                            }
                        }
                        
                        // Delete the attachment
                        attachmentRepository.deleteById(attachmentId);
                        
                        // Check if it was actually deleted
                        boolean existsAfter = attachmentRepository.existsById(attachmentId);
                        System.out.println("Attachment exists after deletion: " + existsAfter);
                    } else {
                        System.out.println("Attachment with ID " + attachmentId + " does not exist");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error deleting attachments: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No attachments to delete");
        }
        
        // Flush to ensure deletions are persisted
        entityManager.flush();

        // Handle link deletions
        if (request.getLinksToDeleteJson() != null && !request.getLinksToDeleteJson().isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<Long> linkIds = objectMapper.readValue(
                    request.getLinksToDeleteJson(), 
                    new TypeReference<List<Long>>(){}
                );
                
                System.out.println("Parsed link IDs to delete: " + linkIds);
                
                for (Long linkId : linkIds) {
                    System.out.println("Deleting link with ID: " + linkId);
                    
                    // First check if the link exists
                    boolean exists = activityLinkRepository.existsById(linkId);
                    System.out.println("Link exists before deletion: " + exists);
                    
                    if (exists) {
                        // Try to find the link first to get more info
                        ActivityLink link = activityLinkRepository.findById(linkId).orElse(null);
                        if (link != null) {
                            System.out.println("Found link: " + link.getUrl() + " for activity: " + link.getActivity().getId());
                            
                            // Remove the link from the activity's collection first
                            Activity linkActivity = link.getActivity();
                            if (linkActivity != null && linkActivity.getLinks() != null) {
                                linkActivity.getLinks().remove(link);
                                System.out.println("Removed link from activity's collection");
                            }
                        }
                        
                        // Delete the link
                        activityLinkRepository.deleteById(linkId);
                        
                        // Check if it was actually deleted
                        boolean existsAfter = activityLinkRepository.existsById(linkId);
                        System.out.println("Link exists after deletion: " + existsAfter);
                    } else {
                        System.out.println("Link with ID " + linkId + " does not exist");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error deleting links: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No links to delete");
        }
        
        // Flush to ensure deletions are persisted
        entityManager.flush();
        
        System.out.println("==================");

        // Handle new file uploads
        if (request.getAttachments() != null && request.getAttachments().length > 0) {
            for (MultipartFile file : request.getAttachments()) {
                if (!file.isEmpty()) {
                    try {
                        String savedFile = saveFile(file);
                        if (savedFile != null) {
                            Attachment attachment = new Attachment();
                            attachment.setFilename(savedFile);
                            attachment.setOriginalName(file.getOriginalFilename());
                            attachment.setFilePath(uploadDir + "/" + savedFile);
                            attachment.setFileSize(file.getSize());
                            attachment.setContentType(file.getContentType());
                            attachment.setActivity(activity);
                            
                            attachmentRepository.save(attachment);
                        }
                    } catch (IOException e) {
                        System.err.println("Error saving file " + file.getOriginalFilename() + ": " + e.getMessage());
                    }
                }
            }
        }

        // Handle new links
        if (request.getNewLinksJson() != null && !request.getNewLinksJson().isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<String> newLinks = objectMapper.readValue(
                    request.getNewLinksJson(), 
                    new TypeReference<List<String>>(){}
                );
                
                for (String linkUrl : newLinks) {
                    ActivityLink link = new ActivityLink();
                    link.setUrl(linkUrl);
                    link.setActivity(activity);
                    activityLinkRepository.save(link);
                }
            } catch (Exception e) {
                System.err.println("Error adding new links: " + e.getMessage());
            }
        }

        Activity savedActivity = activityRepository.save(activity);
        return convertToDTO(savedActivity);
    }

    @Transactional
    public void deleteActivity(Long activityId, String token) {
        Long currentUserId = jwtUtil.extractUserId(token);
        if (currentUserId == null) {
            throw new RuntimeException("Invalid token");
        }

        // Get the existing activity
        Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new RuntimeException("Activity not found"));

        // Check if user is the creator or admin
        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!currentUser.getRole().equals("admin") && !activity.getCreatedBy().equals(currentUserId)) {
            throw new RuntimeException("You are not authorized to delete this activity");
        }

        // Delete all related remarks first
        remarkRepository.deleteByActivityId(activityId);
        
        // Delete all attachments (cascade should handle this, but to be safe)
        attachmentRepository.deleteByActivityId(activityId);
        
        // Delete all activity links (cascade should handle this, but to be safe)
        activityLinkRepository.deleteByActivityId(activityId);

        // Delete the activity
        activityRepository.delete(activity);
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

    private String getStatusDisplayName(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return "Pending";
            case "in-progress":
                return "In Progress";
            case "completed":
                return "Completed";
            case "on-hold":
                return "On Hold";
            default:
                return status;
        }
    }
}