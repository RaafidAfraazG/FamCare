package com.famcare.controller;

import com.famcare.model.FamilyDoctor;
import com.famcare.model.User;
import com.famcare.repository.UserRepository;
import com.famcare.service.FamilyDoctorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class DoctorController {

    @Autowired
    private FamilyDoctorService familyDoctorService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get logged-in user ID from session
     */
    private Integer getLoggedInUserId(HttpSession session) {
        Object userId = session.getAttribute("userId");
        return userId != null ? (Integer) userId : null;
    }

    /**
     * Get logged-in username from session or database
     */
    private String getLoggedInUsername(HttpSession session) {
        // First try to get from session
        Object username = session.getAttribute("username");
        if (username != null) {
            return (String) username;
        }
        
        // If not in session, get from database using userId
        Integer userId = getLoggedInUserId(session);
        if (userId != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                return userOpt.get().getUsername();
            }
        }
        
        return "unknown"; // Fallback
    }

    /**
     * Get user role from session
     */
    private String getUserRole(HttpSession session) {
        Object role = session.getAttribute("userRole");
        return role != null ? (String) role : null;
    }

    /**
     * Check if user is logged in
     */
    private boolean isLoggedIn(HttpSession session) {
        return getLoggedInUserId(session) != null;
    }

    /**
     * Parent view - Manage family doctors (view, add, edit, delete)
     */
    @GetMapping("/parent/doctors")
    public String parentDoctorsPage(HttpSession session, Model model) {
        if (!isLoggedIn(session) || !"PARENT".equalsIgnoreCase(getUserRole(session))) {
            return "redirect:/login";
        }

        Integer parentId = getLoggedInUserId(session);

        try {
            // Get all doctors for this family
            List<FamilyDoctor> doctors = familyDoctorService.getFamilyDoctors(parentId);
            
            model.addAttribute("doctors", doctors);
            model.addAttribute("doctorCount", doctors.size());
        } catch (Exception e) {
            model.addAttribute("error", "Error loading doctors: " + e.getMessage());
            model.addAttribute("doctors", new java.util.ArrayList<>());
            model.addAttribute("doctorCount", 0);
        }

        return "parent/doctors";
    }

    /**
     * Add a new doctor (POST)
     */
    @PostMapping("/parent/doctors/add")
    public String addDoctor(
            @RequestParam String name,
            @RequestParam(required = false) String specialization,
            @RequestParam String mobileNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String address,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!isLoggedIn(session) || !"PARENT".equalsIgnoreCase(getUserRole(session))) {
            return "redirect:/login";
        }

        Integer parentId = getLoggedInUserId(session);
        String username = getLoggedInUsername(session);

        // Debug logging
        System.out.println("DEBUG - Adding doctor:");
        System.out.println("  Parent ID: " + parentId);
        System.out.println("  Username: " + username);
        System.out.println("  Doctor Name: " + name);

        try {
            familyDoctorService.addDoctor(name, specialization, mobileNumber, email, address, parentId, username);
            redirectAttributes.addFlashAttribute("success", "Doctor added successfully!");
        } catch (Exception e) {
            System.err.println("ERROR adding doctor: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error adding doctor: " + e.getMessage());
        }

        return "redirect:/parent/doctors";
    }

    /**
     * Delete a doctor
     */
    @PostMapping("/parent/doctors/delete/{doctorId}")
    public String deleteDoctor(
            @PathVariable Integer doctorId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!isLoggedIn(session) || !"PARENT".equalsIgnoreCase(getUserRole(session))) {
            return "redirect:/login";
        }

        Integer parentId = getLoggedInUserId(session);

        try {
            // Verify doctor belongs to this family
            if (!familyDoctorService.isDoctorInFamily(doctorId, parentId)) {
                redirectAttributes.addFlashAttribute("error", "Access denied");
                return "redirect:/parent/doctors";
            }

            familyDoctorService.deleteDoctor(doctorId);
            redirectAttributes.addFlashAttribute("success", "Doctor deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting doctor: " + e.getMessage());
        }

        return "redirect:/parent/doctors";
    }

    /**
     * Update a doctor
     */
    @PostMapping("/parent/doctors/update/{doctorId}")
    public String updateDoctor(
            @PathVariable Integer doctorId,
            @RequestParam String name,
            @RequestParam(required = false) String specialization,
            @RequestParam String mobileNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String address,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!isLoggedIn(session) || !"PARENT".equalsIgnoreCase(getUserRole(session))) {
            return "redirect:/login";
        }

        Integer parentId = getLoggedInUserId(session);

        try {
            // Verify doctor belongs to this family
            if (!familyDoctorService.isDoctorInFamily(doctorId, parentId)) {
                redirectAttributes.addFlashAttribute("error", "Access denied");
                return "redirect:/parent/doctors";
            }

            familyDoctorService.updateDoctor(doctorId, name, specialization, mobileNumber, email, address);
            redirectAttributes.addFlashAttribute("success", "Doctor updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating doctor: " + e.getMessage());
        }

        return "redirect:/parent/doctors";
    }

    /**
     * Child view - View family doctors (read-only)
     */
    @GetMapping("/child/doctors")
    public String childDoctorsPage(HttpSession session, Model model) {
        if (!isLoggedIn(session) || !"CHILD".equalsIgnoreCase(getUserRole(session))) {
            return "redirect:/login";
        }

        Integer childId = getLoggedInUserId(session);

        try {
            // Get child's parent ID
            var childOptional = userRepository.findById(childId);
            if (childOptional.isEmpty() || childOptional.get().getParentId() == null) {
                model.addAttribute("error", "No parent linked to your account");
                model.addAttribute("doctors", new java.util.ArrayList<>());
                return "child/doctors";
            }

            Integer parentId = childOptional.get().getParentId();

            // Get all doctors for this family
            List<FamilyDoctor> doctors = familyDoctorService.getFamilyDoctors(parentId);
            
            model.addAttribute("doctors", doctors);
            model.addAttribute("doctorCount", doctors.size());
        } catch (Exception e) {
            model.addAttribute("error", "Error loading doctors: " + e.getMessage());
            model.addAttribute("doctors", new java.util.ArrayList<>());
            model.addAttribute("doctorCount", 0);
        }

        return "child/doctors";
    }
}