package com.famcare.repository;

import com.famcare.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ChatMessageRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper to convert database row to ChatMessage object
    private RowMapper<ChatMessage> chatMessageRowMapper = (rs, rowNum) -> {
        ChatMessage message = new ChatMessage();
        message.setId(rs.getInt("id"));
        message.setFamilyGroupId(rs.getInt("family_group_id"));
        message.setSenderId(rs.getInt("sender_id"));
        message.setSenderName(rs.getString("sender_name"));
        message.setMessageText(rs.getString("message_text"));
        
        Timestamp ts = rs.getTimestamp("sent_at");
        if (ts != null) {
            message.setSentAt(ts.toLocalDateTime());
        }
        
        String sentiment = rs.getString("sentiment");
        message.setSentiment(sentiment != null ? sentiment : "SAFE");
        
        return message;
    };

    /**
     * Save a new chat message
     */
    public void save(ChatMessage message) {
        String sql = "INSERT INTO chat_messages (family_group_id, sender_id, sender_name, message_text, sent_at, sentiment) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                message.getFamilyGroupId(),
                message.getSenderId(),
                message.getSenderName(),
                message.getMessageText(),
                Timestamp.valueOf(message.getSentAt()),
                message.getSentiment()
        );
    }

    /**
     * Find message by ID
     */
    public Optional<ChatMessage> findById(Integer id) {
        String sql = "SELECT * FROM chat_messages WHERE id = ?";
        try {
            ChatMessage message = jdbcTemplate.queryForObject(sql, chatMessageRowMapper, id);
            return Optional.of(message);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Get all messages for a family group (ordered by sent time)
     */
    public List<ChatMessage> findByFamilyGroupId(Integer familyGroupId) {
        String sql = "SELECT * FROM chat_messages WHERE family_group_id = ? ORDER BY sent_at ASC";
        return jdbcTemplate.query(sql, chatMessageRowMapper, familyGroupId);
    }

    /**
     * Get recent messages for a family group (last N messages)
     */
    public List<ChatMessage> findRecentMessagesByFamilyGroupId(Integer familyGroupId, int limit) {
        String sql = "SELECT * FROM chat_messages WHERE family_group_id = ? " +
                     "ORDER BY sent_at DESC LIMIT ? OFFSET " +
                     "(SELECT GREATEST(0, COUNT(*) - ?) FROM chat_messages WHERE family_group_id = ?)";
        return jdbcTemplate.query(sql, chatMessageRowMapper, familyGroupId, limit, limit, familyGroupId);
    }

    /**
     * Get messages for a family group from a specific time onwards
     */
    public List<ChatMessage> findMessagesSince(Integer familyGroupId, LocalDateTime since) {
        String sql = "SELECT * FROM chat_messages WHERE family_group_id = ? AND sent_at > ? ORDER BY sent_at ASC";
        return jdbcTemplate.query(sql, chatMessageRowMapper, familyGroupId, Timestamp.valueOf(since));
    }

    /**
     * Get message count for a family group
     */
    public int countByFamilyGroupId(Integer familyGroupId) {
        String sql = "SELECT COUNT(*) FROM chat_messages WHERE family_group_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, familyGroupId);
        return count != null ? count : 0;
    }

    /**
     * Delete a message by ID
     */
    public void deleteById(Integer id) {
        String sql = "DELETE FROM chat_messages WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    /**
     * Delete all messages for a family group
     */
    public void deleteByFamilyGroupId(Integer familyGroupId) {
        String sql = "DELETE FROM chat_messages WHERE family_group_id = ?";
        jdbcTemplate.update(sql, familyGroupId);
    }
}