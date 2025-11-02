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
     * Show journal entry list page (READ - List all)
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
     * CREATE - Handle journal entry creation
     */
    @PostMapping("/journal")
    public String createJournalEntry(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) Boolean isPrivate,
            HttpSession session,
            Model model) {

        if (!isChildLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer userId = getLoggedInUserId(session);

        try {
            // Handle checkbox logic: if null (unchecked), default to false (shared)
            boolean privateEntry = isPrivate != null && isPrivate;
            
            journalService.createJournalEntry(userId, title, content, privateEntry);
            model.addAttribute("success", "Journal entry created successfully!");
            return "redirect:/child/journal";
        } catch (Exception e) {
            model.addAttribute("error", "Error creating journal entry: " + e.getMessage());
            List<JournalEntry> entries = journalService.getUserJournalEntries(userId);
            model.addAttribute("journalEntries", entries);
            return "child/journal";
        }
    }

    /**
     * READ - View a single journal entry
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

        try {
            var entry = journalService.getJournalEntryById(id);

            if (entry.isEmpty() || !entry.get().getUserId().equals(userId)) {
                model.addAttribute("error", "Journal entry not found or access denied");
                return "redirect:/child/journal";
            }

            model.addAttribute("entry", entry.get());
        } catch (Exception e) {
            model.addAttribute("error", "Error loading journal entry: " + e.getMessage());
            return "redirect:/child/journal";
        }

        return "child/journal-view";
    }

    /**
     * UPDATE - Show edit form for journal entry
     */
    @GetMapping("/journal/{id}/edit")
    public String showEditJournalForm(
            @PathVariable Integer id,
            HttpSession session,
            Model model) {

        if (!isChildLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer userId = getLoggedInUserId(session);

        try {
            var entry = journalService.getJournalEntryById(id);

            if (entry.isEmpty() || !entry.get().getUserId().equals(userId)) {
                model.addAttribute("error", "Journal entry not found or access denied");
                return "redirect:/child/journal";
            }

            model.addAttribute("entry", entry.get());
        } catch (Exception e) {
            model.addAttribute("error", "Error loading journal entry: " + e.getMessage());
            return "redirect:/child/journal";
        }

        return "child/journal-edit";
    }

    /**
     * UPDATE - Handle journal entry update
     */
    @PostMapping("/journal/{id}/update")
    public String updateJournalEntry(
            @PathVariable Integer id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) Boolean isPrivate,
            HttpSession session,
            Model model) {

        if (!isChildLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer userId = getLoggedInUserId(session);

        try {
            // Verify ownership
            if (!journalService.isJournalEntryOwnedBy(id, userId)) {
                model.addAttribute("error", "Access denied");
                return "redirect:/child/journal";
            }

            // Handle checkbox logic
            boolean privateEntry = isPrivate != null && isPrivate;
            
            journalService.updateJournalEntry(id, title, content, privateEntry);
            
            // Reload the updated entry to show success
            var updatedEntry = journalService.getJournalEntryById(id);
            if (updatedEntry.isPresent()) {
                model.addAttribute("entry", updatedEntry.get());
                model.addAttribute("success", "Journal entry updated successfully!");
                return "child/journal-view";
            }
            
            return "redirect:/child/journal";
        } catch (Exception e) {
            model.addAttribute("error", "Error updating journal entry: " + e.getMessage());
            var entry = journalService.getJournalEntryById(id);
            if (entry.isPresent()) {
                model.addAttribute("entry", entry.get());
            }
            return "child/journal-edit";
        }
    }

    /**
     * DELETE - Handle journal entry deletion
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

        try {
            if (!journalService.isJournalEntryOwnedBy(id, userId)) {
                model.addAttribute("error", "Access denied");
                return "redirect:/child/journal";
            }

            journalService.deleteJournalEntry(id);
            model.addAttribute("success", "Journal entry deleted successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Error deleting journal entry: " + e.getMessage());
        }

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