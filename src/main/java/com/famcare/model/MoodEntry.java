package com.famcare.model;

import java.time.LocalDateTime;

public class MoodEntry {
    private Integer id;
    private Integer userId;
    private Integer moodScore; // 1-10 scale
    private String moodLabel; // happy, sad, anxious, calm, etc.
    private String notes;
    private LocalDateTime createdAt;

    // Constructors
    public MoodEntry() {
    }

    public MoodEntry(Integer userId, Integer moodScore, String moodLabel, String notes) {
        this.userId = userId;
        this.moodScore = moodScore;
        this.moodLabel = moodLabel;
        this.notes = notes;
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

    public Integer getMoodScore() {
        return moodScore;
    }

    public void setMoodScore(Integer moodScore) {
        this.moodScore = moodScore;
    }

    public String getMoodLabel() {
        return moodLabel;
    }

    public void setMoodLabel(String moodLabel) {
        this.moodLabel = moodLabel;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "MoodEntry{" +
                "id=" + id +
                ", userId=" + userId +
                ", moodScore=" + moodScore +
                ", moodLabel='" + moodLabel + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}