package com.mediconnect.config;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Filter to enable CORS (Cross-Origin Resource Sharing) for our API endpoints
 */
//@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomCorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        
        // Allow all origins for development - in production, restrict this to specific domains
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        // Allow common HTTP methods
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        
        // Max age for preflight requests
        response.setHeader("Access-Control-Max-Age", "3600");
        
        // Allow necessary headers
        response.setHeader("Access-Control-Allow-Headers", 
                "x-requested-with, authorization, content-type, x-auth-token, origin, accept");
        
        // Expose these headers to the frontend application
        response.setHeader("Access-Control-Expose-Headers", "x-auth-token");
        
        // Handle preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, res);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // Initialization code if needed
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }
}