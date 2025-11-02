package com.famcare.service;

import java.util.*;

public class SentimentAnalysisService {

    // Harmful keywords related to depression, self-harm, suicide
    private static final Set<String> HARMFUL_KEYWORDS = new HashSet<>(Arrays.asList(
        // Suicide related
        "suicide", "kill myself", "kill me", "end my life", "want to die", "wish i was dead",
        "die", "dead", "coffin", "grave", "hanging", "overdose", "poison", "jump",
        
        // Self-harm
        "cut myself", "cutting", "cut my", "harm myself", "hurt myself", "self harm",
        "blade", "knife", "razor", "scar",
        
        // Depression/hopelessness
        "depressed", "depression", "hopeless", "no hope", "lost all hope", "worthless",
        "useless", "nobody cares", "no one cares", "alone", "lonely", "isolated",
        "nobody loves me", "no love", "hate myself", "hate me",
        
        // Anxiety/panic
        "panic attack", "anxious", "anxiety", "nervous breakdown",
        
        // Dark thoughts
        "dark thoughts", "dark thoughts", "intrusive thoughts", "voices",
        "lose my mind", "go crazy", "lose it", "break down",
        
        // Other concerning
        "abuse", "abused", "trauma", "ptsd", "rape", "assault"
    ));

    // Medium severity keywords - concerning but not immediate danger
    private static final Set<String> CONCERNING_KEYWORDS = new HashSet<>(Arrays.asList(
        "sad", "upset", "cry", "crying", "stressed", "stress", "pain", "hurt",
        "scared", "fear", "worried", "worry", "anger", "angry", "frustrated",
        "failed", "failure", "mistake", "stupid", "dumb", "idiot", "loser",
        "bullied", "bullying", "tease", "teasing", "mock", "mockery"
    ));

    /**
     * Analyze message for harmful content
     * Returns severity level: SAFE, CONCERNING, or HARMFUL
     */
    public MessageSentiment analyzeSentiment(String messageText) {
        if (messageText == null || messageText.trim().isEmpty()) {
            return new MessageSentiment("SAFE", false, 0);
        }

        String lowerText = messageText.toLowerCase();
        int harmfulCount = 0;
        int concerningCount = 0;

        // Check for harmful keywords
        for (String keyword : HARMFUL_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                harmfulCount++;
            }
        }

        // Check for concerning keywords
        for (String keyword : CONCERNING_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                concerningCount++;
            }
        }

        // Determine severity
        if (harmfulCount >= 1) {
            return new MessageSentiment("HARMFUL", true, harmfulCount);
        } else if (concerningCount >= 2) {
            return new MessageSentiment("CONCERNING", true, concerningCount);
        }

        return new MessageSentiment("SAFE", false, 0);
    }

    /**
     * Check if message contains harmful keywords
     */
    public boolean isHarmful(String messageText) {
        MessageSentiment sentiment = analyzeSentiment(messageText);
        return sentiment.isHarmful;
    }

    /**
     * Inner class to hold sentiment analysis results
     */
    public static class MessageSentiment {
        public String severity; // SAFE, CONCERNING, HARMFUL
        public boolean isHarmful;
        public int keywordCount;

        public MessageSentiment(String severity, boolean isHarmful, int keywordCount) {
            this.severity = severity;
            this.isHarmful = isHarmful;
            this.keywordCount = keywordCount;
        }

        @Override
        public String toString() {
            return "MessageSentiment{" +
                    "severity='" + severity + '\'' +
                    ", isHarmful=" + isHarmful +
                    ", keywordCount=" + keywordCount +
                    '}';
        }
    }
}