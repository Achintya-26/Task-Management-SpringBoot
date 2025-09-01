package com.taskmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class ActivityDTO {
    private Long id;
    private String name;
    private String description;
    private String priority;
    private String status; // Added status field
    private Boolean creatorSubscribed; // Added creator subscription field
    private LocalDateTime targetDate;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Team info
    private Long teamId;
    private String teamName;
    
    // Creator info
    private String creatorName;
    private String creatorEmpId;
    
    // Assigned members
    private List<AssignedMemberDTO> assignedMembers;
    
    // Remarks
    private List<RemarkDTO> remarks;
    
    // Attachments
    private List<AttachmentDTO> attachments;
    
    // Links
    private List<ActivityLinkDTO> links;

    // Constructors
    public ActivityDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getCreatorSubscribed() { return creatorSubscribed; }
    public void setCreatorSubscribed(Boolean creatorSubscribed) { this.creatorSubscribed = creatorSubscribed; }

    public LocalDateTime getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDateTime targetDate) { this.targetDate = targetDate; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    
    // public String getCreatedByName() { return creatorName; }

    public String getCreatorEmpId() { return creatorEmpId; }
    public void setCreatorEmpId(String creatorEmpId) { this.creatorEmpId = creatorEmpId; }

    public List<AssignedMemberDTO> getAssignedMembers() { return assignedMembers; }
    public void setAssignedMembers(List<AssignedMemberDTO> assignedMembers) { this.assignedMembers = assignedMembers; }

    public List<RemarkDTO> getRemarks() { return remarks; }
    public void setRemarks(List<RemarkDTO> remarks) { this.remarks = remarks; }

    public List<AttachmentDTO> getAttachments() { return attachments; }
    public void setAttachments(List<AttachmentDTO> attachments) { this.attachments = attachments; }

    public List<ActivityLinkDTO> getLinks() { return links; }
    public void setLinks(List<ActivityLinkDTO> links) { this.links = links; }

    // Inner class for assigned members
    public static class AssignedMemberDTO {
        private Long id;
        private String name;
        private String empId;

        public AssignedMemberDTO() {}

        public AssignedMemberDTO(Long id, String name, String empId) {
            this.id = id;
            this.name = name;
            this.empId = empId;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmpId() { return empId; }
        public void setEmpId(String empId) { this.empId = empId; }
    }

    // Inner class for attachments
    public static class AttachmentDTO {
        private Long id;
        private String filename;
        private String originalName;
        private String filePath;
        private Long fileSize;
        private String contentType;
        private LocalDateTime uploadedAt;

        public AttachmentDTO() {}

        public AttachmentDTO(Long id, String filename, String originalName, String filePath, 
                           Long fileSize, String contentType, LocalDateTime uploadedAt) {
            this.id = id;
            this.filename = filename;
            this.originalName = originalName;
            this.filePath = filePath;
            this.fileSize = fileSize;
            this.contentType = contentType;
            this.uploadedAt = uploadedAt;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }

        public String getOriginalName() { return originalName; }
        public void setOriginalName(String originalName) { this.originalName = originalName; }

        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }

        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }

        public LocalDateTime getUploadedAt() { return uploadedAt; }
        public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    }

    // Inner class for activity links
    public static class ActivityLinkDTO {
        private Long id;
        private String url;
        private String title;
        private LocalDateTime createdAt;

        public ActivityLinkDTO() {}

        public ActivityLinkDTO(Long id, String url, String title, LocalDateTime createdAt) {
            this.id = id;
            this.url = url;
            this.title = title;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}