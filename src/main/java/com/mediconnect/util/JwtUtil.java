package com.mediconnect.util;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final Key signingKey;
    private final long expirationMs;

    public JwtUtil(
        @Value("${jwt.secret}") String base64Secret,
        @Value("${jwt.expiration}") long expirationMs
    ) {
        // Decode the Base64-encoded secret into a 512-bit+ key
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    /**
     * Generate a token with no additional claims
     */
    public String generateToken(org.springframework.security.core.userdetails.UserDetails userDetails) {
        return createToken(Map.of(), userDetails.getUsername());
    }

    /**
     * Generate a token with extra claims
     */
    public String generateToken(org.springframework.security.core.userdetails.UserDetails userDetails,
                                Map<String, Object> additionalClaims) {
        return createToken(additionalClaims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                   .setClaims(claims)
                   .setSubject(subject)
                   .setIssuedAt(now)
                   .setExpiration(expiry)
                   .signWith(signingKey, SignatureAlgorithm.HS512)
                   .compact();
    }

    public Claims extractAllClaims(String token) {
        Jws<Claims> parsed = Jwts.parserBuilder()
                                 .setSigningKey(signingKey)
                                 .build()
                                 .parseClaimsJws(token);
        return parsed.getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    public boolean validateToken(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
