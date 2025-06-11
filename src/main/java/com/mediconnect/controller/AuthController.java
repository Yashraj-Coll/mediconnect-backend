package com.mediconnect.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.mediconnect.dto.LoginRequest;
import com.mediconnect.dto.SignupRequest;
import com.mediconnect.dto.UserDTO;
import com.mediconnect.model.Session;
import com.mediconnect.model.User;
import com.mediconnect.repository.SessionRepository;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.security.UserDetailsImpl;
import com.mediconnect.service.EmailService;
import com.mediconnect.service.UserService;
import com.mediconnect.util.JwtUtil;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SessionRepository sessionRepo;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    /**
     * User login endpoint - UPDATED TO INCLUDE ROLE
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Authenticate credentials
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getIdentifier(),
                loginRequest.getPassword()
            )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Retrieve user details
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Prepare any additional claims (empty for now)
        Map<String, Object> extraClaims = new HashMap<>();

        // Generate JWT (using overload matching UserDetails + claims)
        String jwt = jwtUtil.generateToken(userDetails, extraClaims);

        // Create a new session entry
        String sessionToken = UUID.randomUUID().toString();
        Session session = new Session();
        session.setUser(userDetails.getUser());
        session.setJwtToken(jwt);
        session.setSessionToken(sessionToken);
        session.setRememberMe(loginRequest.isRememberMe());
        session = sessionRepo.save(session);

        // FIXED: Get user role from user_roles table
        User user = userDetails.getUser();
        String primaryRole = "ROLE_PATIENT"; // Default
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            primaryRole = user.getRoles().iterator().next().getName().name();
        }

        // FIXED: Create response with user data including role
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("type", "Bearer");
        response.put("sessionToken", sessionToken);
        
        // User object with role
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("email", user.getEmail());
        userResponse.put("firstName", user.getFirstName());
        userResponse.put("lastName", user.getLastName());
        userResponse.put("phoneNumber", user.getPhoneNumber());
        userResponse.put("role", primaryRole); // This is the key fix!
        
        response.put("user", userResponse);
        response.put("rememberMe", loginRequest.isRememberMe());

        return ResponseEntity.ok(response);
    }

    /**
     * User registration endpoint
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        // Convert request to DTO
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName(signupRequest.getFirstName());
        userDTO.setLastName(signupRequest.getLastName());
        userDTO.setEmail(signupRequest.getEmail());
        userDTO.setPassword(signupRequest.getPassword());
        userDTO.setPhoneNumber(signupRequest.getPhoneNumber());
        userDTO.setRoles(signupRequest.getRoles());

        // Create user via service
        User user = userService.createUser(userDTO);

        // Respond with confirmation
        return ResponseEntity.ok(
            new MessageResponse("User registered successfully with ID: " + user.getId())
        );
    }

    /**
     * FIXED: GET endpoint to return current authenticated user's info with role
     */
    @GetMapping("/current-user")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(new MessageResponse("Not logged in"));
        }
        
        // Get user with roles
        User user = userDetails.getUser();
        
        // Get primary role from user_roles table
        String primaryRole = "ROLE_PATIENT"; // Default
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            primaryRole = user.getRoles().iterator().next().getName().name();
        }
        
        // Create structured response
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("phoneNumber", user.getPhoneNumber());
        response.put("enabled", user.isEnabled());
        response.put("role", primaryRole); // This is what the frontend expects
        
        return ResponseEntity.ok(response);
    }

    /** Simple wrapper for messages */
    static class MessageResponse {
        private String message;
        public MessageResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    

    /**
     * Change password for authenticated user (add this method to AuthController)
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("Not authenticated"));
        }
        
        try {
            // Validate current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), userDetails.getPassword())) {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse("Current password is incorrect"));
            }
            
            // Validate new password
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse("New passwords do not match"));
            }
            
            if (request.getNewPassword().length() < 8) {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse("New password must be at least 8 characters long"));
            }
            
            // Update password
            User user = userDetails.getUser();
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user = userRepository.save(user);
            
            // Send confirmation email
            try {
                emailService.sendPasswordChangeConfirmation(user.getEmail(), user.getFirstName());
            } catch (Exception e) {
                logger.warn("Failed to send password change confirmation email: {}", e.getMessage());
                // Don't fail the password change if email fails
            }
            
            logger.info("Password changed successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
            
        } catch (Exception e) {
            logger.error("Error changing password for user {}: {}", userDetails.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("An error occurred while changing password"));
        }
    }
    
    /**
     * Check if user exists by email or phone (for better UX in login)
     */
    @GetMapping("/check-user")
    public ResponseEntity<?> checkUserExists(@RequestParam String identifier) {
        try {
            logger.info("Checking user existence for identifier: {}", identifier);
            
            // Check if user exists by email or phone
            boolean userExists = userRepository.existsByEmailOrPhoneNumber(identifier, identifier);
            
            Map<String, Object> response = new HashMap<>();
            response.put("exists", userExists);
            
            logger.info("User exists check result for {}: {}", identifier, userExists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking user existence for {}: {}", identifier, e.getMessage());
            
            // On error, return true to be safe (avoid revealing system issues)
            Map<String, Object> response = new HashMap<>();
            response.put("exists", true);
            response.put("error", "Unable to verify user existence");
            
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Logout endpoint - UPDATED to track logout time (using existing fields)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestParam(required = false) String sessionId) {
        try {
            if (sessionId != null && !sessionId.trim().isEmpty()) {
                // Find the session and update with logout time instead of deleting
                Optional<Session> sessionOpt = sessionRepo.findBySessionToken(sessionId);
                if (sessionOpt.isPresent()) {
                    Session session = sessionOpt.get();
                    
                    // 🆕 UPDATE: Set logout time instead of deleting the session
                    session.setLogoutTime(LocalDateTime.now());
                    sessionRepo.save(session);
                    
                    // Calculate session duration for logging
                    if (session.getLoginTime() != null) {
                        long durationMinutes = java.time.Duration.between(
                            session.getLoginTime(), 
                            session.getLogoutTime()
                        ).toMinutes();
                        
                        logger.info("Session {} logged out at {} (Duration: {} minutes)", 
                            sessionId, session.getLogoutTime(), durationMinutes);
                    } else {
                        logger.info("Session {} logged out at {}", sessionId, session.getLogoutTime());
                    }
                } else {
                    logger.warn("Session {} not found for logout", sessionId);
                }
            }
            
            // Clear security context
            SecurityContextHolder.clearContext();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Logout successful");
            response.put("logoutTime", LocalDateTime.now().toString());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Logout error for session {}: {}", sessionId, e.getMessage());
            
            // Still return success - logout should always work on frontend
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Logout completed");
            response.put("logoutTime", LocalDateTime.now().toString());
            return ResponseEntity.ok(response);
        }
    }
    
    // ADD THIS DTO CLASS AS WELL

    /**
     * Request DTO for password change
     */
    public static class ChangePasswordRequest {
        @NotBlank(message = "Current password is required")
        private String currentPassword;
        
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        private String newPassword;
        
        @NotBlank(message = "Confirm password is required")
        private String confirmPassword;
        
        // Getters and setters
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
        
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }
}