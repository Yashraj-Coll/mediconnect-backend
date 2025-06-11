package com.mediconnect.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "jwt_token", nullable = false, length = 500)
    private String jwtToken;

    @Column(name = "session_token", nullable = false, length = 500)
    private String sessionToken;

    @Column(name = "remember_me", nullable = false)
    private boolean rememberMe;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime = LocalDateTime.now();

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    public Session() { }

    public Session(User user, String jwtToken, String sessionToken, boolean rememberMe) {
        this.user = user;
        this.jwtToken = jwtToken;
        this.sessionToken = sessionToken;
        this.rememberMe = rememberMe;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public String getJwtToken() {
        return jwtToken;
    }
    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getSessionToken() {
        return sessionToken;
    }
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }
    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }
    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public LocalDateTime getLogoutTime() {
        return logoutTime;
    }
    public void setLogoutTime(LocalDateTime logoutTime) {
        this.logoutTime = logoutTime;
    }
}
