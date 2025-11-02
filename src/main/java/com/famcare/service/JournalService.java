package com.famcare.service;

import com.famcare.model.JournalEntry;
import com.famcare.repository.JournalEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class JournalService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    /**
     * Create a new journal entry
     */
    @Transactional
    public JournalEntry createJournalEntry(Integer userId, String title, String content, Boolean isPrivate) {
        JournalEntry entry = new JournalEntry(userId, title, content, isPrivate);
        return journalEntryRepository.save(entry);
    }

    /**
     * Update an existing journal entry
     */
    @Transactional
    public JournalEntry updateJournalEntry(Integer journalId, String title, String content, Boolean isPrivate) {
        JournalEntry entry = journalEntryRepository.findById(journalId)
            .orElseThrow(() -> new RuntimeException("Journal entry not found"));
        
        entry.setTitle(title);
        entry.setContent(content);
        entry.setIsPrivate(isPrivate);
        entry.setUpdatedAt(LocalDateTime.now());
        
        return journalEntryRepository.save(entry);
    }

    /**
     * Get a single journal entry by ID
     */
    public JournalEntry getJournalEntryById(Integer journalId) {
        return journalEntryRepository.findById(journalId)
            .orElseThrow(() -> new RuntimeException("Journal entry not found"));
    }

    /**
     * Get all journal entries for a user
     */
    public List<JournalEntry> getUserJournalEntries(Integer userId) {
        return journalEntryRepository.findByUserId(userId);
    }

    /**
     * Get shared journal entries (visible to parents)
     */
    public List<JournalEntry> getSharedJournalEntries(Integer userId) {
        return journalEntryRepository.findSharedByUserId(userId);
    }

    /**
     * Get private journal entries (only child can see)
     */
    public List<JournalEntry> getPrivateJournalEntries(Integer userId) {
        return journalEntryRepository.findPrivateByUserId(userId);
    }

    /**
     * Delete a journal entry
     */
    @Transactional
    public void deleteJournalEntry(Integer journalId) {
        journalEntryRepository.deleteById(journalId);
    }

    /**
     * Get journal entry count
     */
    public Integer getJournalEntryCount(Integer userId) {
        return journalEntryRepository.countByUserId(userId);
    }

    /**
     * Get shared journal count
     */
    public Integer getSharedJournalEntryCount(Integer userId) {
        List<JournalEntry> shared = journalEntryRepository.findSharedByUserId(userId);
        return shared.size();
    }

    /**
     * Get private journal count
     */
    public Integer getPrivateJournalEntryCount(Integer userId) {
        List<JournalEntry> privateEntries = journalEntryRepository.findPrivateByUserId(userId);
        return privateEntries.size();
    }

    /**
     * Get journals from last N days
     */
    public List<JournalEntry> getUserJournalEntriesLastNDays(Integer userId, int days) {
        return journalEntryRepository.findByUserIdLastNDays(userId, days);
    }

    /**
     * Advanced search with filters
     */
    public List<JournalEntry> searchJournalsWithFilters(Integer userId, String keyword,
                                                         Boolean isPrivate, 
                                                         LocalDate startDate, 
                                                         LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;
        
        return journalEntryRepository.searchWithFilters(userId, keyword, start, end, isPrivate);
    }

    /**
     * Search user's journals by keyword
     */
    public List<JournalEntry> searchUserJournals(Integer userId, String keyword) {
        return searchJournalsWithFilters(userId, keyword, null, null, null);
    }

    /**
     * Get recent journal entries
     */
    public List<JournalEntry> getRecentJournalEntries(Integer userId, int limit) {
        return journalEntryRepository.findRecentByUserId(userId, limit);
    }

    public boolean isJournalEntryOwnedBy(Integer id, Integer userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isJournalEntryOwnedBy'");
    }
}