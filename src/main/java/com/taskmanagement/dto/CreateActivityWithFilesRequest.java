package com.taskmanagement.dto;

import org.springframework.web.multipart.MultipartFile;

public class CreateActivityWithFilesRequest {
    private String name;
    private String description;
    private String priority;
    private Long teamId;
    private String targetDate;
    private String assignedUsersJson;
    private String linksJson;
    private MultipartFile[] attachments;
    private Boolean creatorSubscribed;

    // Constructors
    public CreateActivityWithFilesRequest() {}

    public CreateActivityWithFilesRequest(String name, String description, String priority, 
                                        Long teamId, String targetDate, String assignedUsersJson, 
                                        String linksJson, MultipartFile[] attachments, Boolean creatorSubscribed) {
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.teamId = teamId;
        this.targetDate = targetDate;
        this.assignedUsersJson = assignedUsersJson;
        this.linksJson = linksJson;
        this.attachments = attachments;
        this.creatorSubscribed = creatorSubscribed;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }

    public String getAssignedUsersJson() {
        return assignedUsersJson;
    }

    public void setAssignedUsersJson(String assignedUsersJson) {
        this.assignedUsersJson = assignedUsersJson;
    }

    public String getLinksJson() {
        return linksJson;
    }

    public void setLinksJson(String linksJson) {
        this.linksJson = linksJson;
    }

    public MultipartFile[] getAttachments() {
        return attachments;
    }

    public void setAttachments(MultipartFile[] attachments) {
        this.attachments = attachments;
    }

    public Boolean getCreatorSubscribed() {
        return creatorSubscribed;
    }

    public void setCreatorSubscribed(Boolean creatorSubscribed) {
        this.creatorSubscribed = creatorSubscribed;
    }
}
