package com.famcare.service;

import com.famcare.model.MoodEntry;
import com.famcare.repository.MoodEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MoodService {

    @Autowired
    private MoodEntryRepository moodEntryRepository;

    /**
     * Create a new mood entry
     * 
     * @param userId - ID of the user logging the mood
     * @param moodScore - mood score (1-10)
     * @param moodLabel - label like "happy", "sad", etc.
     * @param notes - optional notes
     */
    public void logMood(Integer userId, Integer moodScore, String moodLabel, String notes) {
        // Validate mood score is between 1-10
        if (moodScore < 1 || moodScore > 10) {
            throw new IllegalArgumentException("Mood score must be between 1 and 10");
        }

        MoodEntry moodEntry = new MoodEntry(userId, moodScore, moodLabel, notes);
        moodEntryRepository.save(moodEntry);
    }

    /**
     * Get all mood entries for a user
     */
    public List<MoodEntry> getUserMoodHistory(Integer userId) {
        return moodEntryRepository.findByUserId(userId);
    }

    /**
     * Get mood entries for last N days
     */
    public List<MoodEntry> getUserMoodHistoryLastNDays(Integer userId, int days) {
        return moodEntryRepository.findByUserIdLastNDays(userId, days);
    }

    /**
     * Get a specific mood entry
     */
    public Optional<MoodEntry> getMoodEntryById(Integer id) {
        return moodEntryRepository.findById(id);
    }

    /**
     * Delete a mood entry
     */
    public void deleteMoodEntry(Integer id) {
        moodEntryRepository.deleteById(id);
    }

    /**
     * Get average mood score for a user over last N days
     * Useful for analytics
     */
    public Double getAverageMoodScore(Integer userId, int days) {
        return moodEntryRepository.getAverageMoodScore(userId, days);
    }

    /**
     * Get average mood score for last 7 days
     */
    public Double getWeeklyAverageMoodScore(Integer userId) {
        return getAverageMoodScore(userId, 7);
    }

    /**
     * Get average mood score for last 30 days
     */
    public Double getMonthlyAverageMoodScore(Integer userId) {
        return getAverageMoodScore(userId, 30);
    }

    /**
     * Get mood entry count for a user
     */
    public int getMoodEntryCount(Integer userId) {
        return moodEntryRepository.countByUserId(userId);
    }

    /**
     * Get most recent mood entry for a user
     */
    public Optional<MoodEntry> getLatestMoodEntry(Integer userId) {
        List<MoodEntry> moods = getUserMoodHistory(userId);
        return moods.isEmpty() ? Optional.empty() : Optional.of(moods.get(0));
    }

    /**
     * Get mood category based on score
     * Helper method for display
     */
    public String getMoodCategory(Integer moodScore) {
        if (moodScore <= 2) return "Very Bad";
        if (moodScore <= 4) return "Bad";
        if (moodScore <= 6) return "Neutral";
        if (moodScore <= 8) return "Good";
        return "Excellent";
    }

    /**
     * Get color for mood visualization
     */
    public String getMoodColor(Integer moodScore) {
        if (moodScore <= 2) return "red";
        if (moodScore <= 4) return "orange";
        if (moodScore <= 6) return "yellow";
        if (moodScore <= 8) return "lightgreen";
        return "green";
    }
}