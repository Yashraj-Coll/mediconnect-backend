package com.mediconnect.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.mediconnect.security.JwtAuthenticationEntryPoint;
import com.mediconnect.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ✅ CRITICAL FIX 1: Allow OPTIONS requests first (CORS preflight)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                
                // 🔌 WEBSOCKET ENDPOINTS - ADD THESE LINES:
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/ws/info").permitAll()
                .requestMatchers("/ws/info/**").permitAll()
                .requestMatchers("/topic/**").permitAll()
                .requestMatchers("/queue/**").permitAll()
                .requestMatchers("/user/**").permitAll()
                .requestMatchers("/app/**").permitAll()
                
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/doctors/public/**").permitAll()
                .requestMatchers("/api/doctors/*/availability").permitAll()
                .requestMatchers("/api/auth/password-reset/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                // ✅ Debug endpoint for testing
                .requestMatchers("/api/ai/debug-auth").permitAll()
                
                // ✅ CRITICAL FIX 2: Use hasAnyAuthority instead of hasAnyRole
                // AI Endpoints with correct authority names
                .requestMatchers("/api/ai/**").hasAnyAuthority("ROLE_PATIENT", "ROLE_DOCTOR")
                .requestMatchers(HttpMethod.POST, "/api/ai/chat").hasAnyAuthority("ROLE_PATIENT", "ROLE_DOCTOR")
                .requestMatchers(HttpMethod.POST, "/api/ai/analyze-image").hasAnyAuthority("ROLE_PATIENT", "ROLE_DOCTOR")
                .requestMatchers(HttpMethod.POST, "/api/ai/analyze-document").hasAnyAuthority("ROLE_PATIENT", "ROLE_DOCTOR")
                .requestMatchers(HttpMethod.POST, "/api/ai/speech-to-text").hasAnyAuthority("ROLE_PATIENT", "ROLE_DOCTOR")
                .requestMatchers(HttpMethod.POST, "/api/ai/translate").hasAnyAuthority("ROLE_PATIENT", "ROLE_DOCTOR")
                .requestMatchers(HttpMethod.GET, "/api/ai/profile").hasAnyAuthority("ROLE_PATIENT", "ROLE_DOCTOR")
                .requestMatchers(HttpMethod.GET, "/api/ai/medical-context").hasAnyAuthority("ROLE_PATIENT", "ROLE_DOCTOR")
                .requestMatchers(HttpMethod.GET, "/api/ai/supported-languages").hasAnyAuthority("ROLE_PATIENT", "ROLE_DOCTOR")
                .requestMatchers(HttpMethod.POST, "/api/ai/detect-language").hasAnyAuthority("ROLE_PATIENT", "ROLE_DOCTOR")
                .requestMatchers(HttpMethod.GET, "/api/ai/language-suggestions/**").hasAnyAuthority("ROLE_PATIENT", "ROLE_DOCTOR")
                .requestMatchers(HttpMethod.POST, "/api/ai/smart-translate").hasAnyAuthority("ROLE_PATIENT", "ROLE_DOCTOR")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // ✅ CRITICAL FIX 3: Enhanced CORS configuration
        // Allow your React app's origin
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://127.0.0.1:5173"
        ));
        
        // Allow the HTTP methods you need (including OPTIONS)
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));
        
        // Allow all headers to be flexible with client requests
        configuration.setAllowedHeaders(List.of("*"));
        
        // Expose headers that frontend can read
        configuration.setExposedHeaders(List.of(
            "Authorization", 
            "X-Auth-Token", 
            "X-Session-Id",
            "Content-Type"
        ));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}