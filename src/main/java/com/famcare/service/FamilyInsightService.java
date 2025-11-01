package com.famcare.service;

import com.famcare.model.FamilyInsight;
import com.famcare.model.MoodTrend;
import com.famcare.model.User;
import com.famcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FamilyInsightService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MoodService moodService;

    /**
     * Get family insights for a parent
     */
    public FamilyInsight getFamilyInsights(Integer parentId) {
        // Get all children of parent
        List<User> children = userRepository.findChildrenByParentId(parentId);

        if (children.isEmpty()) {
            FamilyInsight emptyInsight = new FamilyInsight(0, parentId, 0, 0.0, 0, 0);
            emptyInsight.setFamilyWellnessStatus("No Data");
            emptyInsight.setRecommendation("Add children to track family wellness");
            return emptyInsight;
        }

        // Calculate family stats
        Double totalMood = 0.0;
        Integer lowMoodCount = 0;
        Integer goodMoodCount = 0;
        String dominantMood = "";
        Map<String, Integer> moodFrequency = new HashMap<>();

        for (User child : children) {
            Double avgMood = moodService.getWeeklyAverageMoodScore(child.getId());
            totalMood += avgMood;

            if (avgMood < 3) {
                lowMoodCount++;
            }
            if (avgMood >= 7) {
                goodMoodCount++;
            }

            // Count mood frequencies
            List<com.famcare.model.MoodEntry> moods = moodService.getUserMoodHistoryLastNDays(child.getId(), 7);
            for (com.famcare.model.MoodEntry mood : moods) {
                moodFrequency.put(mood.getMoodLabel(), 
                    moodFrequency.getOrDefault(mood.getMoodLabel(), 0) + 1);
            }
        }

        Double familyAverageMood = children.size() > 0 ? totalMood / children.size() : 0.0;

        // Find dominant mood
        dominantMood = moodFrequency.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Neutral");

        // Create family insight
        FamilyInsight insight = new FamilyInsight(parentId, parentId, children.size(), 
            familyAverageMood, lowMoodCount, goodMoodCount);

        insight.setDominantMood(dominantMood);
        insight.setFamilyWellnessStatus(calculateWellnessStatus(familyAverageMood, lowMoodCount, children.size()));
        insight.setTrend(calculateTrend(children));
        insight.setRecommendation(generateRecommendation(familyAverageMood, lowMoodCount, children.size()));

        return insight;
    }

    /**
     * Get mood trends for all family members
     */
    public List<MoodTrend> getFamilyMoodTrends(Integer parentId) {
        List<User> children = userRepository.findChildrenByParentId(parentId);
        List<MoodTrend> trends = new ArrayList<>();

        for (User child : children) {
            MoodTrend trend = new MoodTrend();
            trend.setUserId(child.getId());
            trend.setUserName(child.getFullName());

            Double weeklyAvg = moodService.getWeeklyAverageMoodScore(child.getId());
            Double monthlyAvg = moodService.getMonthlyAverageMoodScore(child.getId());

            trend.setWeeklyAverage(weeklyAvg != null ? weeklyAvg : 0.0);
            trend.setMonthlyAverage(monthlyAvg != null ? monthlyAvg : 0.0);

            // Count low and high mood days
            List<com.famcare.model.MoodEntry> moods = moodService.getUserMoodHistoryLastNDays(child.getId(), 7);
            int lowDays = (int) moods.stream().filter(m -> m.getMoodScore() < 3).count();
            int highDays = (int) moods.stream().filter(m -> m.getMoodScore() >= 7).count();

            trend.setLowMoodDays(lowDays);
            trend.setHighMoodDays(highDays);
            trend.setMoodLogsCount(moods.size());

            // Calculate trend
            trend.setTrend(calculateIndividualTrend(child.getId()));

            trends.add(trend);
        }

        return trends;
    }

    /**
     * Calculate wellness status based on average mood
     */
    private String calculateWellnessStatus(Double avgMood, Integer lowMoodCount, Integer totalMembers) {
        if (avgMood >= 7) {
            return "Excellent 🤩";
        } else if (avgMood >= 5.5) {
            return "Good 😊";
        } else if (avgMood >= 4) {
            return "Fair 😐";
        } else {
            return "Needs Attention ⚠️";
        }
    }

    /**
     * Calculate family trend
     */
    private String calculateTrend(List<User> children) {
        if (children.isEmpty()) {
            return "No Data";
        }

        Double prevAvg = 0.0;
        Double currAvg = 0.0;

        for (User child : children) {
            Double prev30 = moodService.getAverageMoodScore(child.getId(), 30);
            Double prev7 = moodService.getAverageMoodScore(child.getId(), 7);

            prevAvg += prev30;
            currAvg += prev7;
        }

        prevAvg /= children.size();
        currAvg /= children.size();

        if (currAvg > prevAvg + 1) {
            return "📈 Improving";
        } else if (currAvg < prevAvg - 1) {
            return "📉 Declining";
        } else {
            return "➡️ Stable";
        }
    }

    /**
     * Calculate individual trend for a child
     */
    private String calculateIndividualTrend(Integer userId) {
        Double prev30 = moodService.getAverageMoodScore(userId, 30);
        Double prev7 = moodService.getAverageMoodScore(userId, 7);

        if (prev7 > prev30 + 1) {
            return "Improving";
        } else if (prev7 < prev30 - 1) {
            return "Declining";
        } else {
            return "Stable";
        }
    }

    /**
     * Generate wellness recommendation
     */
    private String generateRecommendation(Double avgMood, Integer lowMoodCount, Integer totalMembers) {
        if (avgMood >= 7) {
            return "✅ Family wellness is excellent! Keep up the positive activities.";
        } else if (avgMood >= 5.5) {
            return "👍 Family is doing well. Consider a family activity to strengthen bonds.";
        } else if (lowMoodCount > 0) {
            return "⚠️ Some family members are experiencing low moods. Consider check-ins or activities together.";
        } else {
            return "🔴 Family wellness needs attention. Schedule individual check-ins with members.";
        }
    }
}