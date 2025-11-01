package com.famcare.model;

public class FamilyInsight {
    private Integer familyId;
    private Integer parentId;
    private Integer totalMembers;
    private Double familyAverageMood;
    private Integer lowMoodCount; // Members with mood < 3
    private Integer goodMoodCount; // Members with mood >= 7
    private String familyWellnessStatus; // "Excellent", "Good", "Fair", "Poor"
    private String dominantMood; // Most common mood label
    private String trend; // "Improving", "Declining", "Stable"
    private String recommendation; // Wellness recommendation

    // Constructors
    public FamilyInsight() {
    }

    public FamilyInsight(Integer familyId, Integer parentId, Integer totalMembers, 
                         Double familyAverageMood, Integer lowMoodCount, Integer goodMoodCount) {
        this.familyId = familyId;
        this.parentId = parentId;
        this.totalMembers = totalMembers;
        this.familyAverageMood = familyAverageMood;
        this.lowMoodCount = lowMoodCount;
        this.goodMoodCount = goodMoodCount;
    }

    // Getters and Setters
    public Integer getFamilyId() {
        return familyId;
    }

    public void setFamilyId(Integer familyId) {
        this.familyId = familyId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(Integer totalMembers) {
        this.totalMembers = totalMembers;
    }

    public Double getFamilyAverageMood() {
        return familyAverageMood;
    }

    public void setFamilyAverageMood(Double familyAverageMood) {
        this.familyAverageMood = familyAverageMood;
    }

    public Integer getLowMoodCount() {
        return lowMoodCount;
    }

    public void setLowMoodCount(Integer lowMoodCount) {
        this.lowMoodCount = lowMoodCount;
    }

    public Integer getGoodMoodCount() {
        return goodMoodCount;
    }

    public void setGoodMoodCount(Integer goodMoodCount) {
        this.goodMoodCount = goodMoodCount;
    }

    public String getFamilyWellnessStatus() {
        return familyWellnessStatus;
    }

    public void setFamilyWellnessStatus(String familyWellnessStatus) {
        this.familyWellnessStatus = familyWellnessStatus;
    }

    public String getDominantMood() {
        return dominantMood;
    }

    public void setDominantMood(String dominantMood) {
        this.dominantMood = dominantMood;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    @Override
    public String toString() {
        return "FamilyInsight{" +
                "parentId=" + parentId +
                ", familyAverageMood=" + familyAverageMood +
                ", wellnessStatus='" + familyWellnessStatus + '\'' +
                '}';
    }
}