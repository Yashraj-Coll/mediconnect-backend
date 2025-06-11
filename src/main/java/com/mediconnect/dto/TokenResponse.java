package com.mediconnect.dto;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

public class TokenResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Collection<? extends GrantedAuthority> roles;
    private String sessionToken;
    private Long sessionId;
    private boolean rememberMe;

    public TokenResponse(String token,
                         Long id,
                         String email,
                         String firstName,
                         String lastName,
                         Collection<? extends GrantedAuthority> roles,
                         String sessionToken,
                         Long sessionId,
                         boolean rememberMe) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
        this.sessionToken = sessionToken;
        this.sessionId = sessionId;
        this.rememberMe = rememberMe;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Collection<? extends GrantedAuthority> getRoles() { return roles; }
    public void setRoles(Collection<? extends GrantedAuthority> roles) { this.roles = roles; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public boolean isRememberMe() { return rememberMe; }
    public void setRememberMe(boolean rememberMe) { this.rememberMe = rememberMe; }
}