package com.famcare.controller;

import com.famcare.model.FamilyInsight;
import com.famcare.model.InterventionAlert;
import com.famcare.model.MoodTrend;
import com.famcare.model.User;
import com.famcare.repository.UserRepository;
import com.famcare.service.FamilyInsightService;
import com.famcare.service.InterventionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/family")
public class FamilyController {

    @Autowired
    private FamilyInsightService familyInsightService;

    @Autowired
    private InterventionService interventionService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Check if user is logged in and is a parent
     */
    private boolean isParentLoggedIn(HttpSession session) {
        Object userRole = session.getAttribute("userRole");
        return "PARENT".equalsIgnoreCase(String.valueOf(userRole));
    }

    /**
     * Get logged-in user ID from session
     */
    private Integer getLoggedInUserId(HttpSession session) {
        Object userId = session.getAttribute("userId");
        return userId != null ? (Integer) userId : null;
    }

    /**
     * Family dashboard - shows collective wellness insights
     */
    @GetMapping("/dashboard")
    public String familyDashboard(HttpSession session, Model model) {
        if (!isParentLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer parentId = getLoggedInUserId(session);

        try {
            // Get family insights
            FamilyInsight insights = familyInsightService.getFamilyInsights(parentId);
            model.addAttribute("insights", insights);

            // Get family mood trends
            List<MoodTrend> trends = familyInsightService.getFamilyMoodTrends(parentId);
            model.addAttribute("trends", trends);

            // Get intervention alerts
            List<InterventionAlert> alerts = interventionService.generateAlerts(parentId);
            model.addAttribute("alerts", alerts);
            model.addAttribute("alertCount", alerts.size());

            // Get children
            List<User> children = userRepository.findChildrenByParentId(parentId);
            model.addAttribute("children", children);

        } catch (Exception e) {
            model.addAttribute("error", "Error loading family insights: " + e.getMessage());
        }

        return "family/dashboard";
    }

    /**
     * View detailed insights and recommendations
     */
    @GetMapping("/insights")
    public String familyInsights(HttpSession session, Model model) {
        if (!isParentLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer parentId = getLoggedInUserId(session);

        try {
            FamilyInsight insights = familyInsightService.getFamilyInsights(parentId);
            List<MoodTrend> trends = familyInsightService.getFamilyMoodTrends(parentId);
            List<User> children = userRepository.findChildrenByParentId(parentId);

            model.addAttribute("insights", insights);
            model.addAttribute("trends", trends);
            model.addAttribute("children", children);

        } catch (Exception e) {
            model.addAttribute("error", "Error loading insights: " + e.getMessage());
        }

        return "family/insights";
    }

    /**
     * View all alerts and intervention suggestions
     */
    @GetMapping("/alerts")
    public String viewAlerts(HttpSession session, Model model) {
        if (!isParentLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer parentId = getLoggedInUserId(session);

        try {
            List<InterventionAlert> alerts = interventionService.generateAlerts(parentId);

            // Group alerts by severity
            long critical = alerts.stream().filter(a -> "CRITICAL".equals(a.getSeverity())).count();
            long high = alerts.stream().filter(a -> "HIGH".equals(a.getSeverity())).count();
            long medium = alerts.stream().filter(a -> "MEDIUM".equals(a.getSeverity())).count();
            long low = alerts.stream().filter(a -> "LOW".equals(a.getSeverity())).count();

            model.addAttribute("alerts", alerts);
            model.addAttribute("criticalCount", critical);
            model.addAttribute("highCount", high);
            model.addAttribute("mediumCount", medium);
            model.addAttribute("lowCount", low);

        } catch (Exception e) {
            model.addAttribute("error", "Error loading alerts: " + e.getMessage());
        }

        return "family/alerts";
    }

    /**
     * View child-specific insights
     */
    @GetMapping("/child/{childId}/insights")
    public String childInsights(@PathVariable Integer childId, HttpSession session, Model model) {
        if (!isParentLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer parentId = getLoggedInUserId(session);

        // Verify child belongs to parent
        User child = userRepository.findById(childId).orElse(null);
        if (child == null || !child.getParentId().equals(parentId)) {
            model.addAttribute("error", "Access denied");
            return "redirect:/family/dashboard";
        }

        try {
            List<InterventionAlert> childAlerts = interventionService.getAlertsForChild(parentId, childId);
            model.addAttribute("child", child);
            model.addAttribute("alerts", childAlerts);

        } catch (Exception e) {
            model.addAttribute("error", "Error loading child insights: " + e.getMessage());
        }

        return "family/child-insights";
    }

    /**
     * Access family chat
     * Both parents and children can access this
     */
    @GetMapping("/chat")
    public String familyChat(HttpSession session, Model model) {
        Integer userId = getLoggedInUserId(session);
        
        if (userId == null) {
            return "redirect:/login";
        }

        // Redirect to actual chat page
        return "redirect:/chat/family";
    }
}