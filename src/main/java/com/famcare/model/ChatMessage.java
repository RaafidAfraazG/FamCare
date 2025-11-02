package com.famcare.model;

import java.time.LocalDateTime;

public class ChatMessage {
    private Integer id;
    private Integer familyId; // Parent ID to group family messages
    private Integer userId;
    private String username;
    private String fullName;
    private String message;
    private LocalDateTime sentAt;

    // Constructors
    public ChatMessage() {
    }

    public ChatMessage(Integer familyId, Integer userId, String username, String fullName, String message) {
        this.familyId = familyId;
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.message = message;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFamilyId() {
        return familyId;
    }

    public void setFamilyId(Integer familyId) {
        this.familyId = familyId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "id=" + id +
                ", familyId=" + familyId +
                ", username='" + username + '\'' +
                ", sentAt=" + sentAt +
                '}';
    }
}