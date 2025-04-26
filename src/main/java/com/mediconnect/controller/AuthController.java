package com.mediconnect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mediconnect.dto.LoginRequest;
import com.mediconnect.dto.SignupRequest;
import com.mediconnect.dto.TokenResponse;
import com.mediconnect.dto.UserDTO;
import com.mediconnect.model.User;
import com.mediconnect.security.UserDetailsImpl;
import com.mediconnect.service.UserService;
import com.mediconnect.util.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * User login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        String jwt = jwtUtil.generateToken(userDetails);
        
        return ResponseEntity.ok(new TokenResponse(
                jwt,
                userDetails.getId(),
                userDetails.getEmail(),
                userDetails.getFirstName(),
                userDetails.getLastName(),
                userDetails.getAuthorities()));
    }

    /**
     * User registration endpoint
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        // Convert to UserDTO
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName(signupRequest.getFirstName());
        userDTO.setLastName(signupRequest.getLastName());
        userDTO.setEmail(signupRequest.getEmail());
        userDTO.setPassword(signupRequest.getPassword());
        userDTO.setPhoneNumber(signupRequest.getPhoneNumber());
        userDTO.setRoles(signupRequest.getRoles());
        
        // Create user
        User user = userService.createUser(userDTO);
        
        // Return success response with the created user ID
        return ResponseEntity.ok().body(new MessageResponse("User registered successfully with ID: " + user.getId()));
    }
    
    /**
     * Simple response for messages
     */
    static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}