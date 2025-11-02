package com.famcare.model;

public class MoodTrend {
    private Integer userId;
    private String userName;
    private Double weeklyAverage;
    private Double monthlyAverage;
    private Integer lowMoodDays; // Days with mood < 3
    private Integer highMoodDays; // Days with mood >= 7
    private String trend; // "Improving", "Declining", "Stable", "No Data"
    private String primaryMood; // Most logged mood
    private Integer moodLogsCount;

    // Constructors
    public MoodTrend() {
    }

    public MoodTrend(Integer userId, String userName, Double weeklyAverage, 
                     Double monthlyAverage, Integer lowMoodDays, Integer highMoodDays) {
        this.userId = userId;
        this.userName = userName;
        this.weeklyAverage = weeklyAverage;
        this.monthlyAverage = monthlyAverage;
        this.lowMoodDays = lowMoodDays;
        this.highMoodDays = highMoodDays;
    }

    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Double getWeeklyAverage() {
        return weeklyAverage;
    }

    public void setWeeklyAverage(Double weeklyAverage) {
        this.weeklyAverage = weeklyAverage;
    }

    public Double getMonthlyAverage() {
        return monthlyAverage;
    }

    public void setMonthlyAverage(Double monthlyAverage) {
        this.monthlyAverage = monthlyAverage;
    }

    public Integer getLowMoodDays() {
        return lowMoodDays;
    }

    public void setLowMoodDays(Integer lowMoodDays) {
        this.lowMoodDays = lowMoodDays;
    }

    public Integer getHighMoodDays() {
        return highMoodDays;
    }

    public void setHighMoodDays(Integer highMoodDays) {
        this.highMoodDays = highMoodDays;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public String getPrimaryMood() {
        return primaryMood;
    }

    public void setPrimaryMood(String primaryMood) {
        this.primaryMood = primaryMood;
    }

    public Integer getMoodLogsCount() {
        return moodLogsCount;
    }

    public void setMoodLogsCount(Integer moodLogsCount) {
        this.moodLogsCount = moodLogsCount;
    }

    @Override
    public String toString() {
        return "MoodTrend{" +
                "userName='" + userName + '\'' +
                ", weeklyAverage=" + weeklyAverage +
                ", trend='" + trend + '\'' +
                '}';
    }
}