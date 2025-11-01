package com.famcare.service;

import com.famcare.model.InterventionAlert;
import com.famcare.model.MoodEntry;
import com.famcare.model.User;
import com.famcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InterventionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MoodService moodService;

    /**
     * Generate intervention alerts for all children of a parent
     */
    public List<InterventionAlert> generateAlerts(Integer parentId) {
        List<InterventionAlert> alerts = new ArrayList<>();
        List<User> children = userRepository.findChildrenByParentId(parentId);

        for (User child : children) {
            // Check for critical low mood
            InterventionAlert criticalAlert = checkCriticalLowMood(parentId, child);
            if (criticalAlert != null) {
                alerts.add(criticalAlert);
            }

            // Check for declining trend
            InterventionAlert trendAlert = checkDecliningTrend(parentId, child);
            if (trendAlert != null) {
                alerts.add(trendAlert);
            }

            // Check for repeated low moods
            InterventionAlert patternAlert = checkRepeatedLowMoods(parentId, child);
            if (patternAlert != null) {
                alerts.add(patternAlert);
            }

            // Check for unusual inactivity
            InterventionAlert inactivityAlert = checkInactivity(parentId, child);
            if (inactivityAlert != null) {
                alerts.add(inactivityAlert);
            }
        }

        return alerts;
    }

    /**
     * Check if latest mood is critically low (< 2)
     */
    private InterventionAlert checkCriticalLowMood(Integer parentId, User child) {
        List<MoodEntry> latestMoods = moodService.getUserMoodHistoryLastNDays(child.getId(), 1);

        if (latestMoods.isEmpty()) {
            return null;
        }

        MoodEntry latest = latestMoods.get(0);

        if (latest.getMoodScore() <= 2) {
            InterventionAlert alert = new InterventionAlert(
                parentId,
                child.getId(),
                child.getFullName(),
                "CRITICAL_LOW",
                "CRITICAL",
                "🚨 " + child.getFullName() + " logged a very low mood (" + latest.getMoodScore() + "/10)",
                "Consider reaching out immediately. Offer support and listen without judgment."
            );
            return alert;
        }

        return null;
    }

    /**
     * Check for declining trend
     */
    private InterventionAlert checkDecliningTrend(Integer parentId, User child) {
        Double weeklyAvg = moodService.getWeeklyAverageMoodScore(child.getId());
        Double monthlyAvg = moodService.getMonthlyAverageMoodScore(child.getId());

        if (weeklyAvg != null && monthlyAvg != null && weeklyAvg < monthlyAvg - 1.5) {
            InterventionAlert alert = new InterventionAlert(
                parentId,
                child.getId(),
                child.getFullName(),
                "DECLINING_TREND",
                "HIGH",
                "📉 " + child.getFullName() + "'s mood has been declining recently",
                "Ask how they're feeling. There might be something bothering them. Plan quality time together."
            );
            return alert;
        }

        return null;
    }

    /**
     * Check for repeated low moods (3+ days with mood < 4 in last 7 days)
     */
    private InterventionAlert checkRepeatedLowMoods(Integer parentId, User child) {
        List<MoodEntry> weekMoods = moodService.getUserMoodHistoryLastNDays(child.getId(), 7);

        long lowMoodDays = weekMoods.stream()
            .filter(m -> m.getMoodScore() < 4)
            .count();

        if (lowMoodDays >= 3) {
            InterventionAlert alert = new InterventionAlert(
                parentId,
                child.getId(),
                child.getFullName(),
                "PATTERN",
                "MEDIUM",
                "⚠️ " + child.getFullName() + " has had low moods for " + lowMoodDays + " days this week",
                "Consider suggesting professional support or identifying and addressing potential stressors."
            );
            return alert;
        }

        return null;
    }

    /**
     * Check for inactivity (no mood logged in 3 days)
     */
    private InterventionAlert checkInactivity(Integer parentId, User child) {
        List<MoodEntry> recentMoods = moodService.getUserMoodHistoryLastNDays(child.getId(), 3);

        if (recentMoods.isEmpty()) {
            InterventionAlert alert = new InterventionAlert(
                parentId,
                child.getId(),
                child.getFullName(),
                "INACTIVITY",
                "LOW",
                "🔔 " + child.getFullName() + " hasn't logged any mood entries recently",
                "Gently remind them to check in. Regular tracking helps identify patterns."
            );
            return alert;
        }

        return null;
    }

    /**
     * Get alerts for a specific child
     */
    public List<InterventionAlert> getAlertsForChild(Integer parentId, Integer childId) {
        User child = userRepository.findById(childId).orElse(null);

        if (child == null || !child.getParentId().equals(parentId)) {
            return new ArrayList<>();
        }

        List<InterventionAlert> alerts = new ArrayList<>();

        InterventionAlert critical = checkCriticalLowMood(parentId, child);
        if (critical != null) alerts.add(critical);

        InterventionAlert trend = checkDecliningTrend(parentId, child);
        if (trend != null) alerts.add(trend);

        InterventionAlert pattern = checkRepeatedLowMoods(parentId, child);
        if (pattern != null) alerts.add(pattern);

        InterventionAlert inactivity = checkInactivity(parentId, child);
        if (inactivity != null) alerts.add(inactivity);

        return alerts;
    }

    /**
     * Get severity color for UI display
     */
    public String getSeverityColor(String severity) {
        switch (severity) {
            case "CRITICAL":
                return "#d32f2f"; // Red
            case "HIGH":
                return "#f57c00"; // Orange
            case "MEDIUM":
                return "#fbc02d"; // Yellow
            case "LOW":
                return "#388e3c"; // Green
            default:
                return "#666";
        }
    }

    /**
     * Get alert icon based on type
     */
    public String getAlertIcon(String alertType) {
        switch (alertType) {
            case "CRITICAL_LOW":
                return "🚨";
            case "DECLINING_TREND":
                return "📉";
            case "PATTERN":
                return "⚠️";
            case "INACTIVITY":
                return "🔔";
            default:
                return "ℹ️";
        }
    }
}