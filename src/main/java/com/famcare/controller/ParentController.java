package com.famcare.controller;

import com.famcare.model.JournalEntry;
import com.famcare.model.MoodEntry;
import com.famcare.model.User;
import com.famcare.service.AuthService;
import com.famcare.service.JournalService;
import com.famcare.service.MoodService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/parent")
public class ParentController {

    @Autowired
    private MoodService moodService;

    @Autowired
    private JournalService journalService;

    @Autowired
    private AuthService authService;

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
     * Parent dashboard - shows overview of children
     */
    @GetMapping("/dashboard")
    public String parentDashboard(HttpSession session, Model model) {
        if (!isParentLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer parentId = getLoggedInUserId(session);

        // Get all children of this parent
        List<User> children = authService.getUserById(parentId)
                .map(user -> new java.util.ArrayList<>(
                        authService.findChildrenByParentId(parentId)
                ))
                .orElse(new java.util.ArrayList<>());

        model.addAttribute("children", children);
        model.addAttribute("childCount", children.size());

        return "parent/dashboard";
    }

    /**
     * View a child's mood analytics
     */
    @GetMapping("/child/{childId}/mood-chart")
    public String viewChildMoodChart(
            @PathVariable Integer childId,
            HttpSession session,
            Model model) {

        if (!isParentLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer parentId = getLoggedInUserId(session);

        // Verify child belongs to this parent
        if (!authService.isChildOfParent(childId, parentId)) {
            model.addAttribute("error", "Access denied");
            return "redirect:/parent/dashboard";
        }

        // Get child details
        var child = authService.getUserById(childId);
        if (child.isEmpty()) {
            model.addAttribute("error", "Child not found");
            return "redirect:/parent/dashboard";
        }

        // Get mood data for the child
        List<MoodEntry> moodHistory = moodService.getUserMoodHistoryLastNDays(childId, 30);
        Double weeklyAverage = moodService.getWeeklyAverageMoodScore(childId);
        Double monthlyAverage = moodService.getMonthlyAverageMoodScore(childId);
        Integer moodCount = moodService.getMoodEntryCount(childId);

        model.addAttribute("child", child.get());
        model.addAttribute("moodHistory", moodHistory);
        model.addAttribute("weeklyAverage", weeklyAverage);
        model.addAttribute("monthlyAverage", monthlyAverage);
        model.addAttribute("moodCount", moodCount);

        return "parent/mood-chart";
    }

    /**
     * View a child's analytics summary
     */
    @GetMapping("/child/{childId}/analytics")
    public String viewChildAnalytics(
            @PathVariable Integer childId,
            HttpSession session,
            Model model) {

        if (!isParentLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer parentId = getLoggedInUserId(session);

        // Verify child belongs to this parent
        if (!authService.isChildOfParent(childId, parentId)) {
            model.addAttribute("error", "Access denied");
            return "redirect:/parent/dashboard";
        }

        // Get child details
        var child = authService.getUserById(childId);
        if (child.isEmpty()) {
            model.addAttribute("error", "Child not found");
            return "redirect:/parent/dashboard";
        }

        // Get analytics
        Integer moodCount = moodService.getMoodEntryCount(childId);
        Double weeklyAverage = moodService.getWeeklyAverageMoodScore(childId);
        Double monthlyAverage = moodService.getMonthlyAverageMoodScore(childId);
        Integer journalCount = journalService.getJournalEntryCount(childId);
        Integer sharedJournalCount = journalService.getSharedJournalEntryCount(childId);

        // Get recent mood entries (last 7 days)
        List<MoodEntry> recentMoods = moodService.getUserMoodHistoryLastNDays(childId, 7);

        // Get shared journal entries (parent can view)
        List<JournalEntry> sharedJournals = journalService.getSharedJournalEntries(childId);

        model.addAttribute("child", child.get());
        model.addAttribute("moodCount", moodCount);
        model.addAttribute("weeklyAverage", weeklyAverage);
        model.addAttribute("monthlyAverage", monthlyAverage);
        model.addAttribute("journalCount", journalCount);
        model.addAttribute("sharedJournalCount", sharedJournalCount);
        model.addAttribute("recentMoods", recentMoods);
        model.addAttribute("sharedJournals", sharedJournals);

        return "parent/child-analytics";
    }

    /**
     * View a shared journal entry
     */
    @GetMapping("/child/{childId}/journal/{journalId}")
    public String viewSharedJournalEntry(
            @PathVariable Integer childId,
            @PathVariable Integer journalId,
            HttpSession session,
            Model model) {

        if (!isParentLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer parentId = getLoggedInUserId(session);

        // Verify child belongs to this parent
        if (!authService.isChildOfParent(childId, parentId)) {
            model.addAttribute("error", "Access denied");
            return "redirect:/parent/dashboard";
        }

        // Get journal entry
        var entry = journalService.getJournalEntryById(journalId);

        if (entry.isEmpty() || !entry.get().getUserId().equals(childId)) {
            model.addAttribute("error", "Journal entry not found");
            return "redirect:/parent/child/" + childId + "/analytics";
        }

        // Check if entry is shared (not private)
        if (entry.get().getIsPrivate()) {
            model.addAttribute("error", "This entry is private");
            return "redirect:/parent/child/" + childId + "/analytics";
        }

        var child = authService.getUserById(childId);
        model.addAttribute("child", child.orElse(null));
        model.addAttribute("entry", entry.get());

        return "parent/journal-view";
    }

    /**
     * REST API: Get mood data as JSON for charts
     */
    @GetMapping("/api/child/{childId}/mood-data")
    @ResponseBody
    public MoodDataResponse getMoodDataForChart(@PathVariable Integer childId, HttpSession session) {
        if (!isParentLoggedIn(session)) {
            return null;
        }

        Integer parentId = getLoggedInUserId(session);

        if (!authService.isChildOfParent(childId, parentId)) {
            return null;
        }

        List<MoodEntry> moods = moodService.getUserMoodHistoryLastNDays(childId, 30);

        MoodDataResponse response = new MoodDataResponse();
        moods.forEach(mood -> {
            response.labels.add(mood.getCreatedAt().toString());
            response.scores.add(mood.getMoodScore());
        });

        return response;
    }

    /**
     * Helper class for JSON response
     */
    public static class MoodDataResponse {
        public java.util.List<String> labels = new java.util.ArrayList<>();
        public java.util.List<Integer> scores = new java.util.ArrayList<>();

        public java.util.List<String> getLabels() { return labels; }
        public java.util.List<Integer> getScores() { return scores; }
    }

    /**
     * Find helper method in AuthService
     */
    private List<User> findChildrenByParentId(Integer parentId) {
        return authService.findChildrenByParentId(parentId);
    }
}