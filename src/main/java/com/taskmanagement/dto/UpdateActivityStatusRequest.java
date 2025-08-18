package com.taskmanagement.dto;

public class UpdateActivityStatusRequest {
    private String status;
    private String remarks;

    // Constructors
    public UpdateActivityStatusRequest() {}

    public UpdateActivityStatusRequest(String status, String remarks) {
        this.status = status;
        this.remarks = remarks;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}