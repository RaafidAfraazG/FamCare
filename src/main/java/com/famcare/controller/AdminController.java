package com.famcare.controller;

import com.famcare.model.User;
import com.famcare.model.JournalEntry;
import com.famcare.repository.UserRepository;
import com.famcare.repository.JournalEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        try {
            List<User> parents = userRepository.findByRole("PARENT");
            List<User> children = userRepository.findByRole("CHILD");
            
            model.addAttribute("parents", parents);
            model.addAttribute("children", children);
            model.addAttribute("parentCount", parents.size());
            model.addAttribute("childCount", children.size());
            model.addAttribute("totalCount", parents.size() + children.size());
        } catch (Exception e) {
            model.addAttribute("error", "Error loading data: " + e.getMessage());
        }
        
        return "admin/dashboard";
    }

    // ==================== USER MANAGEMENT ====================

    @GetMapping("/users")
    public String adminUsers(Model model) {
        try {
            List<User> parents = userRepository.findByRole("PARENT");
            List<User> children = userRepository.findByRole("CHILD");
            
            model.addAttribute("parents", parents);
            model.addAttribute("children", children);
            model.addAttribute("totalUsers", parents.size() + children.size());
        } catch (Exception e) {
            model.addAttribute("error", "Error loading data: " + e.getMessage());
        }
        
        return "admin/users";
    }

    @GetMapping("/create-user")
    public String showCreateUserForm(Model model) {
        try {
            List<User> parents = userRepository.findByRole("PARENT");
            model.addAttribute("parents", parents);
        } catch (Exception e) {
            model.addAttribute("error", "Error loading parents: " + e.getMessage());
        }
        return "admin/create-user";
    }

    @PostMapping("/create-parent")
    public String createParent(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String fullName,
            Model model) {

        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "Username already exists!");
            return "admin/create-user";
        }

        try {
            User parent = new User();
            parent.setUsername(username);
            parent.setPassword(passwordEncoder.encode(password));
            parent.setEmail(email);
            parent.setRole("PARENT");
            parent.setFullName(fullName);
            parent.setParentId(null);
            
            userRepository.save(parent);
            
            model.addAttribute("success", "Parent user '" + username + "' created successfully!");
            model.addAttribute("parents", userRepository.findByRole("PARENT"));
            
            return "admin/create-user";
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            model.addAttribute("parents", userRepository.findByRole("PARENT"));
            return "admin/create-user";
        }
    }

    @PostMapping("/create-child")
    public String createChild(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String fullName,
            @RequestParam Integer parentId,
            Model model) {

        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "Username already exists!");
            model.addAttribute("parents", userRepository.findByRole("PARENT"));
            return "admin/create-user";
        }

        if (userRepository.findById(parentId).isEmpty()) {
            model.addAttribute("error", "Parent not found!");
            model.addAttribute("parents", userRepository.findByRole("PARENT"));
            return "admin/create-user";
        }

        try {
            User child = new User();
            child.setUsername(username);
            child.setPassword(passwordEncoder.encode(password));
            child.setEmail(email);
            child.setRole("CHILD");
            child.setFullName(fullName);
            child.setParentId(parentId);
            
            userRepository.save(child);
            
            model.addAttribute("success", "Child user '" + username + "' created successfully!");
            model.addAttribute("parents", userRepository.findByRole("PARENT"));
            
            return "admin/create-user";
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            model.addAttribute("parents", userRepository.findByRole("PARENT"));
            return "admin/create-user";
        }
    }

    // ==================== JOURNAL MANAGEMENT ====================

    @GetMapping("/journals")
    public String viewAllJournals(Model model) {
        try {
            List<JournalEntry> allJournals = new java.util.ArrayList<>();
            List<User> children = userRepository.findByRole("CHILD");
            
            for (User child : children) {
                List<JournalEntry> childJournals = journalEntryRepository.findByUserId(child.getId());
                // Add user info to each journal
                childJournals.forEach(j -> j.setUser(child));
                allJournals.addAll(childJournals);
            }
            
            allJournals.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            
            model.addAttribute("journals", allJournals);
            model.addAttribute("totalJournals", allJournals.size());
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading journals: " + e.getMessage());
        }
        
        return "admin/journals";
    }

    @GetMapping("/journal/{journalId}")
    public String viewJournalDetails(@PathVariable Integer journalId, Model model) {
        try {
            JournalEntry journal = journalEntryRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("Journal not found"));
            
            User user = userRepository.findById(journal.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            model.addAttribute("journal", journal);
            model.addAttribute("user", user);
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading journal: " + e.getMessage());
            return "redirect:/admin/journals";
        }
        
        return "admin/journal-details";
    }

    // ==================== JOURNAL EDIT/UPDATE ====================

    @GetMapping("/journal/{journalId}/edit")
    public String showEditJournalForm(@PathVariable Integer journalId, Model model) {
        try {
            JournalEntry journal = journalEntryRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("Journal not found"));
            
            User user = userRepository.findById(journal.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            model.addAttribute("journal", journal);
            model.addAttribute("user", user);
            
        } catch (Exception e) {
            model.addAttribute("error", "Error loading journal: " + e.getMessage());
            return "redirect:/admin/journals";
        }
        
        return "admin/edit-journal";
    }

    @PostMapping("/journal/{journalId}/update")
    public String updateJournal(
            @PathVariable Integer journalId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) Boolean isPrivate,
            RedirectAttributes redirectAttributes) {
        
        try {
            JournalEntry journal = journalEntryRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("Journal not found"));
            
            // Update journal details
            journal.setTitle(title);
            journal.setContent(content);
            journal.setIsPrivate(isPrivate != null ? isPrivate : false);
            journal.setUpdatedAt(LocalDateTime.now());
            
            journalEntryRepository.save(journal);
            
            redirectAttributes.addFlashAttribute("success", "Journal updated successfully!");
            return "redirect:/admin/journal/" + journalId;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating journal: " + e.getMessage());
            return "redirect:/admin/journals";
        }
    }

    @PostMapping("/journal/{journalId}/delete")
    public String deleteJournal(@PathVariable Integer journalId, RedirectAttributes redirectAttributes) {
        try {
            journalEntryRepository.deleteById(journalId);
            redirectAttributes.addFlashAttribute("success", "Journal deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting journal: " + e.getMessage());
        }
        
        return "redirect:/admin/journals";
    }
}