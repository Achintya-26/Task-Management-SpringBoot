package com.taskmanagement.dto;

import org.springframework.web.multipart.MultipartFile;

public class UpdateActivityWithFilesRequest {
    private String name;
    private String description;
    private String priority;
    private String targetDate;
    private String assignedUsersJson;
    private String creatorSubscribed;
    private String newLinksJson;
    private String attachmentsToDeleteJson;
    private String linksToDeleteJson;
    private MultipartFile[] attachments;

    // Constructors
    public UpdateActivityWithFilesRequest() {}

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

    public String getCreatorSubscribed() {
        return creatorSubscribed;
    }

    public void setCreatorSubscribed(String creatorSubscribed) {
        this.creatorSubscribed = creatorSubscribed;
    }

    public String getNewLinksJson() {
        return newLinksJson;
    }

    public void setNewLinksJson(String newLinksJson) {
        this.newLinksJson = newLinksJson;
    }

    public String getAttachmentsToDeleteJson() {
        return attachmentsToDeleteJson;
    }

    public void setAttachmentsToDeleteJson(String attachmentsToDeleteJson) {
        this.attachmentsToDeleteJson = attachmentsToDeleteJson;
    }

    public String getLinksToDeleteJson() {
        return linksToDeleteJson;
    }

    public void setLinksToDeleteJson(String linksToDeleteJson) {
        this.linksToDeleteJson = linksToDeleteJson;
    }

    public MultipartFile[] getAttachments() {
        return attachments;
    }

    public void setAttachments(MultipartFile[] attachments) {
        this.attachments = attachments;
    }
}
