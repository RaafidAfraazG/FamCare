package com.famcare.repository;

import com.famcare.model.MoodEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class MoodEntryRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper to convert database row to MoodEntry object
    private RowMapper<MoodEntry> moodRowMapper = (rs, rowNum) -> {
        MoodEntry mood = new MoodEntry();
        mood.setId(rs.getInt("id"));
        mood.setUserId(rs.getInt("user_id"));
        mood.setMoodScore(rs.getInt("mood_score"));
        mood.setMoodLabel(rs.getString("mood_label"));
        mood.setNotes(rs.getString("notes"));
        
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            mood.setCreatedAt(ts.toLocalDateTime());
        }
        
        return mood;
    };

    /**
     * Save a new mood entry
     */
    public void save(MoodEntry moodEntry) {
        String sql = "INSERT INTO mood_entries (user_id, mood_score, mood_label, notes) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                moodEntry.getUserId(),
                moodEntry.getMoodScore(),
                moodEntry.getMoodLabel(),
                moodEntry.getNotes()
        );
    }

    /**
     * Find mood entry by ID
     */
    public Optional<MoodEntry> findById(Integer id) {
        String sql = "SELECT * FROM mood_entries WHERE id = ?";
        try {
            MoodEntry mood = jdbcTemplate.queryForObject(sql, moodRowMapper, id);
            return Optional.of(mood);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Get all mood entries for a user (most recent first)
     */
    public List<MoodEntry> findByUserId(Integer userId) {
        String sql = "SELECT * FROM mood_entries WHERE user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, moodRowMapper, userId);
    }

    /**
     * Get mood entries for a user in the last 7 days
     */
    public List<MoodEntry> findByUserIdLastNDays(Integer userId, int days) {
        String sql = "SELECT * FROM mood_entries WHERE user_id = ? AND created_at >= NOW() - INTERVAL '" + days + " days' ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, moodRowMapper, userId);
    }

    /**
     * Get average mood score for a user over last N days
     */
    public Double getAverageMoodScore(Integer userId, int days) {
        String sql = "SELECT AVG(mood_score) FROM mood_entries WHERE user_id = ? AND created_at >= NOW() - INTERVAL '" + days + " days'";
        try {
            Double average = jdbcTemplate.queryForObject(sql, Double.class, userId);
            return average != null ? average : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Delete a mood entry
     */
    public void deleteById(Integer id) {
        String sql = "DELETE FROM mood_entries WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    /**
     * Count total mood entries for a user
     */
    public int countByUserId(Integer userId) {
        String sql = "SELECT COUNT(*) FROM mood_entries WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;
    }
}