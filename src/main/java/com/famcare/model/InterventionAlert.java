package com.famcare.model;

import java.time.LocalDateTime;

public class InterventionAlert {
    private Integer alertId;
    private Integer parentId;
    private Integer childId;
    private String childName;
    private String alertType; // "LOW_MOOD", "DECLINING_TREND", "CRITICAL", "PATTERN"
    private String severity; // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    private String message;
    private String suggestion;
    private LocalDateTime createdAt;
    private Boolean isRead;

    // Constructors
    public InterventionAlert() {
    }

    public InterventionAlert(Integer parentId, Integer childId, String childName,
                            String alertType, String severity, String message, String suggestion) {
        this.parentId = parentId;
        this.childId = childId;
        this.childName = childName;
        this.alertType = alertType;
        this.severity = severity;
        this.message = message;
        this.suggestion = suggestion;
        this.isRead = false;
    }

    // Getters and Setters
    public Integer getAlertId() {
        return alertId;
    }

    public void setAlertId(Integer alertId) {
        this.alertId = alertId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getChildId() {
        return childId;
    }

    public void setChildId(Integer childId) {
        this.childId = childId;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    @Override
    public String toString() {
        return "InterventionAlert{" +
                "alertType='" + alertType + '\'' +
                ", severity='" + severity + '\'' +
                ", childName='" + childName + '\'' +
                '}';
    }
}