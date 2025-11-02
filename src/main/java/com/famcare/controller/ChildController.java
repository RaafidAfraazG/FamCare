package com.famcare.controller;

import com.famcare.model.JournalEntry;
import com.famcare.model.MoodEntry;
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
@RequestMapping("/child")
public class ChildController {

    @Autowired
    private MoodService moodService;

    @Autowired
    private JournalService journalService;

    @Autowired
    private AuthService authService;

    /**
     * Check if user is logged in and is a child
     */
    private boolean isChildLoggedIn(HttpSession session) {
        Object userRole = session.getAttribute("userRole");
        return "CHILD".equalsIgnoreCase(String.valueOf(userRole));
    }

    /**
     * Get logged-in user ID from session
     */
    private Integer getLoggedInUserId(HttpSession session) {
        Object userId = session.getAttribute("userId");
        return userId != null ? (Integer) userId : null;
    }

    /**
     * Child dashboard - shows overview
     */
    @GetMapping("/dashboard")
    public String childDashboard(HttpSession session, Model model) {
        if (!isChildLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer userId = getLoggedInUserId(session);

        // Get latest mood entry
        var latestMood = moodService.getLatestMoodEntry(userId);
        if (latestMood.isPresent()) {
            model.addAttribute("latestMood", latestMood.get());
            model.addAttribute("moodCategory", moodService.getMoodCategory(latestMood.get().getMoodScore()));
        }

        // Get mood entry count
        model.addAttribute("moodCount", moodService.getMoodEntryCount(userId));

        // Get journal entry count
        model.addAttribute("journalCount", journalService.getJournalEntryCount(userId));

        // Get private journal count
        model.addAttribute("privateJournalCount", journalService.getPrivateJournalEntryCount(userId));

        // Get weekly average mood
        model.addAttribute("weeklyAverage", moodService.getWeeklyAverageMoodScore(userId));

        // Get recent moods (last 7)
        List<MoodEntry> recentMoods = moodService.getUserMoodHistoryLastNDays(userId, 7);
        model.addAttribute("recentMoods", recentMoods);

        return "child/dashboard";
    }

    /**
     * Show mood logging page
     */
    @GetMapping("/mood-log")
    public String showMoodLogPage(HttpSession session) {
        if (!isChildLoggedIn(session)) {
            return "redirect:/login";
        }
        return "child/mood-log";
    }

    /**
     * Handle mood submission
     */
    @PostMapping("/mood-log")
    public String logMood(
            @RequestParam Integer moodScore,
            @RequestParam String moodLabel,
            @RequestParam(required = false) String notes,
            HttpSession session,
            Model model) {

        if (!isChildLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer userId = getLoggedInUserId(session);

        try {
            // Log the mood
            moodService.logMood(userId, moodScore, moodLabel, notes);
            model.addAttribute("success", "Mood logged successfully!");
            return "redirect:/child/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Error logging mood: " + e.getMessage());
            return "child/mood-log";
        }
    }

    /**
     * Show journal entry page
     */
    @GetMapping("/journal")
    public String showJournalPage(HttpSession session, Model model) {
        if (!isChildLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer userId = getLoggedInUserId(session);

        // Get all journal entries for this child
        List<JournalEntry> entries = journalService.getUserJournalEntries(userId);
        model.addAttribute("journalEntries", entries);

        return "child/journal";
    }

    /**
     * Handle journal entry creation
     */
    @PostMapping("/journal")
    public String createJournalEntry(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "true") Boolean isPrivate,
            HttpSession session,
            Model model) {

        if (!isChildLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer userId = getLoggedInUserId(session);

        try {
            journalService.createJournalEntry(userId, title, content, isPrivate);
            model.addAttribute("success", "Journal entry created successfully!");
            return "redirect:/child/journal";
        } catch (Exception e) {
            model.addAttribute("error", "Error creating journal entry: " + e.getMessage());
            Integer userId2 = getLoggedInUserId(session);
            List<JournalEntry> entries = journalService.getUserJournalEntries(userId2);
            model.addAttribute("journalEntries", entries);
            return "child/journal";
        }
    }

    /**
     * View a single journal entry
     */
    @GetMapping("/journal/{id}")
    public String viewJournalEntry(
            @PathVariable Integer id,
            HttpSession session,
            Model model) {

        if (!isChildLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer userId = getLoggedInUserId(session);

        var entry = journalService.getJournalEntryById(id);

        if (entry.isEmpty() || !entry.get().getUserId().equals(userId)) {
            model.addAttribute("error", "Journal entry not found or access denied");
            return "redirect:/child/journal";
        }

        model.addAttribute("entry", entry.get());
        return "child/journal-view";
    }

    /**
     * Delete a journal entry
     */
    @PostMapping("/journal/{id}/delete")
    public String deleteJournalEntry(
            @PathVariable Integer id,
            HttpSession session,
            Model model) {

        if (!isChildLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer userId = getLoggedInUserId(session);

        if (!journalService.isJournalEntryOwnedBy(id, userId)) {
            model.addAttribute("error", "Access denied");
            return "redirect:/child/journal";
        }

        journalService.deleteJournalEntry(id);
        return "redirect:/child/journal";
    }

    /**
     * View mood history
     */
    @GetMapping("/mood-history")
    public String viewMoodHistory(HttpSession session, Model model) {
        if (!isChildLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer userId = getLoggedInUserId(session);

        List<MoodEntry> moodHistory = moodService.getUserMoodHistory(userId);
        model.addAttribute("moodHistory", moodHistory);

        return "child/mood-history";
    }
}