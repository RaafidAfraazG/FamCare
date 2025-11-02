package com.famcare.repository;

import com.famcare.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class ChatMessageRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper to convert database row to ChatMessage object
    private RowMapper<ChatMessage> chatRowMapper = (rs, rowNum) -> {
        ChatMessage chat = new ChatMessage();
        chat.setId(rs.getInt("id"));
        chat.setFamilyId(rs.getInt("family_id"));
        chat.setUserId(rs.getInt("user_id"));
        chat.setUsername(rs.getString("username"));
        chat.setFullName(rs.getString("full_name"));
        chat.setMessage(rs.getString("message"));
        
        Timestamp ts = rs.getTimestamp("sent_at");
        if (ts != null) {
            chat.setSentAt(ts.toLocalDateTime());
        }
        
        return chat;
    };

    /**
     * Save a new chat message
     */
    public void save(ChatMessage chatMessage) {
        String sql = "INSERT INTO chat_messages (family_id, user_id, username, full_name, message) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                chatMessage.getFamilyId(),
                chatMessage.getUserId(),
                chatMessage.getUsername(),
                chatMessage.getFullName(),
                chatMessage.getMessage()
        );
    }

    /**
     * Get all chat messages for a family (most recent first)
     */
    public List<ChatMessage> findByFamilyId(Integer familyId) {
        String sql = "SELECT * FROM chat_messages WHERE family_id = ? ORDER BY sent_at ASC";
        return jdbcTemplate.query(sql, chatRowMapper, familyId);
    }

    /**
     * Get recent chat messages for a family (last N messages)
     */
    public List<ChatMessage> findRecentByFamilyId(Integer familyId, int limit) {
        String sql = "SELECT * FROM chat_messages WHERE family_id = ? ORDER BY sent_at DESC LIMIT ?";
        List<ChatMessage> messages = jdbcTemplate.query(sql, chatRowMapper, familyId, limit);
        // Reverse to show oldest first
        java.util.Collections.reverse(messages);
        return messages;
    }

    /**
     * Get messages from last N hours
     */
    public List<ChatMessage> findByFamilyIdLastNHours(Integer familyId, int hours) {
        String sql = "SELECT * FROM chat_messages WHERE family_id = ? AND sent_at >= NOW() - INTERVAL '" + hours + " hours' ORDER BY sent_at ASC";
        return jdbcTemplate.query(sql, chatRowMapper, familyId);
    }

    /**
     * Count total messages for a family
     */
    public int countByFamilyId(Integer familyId) {
        String sql = "SELECT COUNT(*) FROM chat_messages WHERE family_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, familyId);
        return count != null ? count : 0;
    }

    /**
     * Delete a message (optional - if needed)
     */
    public void deleteById(Integer id) {
        String sql = "DELETE FROM chat_messages WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    /**
     * Delete old messages (cleanup - optional)
     */
    public void deleteOlderThanDays(Integer familyId, int days) {
        String sql = "DELETE FROM chat_messages WHERE family_id = ? AND sent_at < NOW() - INTERVAL '" + days + " days'";
        jdbcTemplate.update(sql, familyId);
    }
}