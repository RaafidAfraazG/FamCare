package com.famcare.model;

import java.time.LocalDateTime;

public class JournalEntry {
    private Integer id;
    private Integer userId;
    private String title;
    private String content;
    private Boolean isPrivate; // true = only child can see, false = parent can see summary
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public JournalEntry() {
    }

    public JournalEntry(Integer userId, String title, String content, Boolean isPrivate) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.isPrivate = isPrivate;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "JournalEntry{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", isPrivate=" + isPrivate +
                ", createdAt=" + createdAt +
                '}';
    }
}