package com.famcare.repository;

import com.famcare.model.JournalEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JournalEntryRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper to convert database row to JournalEntry object
    private final RowMapper<JournalEntry> rowMapper = new RowMapper<JournalEntry>() {
        @Override
        public JournalEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
            JournalEntry entry = new JournalEntry();
            entry.setId(rs.getInt("id"));
            entry.setUserId(rs.getInt("user_id"));
            entry.setTitle(rs.getString("title"));
            entry.setContent(rs.getString("content"));
            entry.setIsPrivate(rs.getBoolean("is_private"));
            
            if (rs.getTimestamp("created_at") != null) {
                entry.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            
            if (rs.getTimestamp("updated_at") != null) {
                entry.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            }
            
            return entry;
        }
    };

    // ==================== CREATE & UPDATE ====================

    /**
     * Save (create new or update existing) journal entry
     */
    public JournalEntry save(JournalEntry entry) {
        if (entry.getId() == null) {
            // Insert new entry
            String sql = "INSERT INTO journal_entries (user_id, title, content, is_private, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            
            LocalDateTime now = LocalDateTime.now();
            entry.setCreatedAt(now);
            entry.setUpdatedAt(now);
            
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, entry.getUserId());
                ps.setString(2, entry.getTitle());
                ps.setString(3, entry.getContent());
                ps.setBoolean(4, entry.getIsPrivate());
                ps.setObject(5, entry.getCreatedAt());
                ps.setObject(6, entry.getUpdatedAt());
                return ps;
            }, keyHolder);
            
            entry.setId(keyHolder.getKey().intValue());
        } else {
            // Update existing entry
            String sql = "UPDATE journal_entries SET title = ?, content = ?, is_private = ?, updated_at = ? WHERE id = ?";
            entry.setUpdatedAt(LocalDateTime.now());
            
            jdbcTemplate.update(sql, 
                entry.getTitle(), 
                entry.getContent(), 
                entry.getIsPrivate(), 
                entry.getUpdatedAt(),
                entry.getId()
            );
        }
        return entry;
    }

    // ==================== READ ====================

    /**
     * Find journal entry by ID
     */
    public Optional<JournalEntry> findById(Integer id) {
        String sql = "SELECT * FROM journal_entries WHERE id = ?";
        try {
            JournalEntry entry = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.of(entry);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Find all journals by user ID (sorted by newest first)
     */
    public List<JournalEntry> findByUserId(Integer userId) {
        String sql = "SELECT * FROM journal_entries WHERE user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    /**
     * Find private journals by user ID
     */
    public List<JournalEntry> findPrivateByUserId(Integer userId) {
        String sql = "SELECT * FROM journal_entries WHERE user_id = ? AND is_private = true ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    /**
     * Find shared (non-private) journals by user ID
     */
    public List<JournalEntry> findSharedByUserId(Integer userId) {
        String sql = "SELECT * FROM journal_entries WHERE user_id = ? AND is_private = false ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    /**
     * Get all journal entries (for admin)
     */
    public List<JournalEntry> findAll() {
        String sql = "SELECT * FROM journal_entries ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    // ==================== COUNT ====================

    /**
     * Count journals by user ID
     */
    public int countByUserId(Integer userId) {
        String sql = "SELECT COUNT(*) FROM journal_entries WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    /**
     * Count private journals
     */
    public int countPrivateByUserId(Integer userId) {
        String sql = "SELECT COUNT(*) FROM journal_entries WHERE user_id = ? AND is_private = true";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    /**
     * Count shared journals
     */
    public int countSharedByUserId(Integer userId) {
        String sql = "SELECT COUNT(*) FROM journal_entries WHERE user_id = ? AND is_private = false";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    // ==================== SEARCH & FILTER ====================

    /**
     * Search journals with filters
     */
    public List<JournalEntry> searchWithFilters(Integer userId, String keyword, 
                                                 LocalDateTime startDate, 
                                                 LocalDateTime endDate, 
                                                 Boolean isPrivate) {
        StringBuilder sql = new StringBuilder("SELECT * FROM journal_entries WHERE user_id = ?");
        List<Object> params = new java.util.ArrayList<>();
        params.add(userId);

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(title) LIKE ? OR LOWER(content) LIKE ?)");
            String likePattern = "%" + keyword.toLowerCase() + "%";
            params.add(likePattern);
            params.add(likePattern);
        }

        if (isPrivate != null) {
            sql.append(" AND is_private = ?");
            params.add(isPrivate);
        }

        if (startDate != null) {
            sql.append(" AND created_at >= ?");
            params.add(startDate);
        }

        if (endDate != null) {
            sql.append(" AND created_at <= ?");
            params.add(endDate);
        }

        sql.append(" ORDER BY created_at DESC");

        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    /**
     * Find journals from last N days
     */
    public List<JournalEntry> findByUserIdLastNDays(Integer userId, int days) {
        String sql = "SELECT * FROM journal_entries WHERE user_id = ? " +
                    "AND created_at >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                    "ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, userId, days);
    }

    /**
     * Get recent journal entries with limit
     */
    public List<JournalEntry> findRecentByUserId(Integer userId, int limit) {
        String sql = "SELECT * FROM journal_entries WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, userId, limit);
    }

    // ==================== DELETE ====================

    /**
     * Delete journal entry by ID
     */
    public void deleteById(Integer id) {
        String sql = "DELETE FROM journal_entries WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    /**
     * Delete all journals by user ID
     */
    public void deleteByUserId(Integer userId) {
        String sql = "DELETE FROM journal_entries WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Check if journal entry exists
     */
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM journal_entries WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    /**
     * Check if user owns the journal entry
     */
    public boolean isOwnedByUser(Integer journalId, Integer userId) {
        String sql = "SELECT COUNT(*) FROM journal_entries WHERE id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, journalId, userId);
        return count != null && count > 0;
    }
}