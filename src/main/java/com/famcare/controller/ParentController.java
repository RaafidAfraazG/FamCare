package com.famcare.controller;

import com.famcare.model.User;
import com.famcare.service.AuthService;
import com.famcare.service.JournalService;
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
    private JournalService journalService;

    @Autowired
    private AuthService authService;

    // ==================== HELPER METHODS ====================

    private boolean isParentLoggedIn(HttpSession session) {
        return "PARENT".equalsIgnoreCase(String.valueOf(session.getAttribute("userRole")));
    }

    private Integer getUserId(HttpSession session) {
        return (Integer) session.getAttribute("userId");
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String parentDashboard(HttpSession session, Model model) {
        if (!isParentLoggedIn(session)) return "redirect:/login";

        Integer parentId = getUserId(session);
        List<User> children = authService.findChildrenByParentId(parentId);

        model.addAttribute("children", children);
        model.addAttribute("childCount", children.size());

        return "parent/dashboard";
    }

    // ==================== CHILD JOURNAL ANALYTICS ====================

    @GetMapping("/child/{childId}/analytics")
    public String viewChildAnalytics(@PathVariable Integer childId,
                                     HttpSession session,
                                     Model model) {
        if (!isParentLoggedIn(session)) return "redirect:/login";

        Integer parentId = getUserId(session);

        // Verify access
        if (!authService.isChildOfParent(childId, parentId)) {
            model.addAttribute("error", "Access denied");
            return "redirect:/parent/dashboard";
        }

        var child = authService.getUserById(childId);
        if (child.isEmpty()) {
            model.addAttribute("error", "Child not found");
            return "redirect:/parent/dashboard";
        }

        // Journal data only (no mood)
        model.addAttribute("child", child.get());
        model.addAttribute("journalCount", journalService.getJournalEntryCount(childId));
        model.addAttribute("sharedJournalCount", journalService.getSharedJournalEntryCount(childId));
        model.addAttribute("sharedJournals", journalService.getSharedJournalEntries(childId));

        return "parent/child-analytics";
    }

    // ==================== VIEW SHARED JOURNAL ====================

    @GetMapping("/child/{childId}/journal/{journalId}")
    public String viewSharedJournalEntry(@PathVariable Integer childId,
                                         @PathVariable Integer journalId,
                                         HttpSession session,
                                         Model model) {
        if (!isParentLoggedIn(session)) return "redirect:/login";

        Integer parentId = getUserId(session);

        // Verify access
        if (!authService.isChildOfParent(childId, parentId)) {
            model.addAttribute("error", "Access denied");
            return "redirect:/parent/dashboard";
        }

        // Get journal entry
        var entry = journalService.getJournalEntryById(journalId);

        if (entry.isEmpty() || !entry.get().getUserId().equals(childId)) {
            model.addAttribute("error", "Journal not found");
            return "redirect:/parent/child/" + childId + "/analytics";
        }

        // Check if private
        if (entry.get().getIsPrivate()) {
            model.addAttribute("error", "This entry is private");
            return "redirect:/parent/child/" + childId + "/analytics";
        }

        model.addAttribute("child", authService.getUserById(childId).orElse(null));
        model.addAttribute("entry", entry.get());

        return "parent/journal-view";
    }
}
