package com.famcare.service;

import com.famcare.model.User;
import com.famcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register a new user (Parent or Child)
     * 
     * @param username - unique username
     * @param password - plain text password
     * @param email - user email
     * @param role - "PARENT" or "CHILD"
     * @param fullName - user's full name
     * @param parentId - parent ID if registering as child (can be null for parents)
     * @return true if registration successful, false if username already exists
     */
    public boolean registerUser(String username, String password, String email, String role, String fullName, Integer parentId) {
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            return false; // Username already taken
        }

        // Create new user
        User user = new User(username, password, email, role, fullName, parentId);
        
        // Encode password (hash it for security)
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);

        // Save to database
        userRepository.save(user);
        return true;
    }

    /**
     * Authenticate user - verify username and password
     * 
     * @param username - username
     * @param password - plain text password
     * @return User object if credentials match, null otherwise
     */
    public User authenticateUser(String username, String password) {
        // Find user by username
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            return null; // User not found
        }

        User user = optionalUser.get();

        // Check if password matches (compare plain password with encoded password)
        if (passwordEncoder.matches(password, user.getPassword())) {
            return user; // Authentication successful
        }

        return null; // Password incorrect
    }

    /**
     * Get user by username
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Get user by ID
     */
    public Optional<User> getUserById(Integer userId) {
        return userRepository.findById(userId);
    }

    /**
     * Check if user is a parent
     */
    public boolean isParent(User user) {
        return user != null && "PARENT".equalsIgnoreCase(user.getRole());
    }

    /**
     * Check if user is a child
     */
    public boolean isChild(User user) {
        return user != null && "CHILD".equalsIgnoreCase(user.getRole());
    }

    /**
     * Validate if child belongs to this parent
     */
    public boolean isChildOfParent(Integer childId, Integer parentId) {
        Optional<User> optionalChild = userRepository.findById(childId);
        
        if (optionalChild.isEmpty()) {
            return false;
        }

        User child = optionalChild.get();
        return parentId.equals(child.getParentId());
    }

    /**
     * Get all children of a parent
     */
    public java.util.List<User> findChildrenByParentId(Integer parentId) {
        return userRepository.findChildrenByParentId(parentId);
    }
}