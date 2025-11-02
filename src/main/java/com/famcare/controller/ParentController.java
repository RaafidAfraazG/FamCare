package com.famcare.controller;

import com.famcare.model.JournalEntry;
import com.famcare.model.MoodEntry;
import com.famcare.model.User;
import com.famcare.repository.UserRepository;
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
     * Parent dashboard - shows overview of children
     */
    @GetMapping("/dashboard")
    public String parentDashboard(HttpSession session, Model model) {
        if (!isParentLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer parentId = getLoggedInUserId(session);

        try {
            // Get all children of this parent
            List<User> children = userRepository.findChildrenByParentId(parentId);
            
            model.addAttribute("children", children);
            model.addAttribute("childCount", children.size());
        } catch (Exception e) {
            model.addAttribute("error", "Error loading children: " + e.getMessage());
            model.addAttribute("children", new java.util.ArrayList<>());
            model.addAttribute("childCount", 0);
        }

        return "parent/dashboard";
    }

    /**
     * View a child's mood analytics
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

        try {
            // Verify child belongs to this parent
            var childOptional = userRepository.findById(childId);
            if (childOptional.isEmpty() || !childOptional.get().getParentId().equals(parentId)) {
                model.addAttribute("error", "Access denied");
                return "redirect:/parent/dashboard";
            }

            User child = childOptional.get();

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

            model.addAttribute("child", child);
            model.addAttribute("moodCount", moodCount);
            model.addAttribute("weeklyAverage", weeklyAverage);
            model.addAttribute("monthlyAverage", monthlyAverage);
            model.addAttribute("journalCount", journalCount);
            model.addAttribute("sharedJournalCount", sharedJournalCount);
            model.addAttribute("recentMoods", recentMoods);
            model.addAttribute("sharedJournals", sharedJournals);

        } catch (Exception e) {
            model.addAttribute("error", "Error loading child analytics: " + e.getMessage());
            return "redirect:/parent/dashboard";
        }

        return "parent/child-analytics";
    }

    /**
     * View a child's mood chart
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

        try {
            // Verify child belongs to this parent
            var childOptional = userRepository.findById(childId);
            if (childOptional.isEmpty() || !childOptional.get().getParentId().equals(parentId)) {
                model.addAttribute("error", "Access denied");
                return "redirect:/parent/dashboard";
            }

            User child = childOptional.get();

            // Get mood data for the child
            List<MoodEntry> moodHistory = moodService.getUserMoodHistoryLastNDays(childId, 30);
            Double weeklyAverage = moodService.getWeeklyAverageMoodScore(childId);
            Double monthlyAverage = moodService.getMonthlyAverageMoodScore(childId);
            Integer moodCount = moodService.getMoodEntryCount(childId);

            model.addAttribute("child", child);
            model.addAttribute("moodHistory", moodHistory);
            model.addAttribute("weeklyAverage", weeklyAverage);
            model.addAttribute("monthlyAverage", monthlyAverage);
            model.addAttribute("moodCount", moodCount);

        } catch (Exception e) {
            model.addAttribute("error", "Error loading mood chart: " + e.getMessage());
            return "redirect:/parent/dashboard";
        }

        return "parent/mood-chart";
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

        try {
            // Verify child belongs to this parent
            var childOptional = userRepository.findById(childId);
            if (childOptional.isEmpty() || !childOptional.get().getParentId().equals(parentId)) {
                model.addAttribute("error", "Access denied");
                return "redirect:/parent/dashboard";
            }

            // Get journal entry
            var entryOptional = journalService.getJournalEntryById(journalId);

            if (entryOptional.isEmpty() || !entryOptional.get().getUserId().equals(childId)) {
                model.addAttribute("error", "Journal entry not found");
                return "redirect:/parent/child/" + childId + "/analytics";
            }

            JournalEntry entry = entryOptional.get();

            // Check if entry is shared (not private)
            if (entry.getIsPrivate()) {
                model.addAttribute("error", "This entry is private");
                return "redirect:/parent/child/" + childId + "/analytics";
            }

            User child = childOptional.get();
            model.addAttribute("child", child);
            model.addAttribute("entry", entry);

        } catch (Exception e) {
            model.addAttribute("error", "Error loading journal entry: " + e.getMessage());
            return "redirect:/parent/dashboard";
        }

        return "parent/journal-view";
    }

    /**
     * REST API: Get mood data as JSON for charts (AJAX)
     */
    @GetMapping("/api/child/{childId}/mood-data")
    @ResponseBody
    public MoodDataResponse getMoodDataForChart(
            @PathVariable Integer childId,
            HttpSession session) {

        if (!isParentLoggedIn(session)) {
            return new MoodDataResponse();
        }

        Integer parentId = getLoggedInUserId(session);

        try {
            // Verify child belongs to this parent
            var childOptional = userRepository.findById(childId);
            if (childOptional.isEmpty() || !childOptional.get().getParentId().equals(parentId)) {
                return new MoodDataResponse();
            }

            // Get mood data
            List<MoodEntry> moods = moodService.getUserMoodHistoryLastNDays(childId, 30);

            MoodDataResponse response = new MoodDataResponse();
            moods.forEach(mood -> {
                response.labels.add(mood.getCreatedAt().toString());
                response.scores.add(mood.getMoodScore());
            });

            return response;

        } catch (Exception e) {
            System.err.println("Error getting mood data: " + e.getMessage());
            return new MoodDataResponse();
        }
    }

    /**
     * Helper class for mood data JSON response
     */
    public static class MoodDataResponse {
        public java.util.List<String> labels = new java.util.ArrayList<>();
        public java.util.List<Integer> scores = new java.util.ArrayList<>();

        public java.util.List<String> getLabels() {
            return labels;
        }

        public java.util.List<Integer> getScores() {
            return scores;
        }
    }
}