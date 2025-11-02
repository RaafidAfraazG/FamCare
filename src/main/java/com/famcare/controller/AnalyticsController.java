package com.famcare.controller;

import com.famcare.service.AnalyticsService;
import com.famcare.service.MoodService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * Get detailed analytics for a user (dashboard view)
     */
    @GetMapping("/user/{userId}")
    public Map<String, Object> getUserAnalytics(@PathVariable Integer userId) {
        return analyticsService.getUserAnalyticsDashboard(userId);
    }

    /**
     * Get only intervention suggestion
     */
    @GetMapping("/user/{userId}/suggestion")
    public String getUserIntervention(@PathVariable Integer userId) {
        return analyticsService.getInterventionSuggestion(userId);
    }

    @GetMapping("/user/{userId}/advanced")
    public Map<String, Object> getUserAdvancedAnalytics(@PathVariable Integer userId) {
        return analyticsService.getAdvancedAnalytics(userId);
    }

    @PostMapping("/family")
    public Map<String, Object> getFamilyAnalytics(@RequestBody List<Integer> childIds) {
        return analyticsService.getFamilyAnalytics(childIds);
    }

    
    

    
}
