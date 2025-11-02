package com.famcare.util;

public class SentimentUtil {

    // Returns 0 (negative) to 1 (positive)
    public static double analyze(String text) {
        if (text == null || text.isBlank()) return 0.5;

        String lower = text.toLowerCase();
        int score = 0;

        if (lower.contains("happy") || lower.contains("good") || lower.contains("great")) score += 2;
        if (lower.contains("calm") || lower.contains("okay") || lower.contains("fine")) score += 1;
        if (lower.contains("sad") || lower.contains("angry") || lower.contains("tired")) score -= 2;
        if (lower.contains("stress") || lower.contains("anxious") || lower.contains("worried")) score -= 1;

        double normalized = Math.max(0, Math.min(1, 0.5 + score * 0.1));
        return normalized;
    }
}
