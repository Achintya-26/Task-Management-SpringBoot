package com.taskmanagement.dto;

public class AddRemarkRequest {
    private String text;

    // Constructors
    public AddRemarkRequest() {}

    public AddRemarkRequest(String text) {
        this.text = text;
    }

    // Getters and Setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}