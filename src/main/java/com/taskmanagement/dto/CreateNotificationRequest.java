package com.taskmanagement.dto;

public class CreateNotificationRequest {
    private Long userId;
    private String title;
    private String message;
    private String type;
    private Long relatedTeamId;
    private Long relatedActivityId;

    // Constructors
    public CreateNotificationRequest() {}

    public CreateNotificationRequest(Long userId, String title, String message, String type) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getRelatedTeamId() {
        return relatedTeamId;
    }

    public void setRelatedTeamId(Long relatedTeamId) {
        this.relatedTeamId = relatedTeamId;
    }

    public Long getRelatedActivityId() {
        return relatedActivityId;
    }

    public void setRelatedActivityId(Long relatedActivityId) {
        this.relatedActivityId = relatedActivityId;
    }
}