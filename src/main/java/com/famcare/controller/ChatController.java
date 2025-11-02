package com.famcare.controller;

import com.famcare.model.ChatMessage;
import com.famcare.model.User;
import com.famcare.service.ChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * Check if user is logged in
     */
    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("userId") != null;
    }

    /**
     * Show family chatroom
     */
    @GetMapping("/family")
    public String showFamilyChat(HttpSession session, Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer userId = getLoggedInUserId(session);
        Integer familyId = chatService.getFamilyIdForUser(userId);

        if (familyId == null) {
            model.addAttribute("error", "Unable to access family chat");
            return "redirect:/";
        }

        // Get recent messages (last 100)
        List<ChatMessage> messages = chatService.getRecentFamilyMessages(familyId, 100);
        int totalMessages = chatService.getMessageCount(familyId);

        model.addAttribute("messages", messages);
        model.addAttribute("totalMessages", totalMessages);
        model.addAttribute("familyId", familyId);
        model.addAttribute("currentUserId", userId);

        // Get user info
        String userName = (String) session.getAttribute("userName");
        String userRole = (String) session.getAttribute("userRole");
        
        model.addAttribute("userName", userName);
        model.addAttribute("userRole", userRole);

        return "chat/family-chat";
    }

    /**
     * Send a message
     */
    @PostMapping("/send")
    public String sendMessage(
            @RequestParam String message,
            HttpSession session,
            Model model) {

        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        Integer userId = getLoggedInUserId(session);
        Integer familyId = chatService.getFamilyIdForUser(userId);

        if (familyId == null) {
            model.addAttribute("error", "Unable to send message");
            return "redirect:/chat/family";
        }

        try {
            chatService.sendMessage(familyId, userId, message);
        } catch (Exception e) {
            model.addAttribute("error", "Error sending message: " + e.getMessage());
        }

        return "redirect:/chat/family";
    }

    /**
     * API endpoint to get messages (for auto-refresh)
     */
    @GetMapping("/api/messages")
    @ResponseBody
    public List<ChatMessage> getMessages(HttpSession session) {
        if (!isLoggedIn(session)) {
            return null;
        }

        Integer userId = getLoggedInUserId(session);
        Integer familyId = chatService.getFamilyIdForUser(userId);

        if (familyId == null) {
            return null;
        }

        return chatService.getRecentFamilyMessages(familyId, 100);
    }

    /**
     * API endpoint to check for new messages
     */
    @GetMapping("/api/check-new")
    @ResponseBody
    public MessageCountResponse checkNewMessages(
            @RequestParam(required = false, defaultValue = "0") Integer lastCount,
            HttpSession session) {

        if (!isLoggedIn(session)) {
            return new MessageCountResponse(0, false);
        }

        Integer userId = getLoggedInUserId(session);
        Integer familyId = chatService.getFamilyIdForUser(userId);

        if (familyId == null) {
            return new MessageCountResponse(0, false);
        }

        int currentCount = chatService.getMessageCount(familyId);
        boolean hasNew = currentCount > lastCount;

        return new MessageCountResponse(currentCount, hasNew);
    }

    /**
     * Helper class for JSON response
     */
    public static class MessageCountResponse {
        private int count;
        private boolean hasNew;

        public MessageCountResponse(int count, boolean hasNew) {
            this.count = count;
            this.hasNew = hasNew;
        }

        public int getCount() {
            return count;
        }

        public boolean isHasNew() {
            return hasNew;
        }
    }
}