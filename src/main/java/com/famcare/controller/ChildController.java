package com.famcare.controller;

import com.famcare.model.JournalEntry;
import com.famcare.repository.JournalEntryRepository;
import com.famcare.service.JournalService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/child")
public class ChildController {

    @Autowired
    private JournalService journalService;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    // ==================== HELPER METHODS ====================

    private boolean isChildLoggedIn(HttpSession session) {
        return "CHILD".equalsIgnoreCase(String.valueOf(session.getAttribute("userRole")));
    }

    private Integer getUserId(HttpSession session) {
        return (Integer) session.getAttribute("userId");
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String childDashboard(HttpSession session, Model model) {
        if (!isChildLoggedIn(session)) return "redirect:/login";

        Integer userId = getUserId(session);

        // Add journal-related stats only
        model.addAttribute("journalCount", journalService.getJournalEntryCount(userId));
        model.addAttribute("privateJournalCount", journalService.getPrivateJournalEntryCount(userId));

        return "child/dashboard";
    }

    // ==================== JOURNAL ====================

    @GetMapping("/journal")
    public String showJournalPage(HttpSession session, Model model) {
        if (!isChildLoggedIn(session)) return "redirect:/login";

        model.addAttribute("journalEntries", journalService.getUserJournalEntries(getUserId(session)));
        return "child/journal";
    }

    @PostMapping("/journal")
    public String createJournalEntry(@RequestParam String title,
                                     @RequestParam String content,
                                     @RequestParam(defaultValue = "true") Boolean isPrivate,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        if (!isChildLoggedIn(session)) return "redirect:/login";

        try {
            journalService.createJournalEntry(getUserId(session), title, content, isPrivate);
            redirectAttributes.addFlashAttribute("success", "Journal created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/child/journal";
    }

    @GetMapping("/journal/{id}")
    public String viewJournalEntry(@PathVariable Integer id, HttpSession session, Model model) {
        if (!isChildLoggedIn(session)) return "redirect:/login";

        Integer userId = getUserId(session);
        var entry = journalService.getJournalEntryById(id);

        if (entry.isEmpty() || !entry.get().getUserId().equals(userId)) {
            model.addAttribute("error", "Access denied");
            return "redirect:/child/journal";
        }

        model.addAttribute("entry", entry.get());
        return "child/journal-view";
    }

    // ==================== JOURNAL EDIT/UPDATE ====================

    @GetMapping("/journal/{journalId}/edit")
    public String showEditJournalForm(@PathVariable Integer journalId,
                                      HttpSession session,
                                      Model model) {
        if (!isChildLoggedIn(session)) return "redirect:/login";

        try {
            Integer userId = getUserId(session);
            JournalEntry journal = journalEntryRepository.findById(journalId)
                    .orElseThrow(() -> new RuntimeException("Journal not found"));

            // Security check
            if (!journal.getUserId().equals(userId)) {
                model.addAttribute("error", "Access denied");
                return "redirect:/child/journal";
            }

            model.addAttribute("journal", journal);
            return "child/edit-journal";

        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            return "redirect:/child/journal";
        }
    }

    @PostMapping("/journal/{journalId}/update")
    public String updateJournal(@PathVariable Integer journalId,
                                @RequestParam String title,
                                @RequestParam String content,
                                @RequestParam(required = false) Boolean isPrivate,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (!isChildLoggedIn(session)) return "redirect:/login";

        try {
            Integer userId = getUserId(session);
            JournalEntry journal = journalEntryRepository.findById(journalId)
                    .orElseThrow(() -> new RuntimeException("Journal not found"));

            // Security check
            if (!journal.getUserId().equals(userId)) {
                redirectAttributes.addFlashAttribute("error", "Access denied");
                return "redirect:/child/journal";
            }

            // Update
            journal.setTitle(title);
            journal.setContent(content);
            journal.setIsPrivate(isPrivate != null ? isPrivate : false);
            journal.setUpdatedAt(LocalDateTime.now());

            journalEntryRepository.save(journal);

            redirectAttributes.addFlashAttribute("success", "Journal updated successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/child/journal";
    }

    @PostMapping("/journal/{id}/delete")
    public String deleteJournalEntry(@PathVariable Integer id,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        if (!isChildLoggedIn(session)) return "redirect:/login";

        try {
            Integer userId = getUserId(session);

            if (!journalService.isJournalEntryOwnedBy(id, userId)) {
                redirectAttributes.addFlashAttribute("error", "Access denied");
                return "redirect:/child/journal";
            }

            journalService.deleteJournalEntry(id);
            redirectAttributes.addFlashAttribute("success", "Journal deleted successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/child/journal";
    }
}
