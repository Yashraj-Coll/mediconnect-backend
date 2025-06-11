package com.mediconnect.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.security.UserDetailsImpl;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Get current user profile - FIXED to include role
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
        
        try {
            // Get user with roles
            User user = userRepository.findByIdWithRoles(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Get primary role
            String primaryRole = "ROLE_PATIENT"; // Default
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                primaryRole = user.getRoles().iterator().next().getName().name();
            }
            
            // Create response with role
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("phoneNumber", user.getPhoneNumber());
            response.put("enabled", user.isEnabled());
            response.put("role", primaryRole); // Include role
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching user profile: " + e.getMessage());
        }
    }

    /**
     * Get user's primary role
     */
    @GetMapping("/{userId}/role")
    public ResponseEntity<?> getUserRole(@PathVariable Long userId) {
        try {
            User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                Role primaryRole = user.getRoles().iterator().next();
                
                Map<String, Object> response = new HashMap<>();
                response.put("id", primaryRole.getId());
                response.put("name", primaryRole.getName().name());
                
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching user role: " + e.getMessage());
        }
    }

    /**
     * Get all user roles
     */
    @GetMapping("/{userId}/roles")
    public ResponseEntity<?> getUserRoles(@PathVariable Long userId) {
        try {
            User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<Map<String, Object>> roleList = new ArrayList<>();
            
            if (user.getRoles() != null) {
                for (Role role : user.getRoles()) {
                    Map<String, Object> roleData = new HashMap<>();
                    roleData.put("id", role.getId());
                    roleData.put("name", role.getName().name());
                    roleList.add(roleData);
                }
            }
            
            return ResponseEntity.ok(roleList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching user roles: " + e.getMessage());
        }
    }
    /**
     * Update user profile
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#id)")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updateData) {
        try {
            // Find the user
            User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Update basic fields
            if (updateData.containsKey("firstName")) {
                user.setFirstName((String) updateData.get("firstName"));
            }
            if (updateData.containsKey("lastName")) {
                user.setLastName((String) updateData.get("lastName"));
            }
            if (updateData.containsKey("email")) {
                user.setEmail((String) updateData.get("email"));
            }
            if (updateData.containsKey("phoneNumber")) {
                user.setPhoneNumber((String) updateData.get("phoneNumber"));
            }
            
            // Handle password update (if provided)
            if (updateData.containsKey("password") && updateData.get("password") != null) {
                String newPassword = (String) updateData.get("password");
                if (!newPassword.trim().isEmpty()) {
                    // You should encode the password here
                    // user.setPassword(passwordEncoder.encode(newPassword));
                    user.setPassword(newPassword); // For now, add encoding later
                }
            }
            
            // Handle notification preferences
            if (updateData.containsKey("emailNotifications")) {
                user.setEmailNotifications((Boolean) updateData.get("emailNotifications"));
            }
            if (updateData.containsKey("smsNotifications")) {
                user.setSmsNotifications((Boolean) updateData.get("smsNotifications"));
            }
            if (updateData.containsKey("pushNotifications")) {
                user.setPushNotifications((Boolean) updateData.get("pushNotifications"));
            }
            
            // Save the updated user
            User savedUser = userRepository.save(user);
            
            // Return response without password
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedUser.getId());
            response.put("firstName", savedUser.getFirstName());
            response.put("lastName", savedUser.getLastName());
            response.put("email", savedUser.getEmail());
            response.put("phoneNumber", savedUser.getPhoneNumber());
            response.put("emailNotifications", savedUser.getEmailNotifications());
            response.put("smsNotifications", savedUser.getSmsNotifications());
            response.put("pushNotifications", savedUser.getPushNotifications());
            response.put("message", "User updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            if (!userRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            userRepository.deleteById(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}