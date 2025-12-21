package com.trackIt.api_gateway.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;
    private Key signingKey;

    @PostConstruct
    public void init(){
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalStateException("JWT secret key is missing or too short");
        }
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public void validateToken(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);

            log.debug("Token validate successfully");
        } catch (SecurityException | SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT signature");
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token");
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            throw new RuntimeException("JWT token is expired");
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw new RuntimeException("JWT token is unsupported");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty");
            throw new RuntimeException("JWT claims string is empty");
        }
    }

    /**
     * Extract username
     */
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extract role
     */
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * Extract user ID
     */
    public Long extractUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    /**
     * Centralized claims extraction
     */
    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.error("Invalid JWT: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token");
        }
    }

}
