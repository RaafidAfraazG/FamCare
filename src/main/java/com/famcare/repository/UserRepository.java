package com.famcare.repository;

import com.famcare.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper to convert database row to User object
    private RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        user.setFullName(rs.getString("full_name"));
        user.setParentId(rs.getInt("parent_id") == 0 ? null : rs.getInt("parent_id"));
        
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            user.setCreatedAt(ts.toLocalDateTime());
        }
        
        // NEW: Add reset token fields if they exist
        try {
            String resetToken = rs.getString("reset_token");
            user.setResetToken(resetToken);
            
            Timestamp resetTokenExpiry = rs.getTimestamp("reset_token_expiry");
            if (resetTokenExpiry != null) {
                user.setResetTokenExpiry(resetTokenExpiry.toLocalDateTime());
            }
        } catch (SQLException e) {
            // Columns don't exist yet, ignore
        }
        
        return user;
    };

    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, username);
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(Integer id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * NEW: Find user by email
     */
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * NEW: Find user by reset token
     */
    public Optional<User> findByResetToken(String resetToken) {
        String sql = "SELECT * FROM users WHERE reset_token = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, resetToken);
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Save a new user
     */
    public void save(User user) {
        String sql = "INSERT INTO users (username, password, email, role, full_name, parent_id) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getRole(),
                user.getFullName(),
                user.getParentId()
        );
    }

    /**
     * Get all children of a parent
     */
    public List<User> findChildrenByParentId(Integer parentId) {
        String sql = "SELECT * FROM users WHERE parent_id = ? AND role = 'CHILD'";
        return jdbcTemplate.query(sql, userRowMapper, parentId);
    }

    /**
     * NEW: Find by parent ID (all roles)
     */
    public List<User> findByParentId(Integer parentId) {
        String sql = "SELECT * FROM users WHERE parent_id = ?";
        return jdbcTemplate.query(sql, userRowMapper, parentId);
    }

    /**
     * Get all users with a specific role
     */
    public List<User> findByRole(String role) {
        String sql = "SELECT * FROM users WHERE role = ?";
        return jdbcTemplate.query(sql, userRowMapper, role);
    }

    /**
     * NEW: Get all users
     */
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    /**
     * Check if username exists
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    /**
     * NEW: Check if email exists
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    /**
     * Update user (basic info)
     */
    public void update(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, full_name = ?, parent_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, 
            user.getUsername(),
            user.getEmail(), 
            user.getFullName(), 
            user.getParentId(),
            user.getId()
        );
    }

    /**
     * NEW: Update user password
     */
    public void updatePassword(Integer userId, String encodedPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        jdbcTemplate.update(sql, encodedPassword, userId);
    }

    /**
     * NEW: Update user with password
     */
    public void updateWithPassword(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, email = ?, full_name = ?, parent_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, 
            user.getUsername(),
            user.getPassword(),
            user.getEmail(), 
            user.getFullName(), 
            user.getParentId(),
            user.getId()
        );
    }

    /**
     * NEW: Save/Update reset token
     */
    public void updateResetToken(Integer userId, String resetToken, LocalDateTime expiry) {
        String sql = "UPDATE users SET reset_token = ?, reset_token_expiry = ? WHERE id = ?";
        jdbcTemplate.update(sql, resetToken, expiry, userId);
    }

    /**
     * NEW: Clear reset token
     */
    public void clearResetToken(Integer userId) {
        String sql = "UPDATE users SET reset_token = NULL, reset_token_expiry = NULL WHERE id = ?";
        jdbcTemplate.update(sql, userId);
    }

    /**
     * NEW: Delete user by ID
     */
    public void deleteById(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    /**
     * NEW: Count total users
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM users";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * NEW: Count users by role
     */
    public long countByRole(String role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, role);
        return count != null ? count : 0;
    }

    // ========== ADVANCED SEARCH METHODS ==========

    /**
     * NEW: Search users by keyword (username, email, or full name)
     */
    public List<User> findByUsernameContainingOrEmailContainingOrFullNameContaining(
            String username, String email, String fullName) {
        String sql = "SELECT * FROM users WHERE " +
                    "LOWER(username) LIKE LOWER(?) OR " +
                    "LOWER(email) LIKE LOWER(?) OR " +
                    "LOWER(full_name) LIKE LOWER(?) " +
                    "ORDER BY created_at DESC";
        String searchPattern = "%" + username + "%";
        return jdbcTemplate.query(sql, userRowMapper, searchPattern, searchPattern, searchPattern);
    }

    /**
     * NEW: Search users by keyword and role
     */
    public List<User> findByUsernameContainingOrEmailContainingOrFullNameContainingAndRole(
            String username, String email, String fullName, String role) {
        String sql = "SELECT * FROM users WHERE " +
                    "(LOWER(username) LIKE LOWER(?) OR " +
                    "LOWER(email) LIKE LOWER(?) OR " +
                    "LOWER(full_name) LIKE LOWER(?)) " +
                    "AND role = ? " +
                    "ORDER BY created_at DESC";
        String searchPattern = "%" + username + "%";
        return jdbcTemplate.query(sql, userRowMapper, searchPattern, searchPattern, searchPattern, role);
    }

    /**
     * NEW: Search users by email containing
     */
    public List<User> findByEmailContaining(String email) {
        String sql = "SELECT * FROM users WHERE LOWER(email) LIKE LOWER(?) ORDER BY created_at DESC";
        String searchPattern = "%" + email + "%";
        return jdbcTemplate.query(sql, userRowMapper, searchPattern);
    }

    /**
     * NEW: Search users by username containing
     */
    public List<User> findByUsernameContaining(String username) {
        String sql = "SELECT * FROM users WHERE LOWER(username) LIKE LOWER(?) ORDER BY created_at DESC";
        String searchPattern = "%" + username + "%";
        return jdbcTemplate.query(sql, userRowMapper, searchPattern);
    }

    /**
     * NEW: Find users created between dates
     */
    public List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM users WHERE created_at BETWEEN ? AND ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, userRowMapper, start, end);
    }

    /**
     * NEW: Get recently registered users
     */
    public List<User> findTop10ByOrderByCreatedAtDesc() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC LIMIT 10";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    /**
     * NEW: Get users by role with limit
     */
    public List<User> findByRoleWithLimit(String role, int limit) {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY created_at DESC LIMIT ?";
        return jdbcTemplate.query(sql, userRowMapper, role, limit);
    }
}