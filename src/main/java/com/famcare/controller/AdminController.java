package com.famcare.controller;

import com.famcare.model.User;
import com.famcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    /**
     * DELETE - Delete a user by ID
     */
    @PostMapping("/delete-user/{userId}")
    public String deleteUser(
            @PathVariable Integer userId,
            Model model) {

        try {
            // Check if user exists
            var userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                model.addAttribute("error", "User not found!");
                return "redirect:/admin/users";
            }

            User user = userOptional.get();
            String deletedUsername = user.getUsername();
            String deletedUserRole = user.getRole();

            // Delete the user
            userRepository.deleteById(userId);

            System.out.println("✅ User deleted: " + deletedUsername + " (" + deletedUserRole + ")");
            model.addAttribute("success", "User '" + deletedUsername + "' deleted successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error deleting user: " + e.getMessage());
            model.addAttribute("error", "Error deleting user: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }
}