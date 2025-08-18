package com.taskmanagement.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ActivityDTO {
    private Long id;
    private String name;
    private String description;
    private String priority;
    private String status; // Added status field
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

    public String getCreatorEmpId() { return creatorEmpId; }
    public void setCreatorEmpId(String creatorEmpId) { this.creatorEmpId = creatorEmpId; }

    public List<AssignedMemberDTO> getAssignedMembers() { return assignedMembers; }
    public void setAssignedMembers(List<AssignedMemberDTO> assignedMembers) { this.assignedMembers = assignedMembers; }

    public List<RemarkDTO> getRemarks() { return remarks; }
    public void setRemarks(List<RemarkDTO> remarks) { this.remarks = remarks; }

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
}