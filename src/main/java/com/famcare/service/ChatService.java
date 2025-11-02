package com.famcare.service;

import com.famcare.model.ChatMessage;
import com.famcare.model.User;
import com.famcare.repository.ChatMessageRepository;
import com.famcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Send a chat message
     */
    public void sendMessage(Integer familyId, Integer userId, String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        // Get user details
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();

        ChatMessage chatMessage = new ChatMessage(
            familyId,
            userId,
            user.getUsername(),
            user.getFullName(),
            message.trim()
        );

        chatMessageRepository.save(chatMessage);
    }

    /**
     * Get all messages for a family
     */
    public List<ChatMessage> getFamilyMessages(Integer familyId) {
        return chatMessageRepository.findByFamilyId(familyId);
    }

    /**
     * Get recent messages for a family
     */
    public List<ChatMessage> getRecentFamilyMessages(Integer familyId, int limit) {
        return chatMessageRepository.findRecentByFamilyId(familyId, limit);
    }

    /**
     * Get messages from last N hours
     */
    public List<ChatMessage> getMessagesFromLastHours(Integer familyId, int hours) {
        return chatMessageRepository.findByFamilyIdLastNHours(familyId, hours);
    }

    /**
     * Get total message count for a family
     */
    public int getMessageCount(Integer familyId) {
        return chatMessageRepository.countByFamilyId(familyId);
    }

    /**
     * Delete a message
     */
    public void deleteMessage(Integer messageId) {
        chatMessageRepository.deleteById(messageId);
    }

    /**
     * Get family ID for a user (parent ID or user's own ID if parent)
     */
    public Integer getFamilyIdForUser(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();
        
        // If user is a parent, family ID is their own ID
        if ("PARENT".equalsIgnoreCase(user.getRole())) {
            return user.getId();
        }
        
        // If user is a child, family ID is their parent's ID
        return user.getParentId();
    }

    /**
     * Check if user belongs to this family
     */
    public boolean canAccessFamilyChat(Integer userId, Integer familyId) {
        Integer userFamilyId = getFamilyIdForUser(userId);
        return userFamilyId != null && userFamilyId.equals(familyId);
    }
}