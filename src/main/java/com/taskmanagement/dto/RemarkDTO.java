package com.taskmanagement.dto;

import java.time.LocalDateTime;

public class RemarkDTO {
    private Long id;
    private String text;
    private Long userId;
    private String userName;
    private String userEmpId;
    private Long activityId;
    private String type;
    private LocalDateTime createdAt;

    public RemarkDTO() {}

    public RemarkDTO(Long id, String text, Long userId, String userName, String userEmpId, 
                    Long activityId, String type, LocalDateTime createdAt) {
        this.id = id;
        this.text = text;
        this.userId = userId;
        this.userName = userName;
        this.userEmpId = userEmpId;
        this.activityId = activityId;
        this.type = type;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmpId() {
        return userEmpId;
    }

    public void setUserEmpId(String userEmpId) {
        this.userEmpId = userEmpId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
