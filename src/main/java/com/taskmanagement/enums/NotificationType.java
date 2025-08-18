package com.taskmanagement.enums;

public enum NotificationType {
    INFO("info"),
    SUCCESS("success"),
    WARNING("warning"),
    ERROR("error");
    
    private final String value;
    
    NotificationType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static NotificationType fromString(String text) {
        for (NotificationType type : NotificationType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No notification type with text " + text + " found");
    }
}