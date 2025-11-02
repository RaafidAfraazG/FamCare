package com.famcare.controller;

import com.famcare.model.User;
import com.famcare.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Show home page
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * Show login page
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    /**
     * Handle login form submission
     */
    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        // Authenticate user
        User user = authService.authenticateUser(username, password);

        if (user == null) {
            // Login failed
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }

        // Store user in session
        session.setAttribute("loggedInUser", user);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userRole", user.getRole());
        session.setAttribute("userName", user.getFullName());

        // Set parent ID for family chat access
        // For parents: their own ID
        // For children: their parent's ID
        if ("CHILD".equalsIgnoreCase(user.getRole())) {
            Integer parentId = user.getParentId();
            session.setAttribute("parentId", parentId);
            System.out.println("Child login - ParentId set to: " + parentId);
        } else {
            session.setAttribute("parentId", user.getId());
            System.out.println("Parent login - ParentId set to: " + user.getId());
        }

        // Redirect based on role
        if ("PARENT".equalsIgnoreCase(user.getRole())) {
            return "redirect:/parent/dashboard";
        } else if ("CHILD".equalsIgnoreCase(user.getRole())) {
            return "redirect:/child/dashboard";
        }

        return "redirect:/";
    }

    /**
     * Show registration page (for demonstration)
     * In real app, you'd need separate pages for parent/child registration
     */
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // We'll create this template later
    }

    /**
     * Handle parent registration
     */
    @PostMapping("/register/parent")
    public String registerParent(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String fullName,
            Model model) {

        boolean success = authService.registerUser(username, password, email, "PARENT", fullName, null);

        if (!success) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }

        model.addAttribute("success", "Parent account created! Please login.");
        return "login";
    }

    /**
     * Handle child registration
     */
    @PostMapping("/register/child")
    public String registerChild(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String fullName,
            @RequestParam Integer parentId,
            Model model) {

        // Verify parent exists
        if (authService.getUserById(parentId).isEmpty()) {
            model.addAttribute("error", "Parent ID not found");
            return "register";
        }

        boolean success = authService.registerUser(username, password, email, "CHILD", fullName, parentId);

        if (!success) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }

        model.addAttribute("success", "Child account created! Please login.");
        return "login";
    }

    /**
     * Handle logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clear session
        return "redirect:/";
    }

    /**
     * Check if user is logged in (helper method)
     */
    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("userId") != null;
    }
}