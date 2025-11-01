package com.famcare.repository;

import com.famcare.model.User;
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
     * Get all users with a specific role
     */
    public List<User> findByRole(String role) {
        String sql = "SELECT * FROM users WHERE role = ?";
        return jdbcTemplate.query(sql, userRowMapper, role);
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
     * Update user
     */
    public void update(User user) {
        String sql = "UPDATE users SET email = ?, full_name = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getEmail(), user.getFullName(), user.getId());
    }
}