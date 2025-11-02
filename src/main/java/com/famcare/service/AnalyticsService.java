package com.famcare.service;

import com.famcare.model.MoodEntry;
import com.famcare.model.JournalEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides advanced analytics by combining mood and journal insights
 * without modifying core MoodService or JournalService logic.
 */
@Service
public class AnalyticsService {

    @Autowired
    private MoodService moodService;

    @Autowired
    private JournalService journalService;

    /**
     * Get weekly mood trend (average mood per day)
     */
    public Map<LocalDate, Double> getWeeklyMoodTrend(Integer userId) {
        List<MoodEntry> moods = moodService.getUserMoodHistoryLastNDays(userId, 7);

        return moods.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getCreatedAt().toLocalDate(),
                        Collectors.averagingDouble(MoodEntry::getMoodScore)
                ));
    }

    /**
     * Detect mood fluctuations — how much mood changes day to day
     */
    public double getMoodVolatility(Integer userId, int days) {
        List<MoodEntry> moods = moodService.getUserMoodHistoryLastNDays(userId, days)
                .stream()
                .sorted(Comparator.comparing(MoodEntry::getCreatedAt))
                .toList();

        if (moods.size() < 2) return 0.0;

        double sumChange = 0;
        for (int i = 1; i < moods.size(); i++) {
            sumChange += Math.abs(moods.get(i).getMoodScore() - moods.get(i - 1).getMoodScore());
        }

        return sumChange / (moods.size() - 1);
    }

    /**
     * Get keyword frequency in journals to detect emotional patterns
     */
    public Map<String, Long> getJournalKeywordFrequency(Integer userId) {
        List<JournalEntry> entries = journalService.getUserJournalEntries(userId);

        List<String> keywords = Arrays.asList("happy", "sad", "angry", "stress", "family", "love", "school", "tired", "lonely");
        Map<String, Long> keywordCount = new LinkedHashMap<>();

        for (String keyword : keywords) {
            long count = entries.stream()
                    .filter(e -> e.getContent().toLowerCase().contains(keyword))
                    .count();
            keywordCount.put(keyword, count);
        }

        return keywordCount;
    }

    /**
     * Suggest interventions based on mood + journal analysis
     */
    public String getInterventionSuggestion(Integer userId) {
        Double avgMood = moodService.getWeeklyAverageMoodScore(userId);
        double volatility = getMoodVolatility(userId, 7);
        Map<String, Long> journalWords = getJournalKeywordFrequency(userId);

        if (avgMood == null) return "No mood data available. Encourage journaling and self-reflection.";

        if (avgMood < 4) {
            if (journalWords.getOrDefault("stress", 0L) > 2 || journalWords.getOrDefault("tired", 0L) > 2) {
                return "High stress detected. Encourage rest, mindfulness activities, or open family discussions.";
            }
            return "Mood seems consistently low. Consider scheduling a family wellness check-in or professional support.";
        }

        if (volatility > 3) {
            return "Significant mood fluctuations detected. Suggest open communication and emotional regulation activities.";
        }

        if (avgMood >= 7) {
            return "Great emotional balance detected! Encourage continuing positive family routines and gratitude journaling.";
        }

        return "Overall mood is stable. Continue regular tracking and promote healthy emotional sharing.";
    }

    /**
     * Combine everything into one dashboard-like insight
     */
    public Map<String, Object> getUserAnalyticsDashboard(Integer userId) {
        Map<String, Object> dashboard = new LinkedHashMap<>();

        dashboard.put("weeklyMoodTrend", getWeeklyMoodTrend(userId));
        dashboard.put("averageMoodScore", moodService.getWeeklyAverageMoodScore(userId));
        dashboard.put("moodVolatility", getMoodVolatility(userId, 7));
        dashboard.put("journalKeywordFrequency", getJournalKeywordFrequency(userId));
        dashboard.put("interventionSuggestion", getInterventionSuggestion(userId));

        return dashboard;
    }

    public String getEmotionalDominantTrend(Integer userId) {
        Double avgMood = moodService.getWeeklyAverageMoodScore(userId);
        Map<String, Long> words = getJournalKeywordFrequency(userId);

        long happy = words.getOrDefault("happy", 0L);
        long stress = words.getOrDefault("stress", 0L);
        long tired = words.getOrDefault("tired", 0L);
        long love = words.getOrDefault("love", 0L);

        if (avgMood == null) return "No sufficient data";

        if (avgMood >= 7 && (happy > stress)) return "Positive and Stable";
        if (avgMood < 4 && stress > happy) return "High Stress Detected";
        if (tired > 2 && avgMood < 6) return "Fatigue / Emotional Exhaustion";
        if (love > 1 && avgMood >= 6) return "Emotionally Connected Family";

        return "Moderate Emotional State";
    }

    public double getStressIndex(Integer userId) {
        Double avgMood = moodService.getWeeklyAverageMoodScore(userId);
        double volatility = getMoodVolatility(userId, 7);
        Map<String, Long> words = getJournalKeywordFrequency(userId);

        long stressKeywords = Stream.of("stress", "tired", "angry", "lonely")
                .mapToLong(w -> words.getOrDefault(w, 0L)).sum();

        double moodFactor = (10 - (avgMood == null ? 5 : avgMood)) * 5;
        double volatilityFactor = volatility * 10;
        double wordFactor = stressKeywords * 8;

        double index = Math.min(100, moodFactor + volatilityFactor + wordFactor);
        return Math.round(index * 100.0) / 100.0;
    }

    public Map<String, Object> getSentimentRatio(Integer userId) {
        Map<String, Long> words = getJournalKeywordFrequency(userId);

        long positive = Stream.of("happy", "love", "family").mapToLong(w -> words.getOrDefault(w, 0L)).sum();
        long negative = Stream.of("stress", "angry", "tired", "lonely").mapToLong(w -> words.getOrDefault(w, 0L)).sum();

        double total = positive + negative;
        double posRatio = total == 0 ? 0 : (positive / total) * 100;
        double negRatio = total == 0 ? 0 : (negative / total) * 100;

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("positivePercent", posRatio);
        map.put("negativePercent", negRatio);
        return map;
    }

    public Map<String, Object> getAdvancedAnalytics(Integer userId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dominantTrend", getEmotionalDominantTrend(userId));
        result.put("stressIndex", getStressIndex(userId));
        result.put("sentimentRatio", getSentimentRatio(userId));
        result.put("baseAnalytics", getUserAnalyticsDashboard(userId));
        return result;
    }

    public Map<String, Object> getFamilyAnalytics(List<Integer> childIds) {
        Map<String, Object> family = new LinkedHashMap<>();

        double avgStress = 0;
        double avgMood = 0;
        int count = childIds.size();

        List<Map<String, Object>> members = new ArrayList<>();

        for (Integer id : childIds) {
            Map<String, Object> data = getAdvancedAnalytics(id);
            members.add(Map.of(
                "userId", id,
                "trend", data.get("dominantTrend"),
                "stressIndex", data.get("stressIndex")
            ));
            avgStress += (double) data.get("stressIndex");
            avgMood += (Double) ((Map<String, Object>) data.get("baseAnalytics")).get("averageMoodScore");
        }

        family.put("members", members);
        family.put("averageStressIndex", avgStress / count);
        family.put("averageMood", avgMood / count);
        family.put("familyInsight",
                avgStress / count > 60
                        ? "High family stress detected – consider shared activities or discussions."
                        : "Family mood appears stable and positive overall.");

        return family;
    }
    
    
    
}


