package com.taskmanagement.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CreateActivityRequest {
    private String name;
    private String description;
    private String priority;
    private Long team_id;
    private String targetDate;
    private List<Long> assignedUsers;

    // Constructors
    public CreateActivityRequest() {}

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

    public Long getTeam_id() {
        return team_id;
    }

    public void setTeam_id(Long team_id) {
        this.team_id = team_id;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }

    public List<Long> getAssignedUsers() {
        return assignedUsers;
    }

    public void setAssignedUsers(List<Long> assignedUsers) {
        this.assignedUsers = assignedUsers;
    }
}