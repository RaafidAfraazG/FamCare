package com.famcare.repository;

import com.famcare.model.MoodEntry;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class MoodEntryRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper to convert database row to MoodEntry object
    private final RowMapper<MoodEntry> rowMapper = new RowMapper<MoodEntry>() {
        @Override
        public MoodEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
            MoodEntry entry = new MoodEntry();
            entry.setId(rs.getInt("id"));
            entry.setUserId(rs.getInt("user_id"));
            entry.setMoodScore(rs.getInt("mood_score"));
            entry.setMoodLabel(rs.getString("mood_label"));
            entry.setNotes(rs.getString("notes"));
            
            if (rs.getTimestamp("created_at") != null) {
                entry.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            
            return entry;
        }
    };

    // ==================== CREATE ====================

    /**
     * Save a new mood entry
     */
    public MoodEntry save(MoodEntry entry) {
        String sql = "INSERT INTO mood_entries (user_id, mood_score, mood_label, notes, created_at) " +
                    "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        LocalDateTime now = LocalDateTime.now();
        entry.setCreatedAt(now);
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, entry.getUserId());
            ps.setInt(2, entry.getMoodScore());
            ps.setString(3, entry.getMoodLabel());
            ps.setString(4, entry.getNotes());
            ps.setObject(5, entry.getCreatedAt());
            return ps;
        }, keyHolder);
        
        entry.setId(keyHolder.getKey().intValue());
        return entry;
    }

    // ==================== READ ====================

    /**
     * Find mood entry by ID
     */
    public Optional<MoodEntry> findById(Integer id) {
        String sql = "SELECT * FROM mood_entries WHERE id = ?";
        try {
            MoodEntry entry = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.of(entry);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Find all mood entries by user ID (sorted by newest first)
     */
    public List<MoodEntry> findByUserId(Integer userId) {
        String sql = "SELECT * FROM mood_entries WHERE user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    /**
     * Find latest mood entry by user ID
     */
    public Optional<MoodEntry> findTopByUserIdOrderByCreatedAtDesc(Integer userId) {
        String sql = "SELECT * FROM mood_entries WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        try {
            MoodEntry entry = jdbcTemplate.queryForObject(sql, rowMapper, userId);
            return Optional.of(entry);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Find mood entries by mood label
     */
    public List<MoodEntry> findByUserIdAndMoodLabel(Integer userId, String moodLabel) {
        String sql = "SELECT * FROM mood_entries WHERE user_id = ? AND mood_label = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, userId, moodLabel);
    }

    /**
     * Get all mood entries (for admin)
     */
    public List<MoodEntry> findAll() {
        String sql = "SELECT * FROM mood_entries ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    // ==================== COUNT ====================

    /**
     * Count mood entries by user ID
     */
    public int countByUserId(Integer userId) {
        String sql = "SELECT COUNT(*) FROM mood_entries WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }

    // ==================== STATISTICS & ANALYTICS ====================

    /**
     * Get average mood score for a user (last N days)
     */
    public Double getAverageMoodScore(Integer userId, int days) {
        String sql = "SELECT AVG(mood_score) FROM mood_entries WHERE user_id = ? " +
                    "AND created_at >= DATE_SUB(NOW(), INTERVAL ? DAY)";
        Double avg = jdbcTemplate.queryForObject(sql, Double.class, userId, days);
        return avg != null ? avg : 0.0;
    }

    /**
     * Get mood distribution (count by mood label)
     */
    public List<Map<String, Object>> getMoodDistribution(Integer userId, int days) {
        String sql = "SELECT mood_label, COUNT(*) as count " +
                    "FROM mood_entries " +
                    "WHERE user_id = ? AND created_at >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                    "GROUP BY mood_label " +
                    "ORDER BY count DESC";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("mood_label", rs.getString("mood_label"));
            map.put("count", rs.getLong("count"));
            return map;
        }, userId, days);
    }

    /**
     * Get daily average mood scores
     */
    public List<Map<String, Object>> getDailyAverageMoodScores(Integer userId, int days) {
        String sql = "SELECT DATE(created_at) as date, AVG(mood_score) as avg_score " +
                    "FROM mood_entries " +
                    "WHERE user_id = ? AND created_at >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                    "GROUP BY DATE(created_at) " +
                    "ORDER BY date";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", rs.getDate("date"));
            map.put("avg_score", rs.getDouble("avg_score"));
            return map;
        }, userId, days);
    }

    // ==================== SEARCH & FILTER ====================

    /**
     * Search mood entries with filters
     */
    public List<MoodEntry> searchWithFilters(Integer userId, String keyword,
                                             LocalDateTime startDate, 
                                             LocalDateTime endDate,
                                             Integer minScore,
                                             Integer maxScore) {
        StringBuilder sql = new StringBuilder("SELECT * FROM mood_entries WHERE user_id = ?");
        List<Object> params = new java.util.ArrayList<>();
        params.add(userId);

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(notes) LIKE ? OR LOWER(mood_label) LIKE ?)");
            String likePattern = "%" + keyword.toLowerCase() + "%";
            params.add(likePattern);
            params.add(likePattern);
        }

        if (startDate != null) {
            sql.append(" AND created_at >= ?");
            params.add(startDate);
        }

        if (endDate != null) {
            sql.append(" AND created_at <= ?");
            params.add(endDate);
        }

        if (minScore != null) {
            sql.append(" AND mood_score >= ?");
            params.add(minScore);
        }

        if (maxScore != null) {
            sql.append(" AND mood_score <= ?");
            params.add(maxScore);
        }

        sql.append(" ORDER BY created_at DESC");

        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    /**
     * Search mood entries by keyword in notes
     */
    public List<MoodEntry> searchByKeyword(Integer userId, String keyword) {
        String sql = "SELECT * FROM mood_entries WHERE user_id = ? " +
                    "AND (LOWER(notes) LIKE ? OR LOWER(mood_label) LIKE ?) " +
                    "ORDER BY created_at DESC";
        String likePattern = "%" + keyword.toLowerCase() + "%";
        return jdbcTemplate.query(sql, rowMapper, userId, likePattern, likePattern);
    }

    /**
     * Find mood entries from last N days
     */
    public List<MoodEntry> findByUserIdLastNDays(Integer userId, int days) {
        String sql = "SELECT * FROM mood_entries WHERE user_id = ? " +
                    "AND created_at >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                    "ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, userId, days);
    }

    /**
     * Get recent mood entries with limit
     */
    public List<MoodEntry> findRecentByUserId(Integer userId, int limit) {
        String sql = "SELECT * FROM mood_entries WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, userId, limit);
    }

    // ==================== DELETE ====================

    /**
     * Delete mood entry by ID
     */
    public void deleteById(Integer id) {
        String sql = "DELETE FROM mood_entries WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    /**
     * Delete all mood entries by user ID
     */
    public void deleteByUserId(Integer userId) {
        String sql = "DELETE FROM mood_entries WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Check if mood entry exists
     */
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM mood_entries WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    /**
     * Check if user owns the mood entry
     */
    public boolean isOwnedByUser(Integer moodId, Integer userId) {
        String sql = "SELECT COUNT(*) FROM mood_entries WHERE id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, moodId, userId);
        return count != null && count > 0;
    }
}