package com.famcare.service;

import com.famcare.model.JournalEntry;
import com.famcare.repository.JournalEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JournalService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    /**
     * Create a new journal entry
     * 
     * @param userId - ID of the user writing the journal
     * @param title - journal entry title
     * @param content - journal entry content
     * @param isPrivate - true if private (only child sees), false if shareable (parent can see)
     */
    public void createJournalEntry(Integer userId, String title, String content, Boolean isPrivate) {
        // Validate input
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }

        JournalEntry journalEntry = new JournalEntry(userId, title, content, isPrivate);
        journalEntryRepository.save(journalEntry);
    }

    /**
     * Get all journal entries for a user
     */
    public List<JournalEntry> getUserJournalEntries(Integer userId) {
        return journalEntryRepository.findByUserId(userId);
    }

    /**
     * Get only private journal entries for a user (child's personal entries)
     */
    public List<JournalEntry> getPrivateJournalEntries(Integer userId) {
        return journalEntryRepository.findPrivateByUserId(userId);
    }

    /**
     * Get shareable journal entries for a user (parent can view)
     */
    public List<JournalEntry> getSharedJournalEntries(Integer userId) {
        return journalEntryRepository.findSharedByUserId(userId);
    }

    /**
     * Get a specific journal entry
     */
    public Optional<JournalEntry> getJournalEntryById(Integer id) {
        return journalEntryRepository.findById(id);
    }

    /**
     * Update a journal entry
     */
    public void updateJournalEntry(Integer id, String title, String content, Boolean isPrivate) {
        Optional<JournalEntry> optionalEntry = journalEntryRepository.findById(id);
        
        if (optionalEntry.isEmpty()) {
            throw new IllegalArgumentException("Journal entry not found");
        }

        JournalEntry journalEntry = optionalEntry.get();
        journalEntry.setTitle(title);
        journalEntry.setContent(content);
        journalEntry.setIsPrivate(isPrivate);

        journalEntryRepository.update(journalEntry);
    }

    /**
     * Delete a journal entry
     */
    public void deleteJournalEntry(Integer id) {
        journalEntryRepository.deleteById(id);
    }

    /**
     * Get journal entries from last N days
     */
    public List<JournalEntry> getJournalEntriesLastNDays(Integer userId, int days) {
        return journalEntryRepository.findByUserIdLastNDays(userId, days);
    }

    /**
     * Get journal entry count for a user
     */
    public int getJournalEntryCount(Integer userId) {
        return journalEntryRepository.countByUserId(userId);
    }

    /**
     * Get private journal entry count
     */
    public int getPrivateJournalEntryCount(Integer userId) {
        return getPrivateJournalEntries(userId).size();
    }

    /**
     * Get shared journal entry count
     */
    public int getSharedJournalEntryCount(Integer userId) {
        return getSharedJournalEntries(userId).size();
    }

    /**
     * Check if a journal entry belongs to a user
     */
    public boolean isJournalEntryOwnedBy(Integer journalId, Integer userId) {
        Optional<JournalEntry> optionalEntry = journalEntryRepository.findById(journalId);
        
        if (optionalEntry.isEmpty()) {
            return false;
        }

        return optionalEntry.get().getUserId().equals(userId);
    }

    /**
     * Check if parent can view a child's journal entry
     */
    public boolean canParentViewEntry(Integer journalId) {
        Optional<JournalEntry> optionalEntry = journalEntryRepository.findById(journalId);
        
        if (optionalEntry.isEmpty()) {
            return false;
        }

        // Parent can only view shared (non-private) entries
        return !optionalEntry.get().getIsPrivate();
    }
}