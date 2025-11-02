package com.famcare.controller;

import com.famcare.model.ChatMessage;
import com.famcare.model.User;
import com.famcare.service.ChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * Get logged-in user ID from session
     */
    private Integer getLoggedInUserId(HttpSession session) {
        Object userId = session.getAttribute("userId");
        return userId != null ? (Integer) userId : null;
    }

    /**
     * Get family group ID (parent ID) from session
     * For parents: their own ID
     * For children: their parent's ID
     */
    private Integer getFamilyGroupId(HttpSession session) {
        Object userRole = session.getAttribute("userRole");
        Object userId = session.getAttribute("userId");
        
        if (userId == null) {
            return null;
        }

        // If user is parent, family group ID is their ID
        if ("PARENT".equalsIgnoreCase(String.valueOf(userRole))) {
            return (Integer) userId;
        }

        // If user is child, we need to get their parent ID from session
        // (This would need to be set during login or fetched from DB)
        Object parentId = session.getAttribute("parentId");
        return parentId != null ? (Integer) parentId : null;
    }

    /**
     * Display family chat page
     */
    @GetMapping("/family")
    public String familyChat(HttpSession session, Model model) {
        Integer userId = getLoggedInUserId(session);
        Integer familyGroupId = getFamilyGroupId(session);

        if (userId == null || familyGroupId == null) {
            return "redirect:/login";
        }

        // Verify user is family member
        if (!chatService.isFamilyMember(userId, familyGroupId)) {
            model.addAttribute("error", "Access denied");
            return "redirect:/";
        }

        try {
            // Get all messages for this family
            List<ChatMessage> messages = chatService.getFamilyMessages(familyGroupId);
            
            // Get family members
            List<User> familyMembers = chatService.getFamilyMembers(familyGroupId);

            model.addAttribute("messages", messages != null ? messages : java.util.Collections.emptyList());
            model.addAttribute("familyMembers", familyMembers != null ? familyMembers : java.util.Collections.emptyList());
            model.addAttribute("loggedInUserId", userId);
            model.addAttribute("messageCount", messages != null ? messages.size() : 0);

        } catch (Exception e) {
            model.addAttribute("error", "Error loading chat: " + e.getMessage());
            model.addAttribute("messages", java.util.Collections.emptyList());
            model.addAttribute("familyMembers", java.util.Collections.emptyList());
            model.addAttribute("messageCount", 0);
        }

        return "chat/family-chat";
    }

    /**
     * Send a message (AJAX endpoint)
     */
    @PostMapping("/send-message")
    @ResponseBody
    public ResponseEntity<?> sendMessage(
            @RequestParam String messageText,
            HttpSession session) {

        Integer userId = getLoggedInUserId(session);
        Integer familyGroupId = getFamilyGroupId(session);

        // Validate user is logged in
        if (userId == null || familyGroupId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("User not logged in"));
        }

        // Verify user is family member
        if (!chatService.isFamilyMember(userId, familyGroupId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Access denied"));
        }

        try {
            ChatMessage message = chatService.sendMessage(familyGroupId, userId, messageText);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get all messages for family (AJAX endpoint)
     */
    @GetMapping("/get-messages")
    @ResponseBody
    public ResponseEntity<?> getMessages(HttpSession session) {
        Integer userId = getLoggedInUserId(session);
        Integer familyGroupId = getFamilyGroupId(session);

        if (userId == null || familyGroupId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Collections.emptyList());
        }

        if (!chatService.isFamilyMember(userId, familyGroupId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(java.util.Collections.emptyList());
        }

        try {
            List<ChatMessage> messages = chatService.getFamilyMessages(familyGroupId);
            return ResponseEntity.ok(messages != null ? messages : java.util.Collections.emptyList());
        } catch (Exception e) {
            System.err.println("Error getting messages: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }

    /**
     * Get recent messages (AJAX endpoint with limit)
     */
    @GetMapping("/get-recent-messages")
    @ResponseBody
    public ResponseEntity<?> getRecentMessages(
            @RequestParam(defaultValue = "50") int limit,
            HttpSession session) {

        Integer userId = getLoggedInUserId(session);
        Integer familyGroupId = getFamilyGroupId(session);

        if (userId == null || familyGroupId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("User not logged in"));
        }

        if (!chatService.isFamilyMember(userId, familyGroupId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Access denied"));
        }

        try {
            List<ChatMessage> messages = chatService.getRecentFamilyMessages(familyGroupId, limit);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get family members (AJAX endpoint)
     */
    @GetMapping("/get-family-members")
    @ResponseBody
    public ResponseEntity<?> getFamilyMembers(HttpSession session) {
        Integer userId = getLoggedInUserId(session);
        Integer familyGroupId = getFamilyGroupId(session);

        if (userId == null || familyGroupId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("User not logged in"));
        }

        if (!chatService.isFamilyMember(userId, familyGroupId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Access denied"));
        }

        try {
            List<User> members = chatService.getFamilyMembers(familyGroupId);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Delete a message (AJAX endpoint)
     */
    @DeleteMapping("/delete-message/{messageId}")
    @ResponseBody
    public ResponseEntity<?> deleteMessage(
            @PathVariable Integer messageId,
            HttpSession session) {

        Integer userId = getLoggedInUserId(session);
        Integer familyGroupId = getFamilyGroupId(session);

        if (userId == null || familyGroupId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("User not logged in"));
        }

        if (!chatService.isFamilyMember(userId, familyGroupId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("Access denied"));
        }

        try {
            chatService.deleteMessage(messageId, userId);
            return ResponseEntity.ok(createSuccessResponse("Message deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Helper method to create error response
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return response;
    }

    /**
     * Helper method to create success response
     */
    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        return response;
    }
}