package com.famcare.repository;

import com.famcare.model.JournalEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class JournalEntryRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper to convert database row to JournalEntry object
    private RowMapper<JournalEntry> journalRowMapper = (rs, rowNum) -> {
        JournalEntry journal = new JournalEntry();
        journal.setId(rs.getInt("id"));
        journal.setUserId(rs.getInt("user_id"));
        journal.setTitle(rs.getString("title"));
        journal.setContent(rs.getString("content"));
        journal.setIsPrivate(rs.getBoolean("is_private"));
        
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            journal.setCreatedAt(createdTs.toLocalDateTime());
        }
        
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) {
            journal.setUpdatedAt(updatedTs.toLocalDateTime());
        }
        
        return journal;
    };

    /**
     * Save a new journal entry
     */
    public void save(JournalEntry journalEntry) {
        String sql = "INSERT INTO journal_entries (user_id, title, content, is_private) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                journalEntry.getUserId(),
                journalEntry.getTitle(),
                journalEntry.getContent(),
                journalEntry.getIsPrivate()
        );
    }

    /**
     * Find journal entry by ID
     */
    public Optional<JournalEntry> findById(Integer id) {
        String sql = "SELECT * FROM journal_entries WHERE id = ?";
        try {
            JournalEntry journal = jdbcTemplate.queryForObject(sql, journalRowMapper, id);
            return Optional.of(journal);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Get all journal entries for a user (most recent first)
     */
    public List<JournalEntry> findByUserId(Integer userId) {
        String sql = "SELECT * FROM journal_entries WHERE user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, journalRowMapper, userId);
    }

    /**
     * Get only private journal entries for a user (child's personal entries)
     */
    public List<JournalEntry> findPrivateByUserId(Integer userId) {
        String sql = "SELECT * FROM journal_entries WHERE user_id = ? AND is_private = true ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, journalRowMapper, userId);
    }

    /**
     * Get only shared journal entries for a user (parent can view)
     */
    public List<JournalEntry> findSharedByUserId(Integer userId) {
        String sql = "SELECT * FROM journal_entries WHERE user_id = ? AND is_private = false ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, journalRowMapper, userId);
    }

    /**
     * Update a journal entry
     */
    public void update(JournalEntry journalEntry) {
        String sql = "UPDATE journal_entries SET title = ?, content = ?, is_private = ?, updated_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql,
                journalEntry.getTitle(),
                journalEntry.getContent(),
                journalEntry.getIsPrivate(),
                journalEntry.getId()
        );
    }

    /**
     * Delete a journal entry
     */
    public void deleteById(Integer id) {
        String sql = "DELETE FROM journal_entries WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    /**
     * Count total journal entries for a user
     */
    public int countByUserId(Integer userId) {
        String sql = "SELECT COUNT(*) FROM journal_entries WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    /**
     * Get journal entries for a user from the last N days
     */
    public List<JournalEntry> findByUserIdLastNDays(Integer userId, int days) {
        String sql = "SELECT * FROM journal_entries WHERE user_id = ? AND created_at >= NOW() - INTERVAL '" + days + " days' ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, journalRowMapper, userId);
    }
}