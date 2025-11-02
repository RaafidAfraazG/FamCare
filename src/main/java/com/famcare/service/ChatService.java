package com.famcare.service;

import com.famcare.model.ChatMessage;
import com.famcare.model.User;
import com.famcare.repository.ChatMessageRepository;
import com.famcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    private SentimentAnalysisService sentimentAnalysisService = new SentimentAnalysisService();

    /**
     * Send a message in family chat
     * Validates that the sender is part of the family
     */
    public ChatMessage sendMessage(Integer familyGroupId, Integer senderId, String messageText) {
        // Validate sender
        Optional<User> optionalSender = userRepository.findById(senderId);
        if (optionalSender.isEmpty()) {
            throw new RuntimeException("Sender not found");
        }

        User sender = optionalSender.get();
        
        // Validate message is not empty
        if (messageText == null || messageText.trim().isEmpty()) {
            throw new RuntimeException("Message cannot be empty");
        }

        // Analyze sentiment
        SentimentAnalysisService.MessageSentiment sentiment = sentimentAnalysisService.analyzeSentiment(messageText);
        
        // Create and save message with sentiment analysis
        ChatMessage message = new ChatMessage(familyGroupId, senderId, sender.getFullName(), messageText.trim(), sentiment.severity);
        chatMessageRepository.save(message);
        
        // Log if harmful message detected
        if (sentiment.isHarmful) {
            System.out.println("⚠️ HARMFUL MESSAGE DETECTED from " + sender.getFullName() + ": " + sentiment.severity);
            System.out.println("   Message: " + messageText);
        }
        
        return message;
    }

    /**
     * Get all messages for a family group
     */
    public List<ChatMessage> getFamilyMessages(Integer familyGroupId) {
        return chatMessageRepository.findByFamilyGroupId(familyGroupId);
    }

    /**
     * Get last N messages for a family group
     */
    public List<ChatMessage> getRecentFamilyMessages(Integer familyGroupId, int limit) {
        return chatMessageRepository.findRecentMessagesByFamilyGroupId(familyGroupId, limit);
    }

    /**
     * Get messages since a specific time
     */
    public List<ChatMessage> getFamilyMessagesSince(Integer familyGroupId, LocalDateTime since) {
        return chatMessageRepository.findMessagesSince(familyGroupId, since);
    }

    /**
     * Get total message count for family
     */
    public int getFamilyChatMessageCount(Integer familyGroupId) {
        return chatMessageRepository.countByFamilyGroupId(familyGroupId);
    }

    /**
     * Get all family members (parent + all children)
     * Family group ID = parent ID
     */
    public List<User> getFamilyMembers(Integer parentId) {
        try {
            // Get parent
            Optional<User> optionalParent = userRepository.findById(parentId);
            if (optionalParent.isEmpty()) {
                System.err.println("Parent not found with ID: " + parentId);
                return new java.util.ArrayList<>();
            }

            // Get all children of parent
            List<User> familyMembers = userRepository.findChildrenByParentId(parentId);
            
            // Add parent to the list
            familyMembers.add(0, optionalParent.get());
            
            return familyMembers;
        } catch (Exception e) {
            System.err.println("Error getting family members: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Check if user is member of family (can access chat)
     * A user is family member if:
     * - They are the parent (familyGroupId = parentId)
     * - They are a child and their parentId matches the familyGroupId
     */
    public boolean isFamilyMember(Integer userId, Integer familyGroupId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        
        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();

        // If user is parent and their ID matches familyGroupId
        if ("PARENT".equalsIgnoreCase(user.getRole()) && user.getId().equals(familyGroupId)) {
            return true;
        }

        // If user is child and their parentId matches familyGroupId
        if ("CHILD".equalsIgnoreCase(user.getRole()) && familyGroupId.equals(user.getParentId())) {
            return true;
        }

        return false;
    }

    /**
     * Delete a message (only sender can delete)
     */
    public boolean deleteMessage(Integer messageId, Integer userId) {
        Optional<ChatMessage> optionalMessage = chatMessageRepository.findById(messageId);
        
        if (optionalMessage.isEmpty()) {
            return false;
        }

        ChatMessage message = optionalMessage.get();
        
        // Only sender can delete their message
        if (!message.getSenderId().equals(userId)) {
            throw new RuntimeException("You can only delete your own messages");
        }

        chatMessageRepository.deleteById(messageId);
        return true;
    }

    /**
     * Get message count for pagination
     */
    public int getTotalMessageCount(Integer familyGroupId) {
        return chatMessageRepository.countByFamilyGroupId(familyGroupId);
    }
}