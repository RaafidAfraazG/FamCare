package com.famcare.model;

import java.time.LocalDateTime;

public class ChatMessage {
    private Integer id;
    private Integer familyGroupId;
    private Integer senderId;
    private String senderName;
    private String messageText;
    private LocalDateTime sentAt;
    private String sentiment; // SAFE, CONCERNING, HARMFUL

    // Constructors
    public ChatMessage() {
    }

    public ChatMessage(Integer familyGroupId, Integer senderId, String senderName, String messageText) {
        this.familyGroupId = familyGroupId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.sentAt = LocalDateTime.now();
        this.sentiment = "SAFE";
    }

    public ChatMessage(Integer familyGroupId, Integer senderId, String senderName, String messageText, String sentiment) {
        this.familyGroupId = familyGroupId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.sentAt = LocalDateTime.now();
        this.sentiment = sentiment;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFamilyGroupId() {
        return familyGroupId;
    }

    public void setFamilyGroupId(Integer familyGroupId) {
        this.familyGroupId = familyGroupId;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "id=" + id +
                ", familyGroupId=" + familyGroupId +
                ", senderId=" + senderId +
                ", senderName='" + senderName + '\'' +
                ", messageText='" + messageText + '\'' +
                ", sentAt=" + sentAt +
                ", sentiment='" + sentiment + '\'' +
                '}';
    }
}