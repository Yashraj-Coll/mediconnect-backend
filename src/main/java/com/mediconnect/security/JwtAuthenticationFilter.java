package com.mediconnect.security;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mediconnect.util.JwtUtil;
import com.mediconnect.repository.SessionRepository;
import com.mediconnect.model.Session;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private SessionRepository sessionRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        
        // ✅ CRITICAL FIX: Skip JWT processing for CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            logger.debug("Skipping JWT authentication for OPTIONS request to: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ CRITICAL FIX: Skip JWT for public endpoints to avoid authentication errors
        String requestURI = request.getRequestURI();
        if (isPublicEndpoint(requestURI)) {
            logger.debug("Skipping JWT authentication for public endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = parseJwt(request);
            String sessionToken = request.getHeader("X-Session-Id");

            if (jwt != null && jwtUtil.validateToken(jwt)) {
                // ✅ Make session validation optional for AI endpoints
                if (StringUtils.hasText(sessionToken)) {
                    // Full session validation for regular endpoints
                    Optional<Session> sessionOpt = sessionRepo.findByJwtToken(jwt)
                        .filter(s -> s.getSessionToken().equals(sessionToken))
                        .filter(s -> s.getLogoutTime() == null);

                    if (sessionOpt.isPresent()) {
                        setAuthentication(jwt, request);
                    } else {
                        logger.warn("Session validation failed for token: {}", sessionToken);
                    }
                } else {
                    // ✅ For AI endpoints, allow JWT without session validation
                    if (requestURI.startsWith("/api/ai/")) {
                        logger.debug("AI endpoint detected, using JWT-only authentication");
                        setAuthentication(jwt, request);
                    } else {
                        logger.warn("Missing session token for non-AI endpoint: {}", requestURI);
                    }
                }
            } else if (jwt != null) {
                logger.warn("Invalid JWT token provided");
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication for {}: {}", request.getRequestURI(), e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * ✅ Helper method to set authentication
     */
    private void setAuthentication(String jwt, HttpServletRequest request) {
        try {
            String username = jwtUtil.extractUsername(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
            authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            logger.debug("Successfully authenticated user: {} with authorities: {}", 
                        username, userDetails.getAuthorities());
        } catch (Exception e) {
            logger.error("Failed to set authentication: {}", e.getMessage());
        }
    }

    /**
     * ✅ Check if endpoint is public and should skip JWT processing
     */
    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.startsWith("/api/auth/") ||
               requestURI.startsWith("/api/public/") ||
               requestURI.startsWith("/api/doctors/public/") ||
               requestURI.matches("/api/doctors/.*/availability") ||
               requestURI.startsWith("/uploads/") ||
               requestURI.equals("/error") ||
               requestURI.startsWith("/actuator/") ||
               requestURI.startsWith("/v3/api-docs/") ||
               requestURI.startsWith("/swagger-ui/") ||
               requestURI.equals("/swagger-ui.html") ||
               requestURI.equals("/api/ai/debug-auth"); // Allow debug endpoint
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}