package com.mediconnect.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediconnect.dto.UserDTO;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.model.ERole;
import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import com.mediconnect.repository.RoleRepository;
import com.mediconnect.repository.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
    
    /**
     * Get user by email
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Search users by keyword
     */
    public List<User> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword);
    }
    
    /**
     * Get users by role
     */
    public List<User> getUsersByRole(ERole roleName) {
        return userRepository.findByRolesName(roleName);
    }
    
    /**
     * Get recent users by role
     */
    public List<User> getRecentUsersByRole(ERole roleName, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return userRepository.findRecentUsersByRole(roleName, pageable);
    }
    
    /**
     * Get active users by role
     */
    public List<User> getActiveUsersByRole(ERole roleName) {
        return userRepository.findActiveUsersByRole(roleName);
    }
    
    /**
     * Get users by status (enabled/disabled)
     */
    public List<User> getUsersByStatus(boolean status) {
        return userRepository.findByStatus(status);
    }
    
    /**
     * Create new user
     */
    @Transactional
    public User createUser(UserDTO userDTO) {
        // Check if email already exists
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }
        
        // Check if phone number already exists
        if (userDTO.getPhoneNumber() != null && 
            !userDTO.getPhoneNumber().isEmpty() && 
            userRepository.existsByPhoneNumber(userDTO.getPhoneNumber())) {
            throw new BadRequestException("Phone number is already in use!");
        }
        
        // Create new user
        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setEnabled(true);
        
        // Set roles
        Set<Role> roles = new HashSet<>();
        if (userDTO.getRoles() == null || userDTO.getRoles().isEmpty()) {
            // Default to USER role if none specified
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            userDTO.getRoles().forEach(role -> {
                Role userRole = roleRepository.findByName(role)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            });
        }
        
        user.setRoles(roles);
        return userRepository.save(user);
    }
    
    /**
     * Update existing user
     */
    @Transactional
    public User updateUser(Long id, UserDTO userDTO) {
        User user = getUserById(id);
        
        // Check if email is being changed and already exists
        if (userDTO.getEmail() != null && 
            !user.getEmail().equals(userDTO.getEmail()) && 
            userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }
        
        // Check if phone number is being changed and already exists
        if (userDTO.getPhoneNumber() != null && 
            !userDTO.getPhoneNumber().isEmpty() && 
            !userDTO.getPhoneNumber().equals(user.getPhoneNumber()) && 
            userRepository.existsByPhoneNumber(userDTO.getPhoneNumber())) {
            throw new BadRequestException("Phone number is already in use!");
        }
        
        // Update user details
        if (userDTO.getFirstName() != null) {
            user.setFirstName(userDTO.getFirstName());
        }
        
        if (userDTO.getLastName() != null) {
            user.setLastName(userDTO.getLastName());
        }
        
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        
        // Only update password if provided
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        
        if (userDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(userDTO.getPhoneNumber());
        }
        
        // Update roles if provided
        if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            userDTO.getRoles().forEach(role -> {
                Role userRole = roleRepository.findByName(role)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            });
            user.setRoles(roles);
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Change user password
     */
    @Transactional
    public User changePassword(Long id, String currentPassword, String newPassword) {
        User user = getUserById(id);
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        
        // Update with new password
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }
    
    /**
     * Enable or disable user
     */
    @Transactional
    public User changeUserStatus(Long id, boolean enabled) {
        User user = getUserById(id);
        user.setEnabled(enabled);
        return userRepository.save(user);
    }
    
    /**
     * Add role to user
     */
    @Transactional
    public User addRoleToUser(Long userId, ERole roleName) {
        User user = getUserById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        
        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            return userRepository.save(user);
        }
        
        return user; // Role already assigned
    }
    
    /**
     * Remove role from user
     */
    @Transactional
    public User removeRoleFromUser(Long userId, ERole roleName) {
        User user = getUserById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        
        if (user.getRoles().contains(role)) {
            user.getRoles().remove(role);
            
            // Ensure user has at least one role
            if (user.getRoles().isEmpty()) {
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role USER not found."));
                user.getRoles().add(userRole);
            }
            
            return userRepository.save(user);
        }
        
        return user; // Role not assigned
    }
    
    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}