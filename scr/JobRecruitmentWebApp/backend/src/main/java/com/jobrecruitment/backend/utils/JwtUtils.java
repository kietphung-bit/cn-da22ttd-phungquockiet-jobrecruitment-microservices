package com.jobrecruitment.backend.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * JWT Utility Component - Modern JJWT 0.12.x+ Implementation
 * Handles JWT token generation and validation with latest security standards
 * 
 * Features:
 * - Uses modern JJWT API (0.12.5+)
 * - Proper SecretKey handling (no raw strings)
 * - Builder pattern for parsing and generation
 * - Thread-safe and secure
 * 
 * Required Dependencies:
 * - io.jsonwebtoken:jjwt-api:0.12.5
 * - io.jsonwebtoken:jjwt-impl:0.12.5
 * - io.jsonwebtoken:jjwt-jackson:0.12.5
 * 
 * Configuration:
 * Set in application.properties:
 * jwt.secret=<BASE64-encoded 256-bit key>
 * jwt.expiration=86400000
 */
@Component
public class JwtUtils {
    
    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;
    
    @Value("${jwt.expiration:86400000}") // Default: 24 hours in milliseconds
    private Long jwtExpiration;
    
    /**
     * Get signing key for JWT operations
     * Decodes BASE64 secret and creates HMAC-SHA256 key
     * 
     * @return SecretKey for signing/verification
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Extract username from JWT token
     * 
     * @param token JWT token
     * @return Username (subject)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extract expiration date from JWT token
     * 
     * @param token JWT token
     * @return Expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extract a specific claim from JWT token
     * 
     * @param token JWT token
     * @param claimsResolver Function to extract claim
     * @return Extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extract all claims from JWT token
     * Uses modern JJWT 0.12.x parser API
     * 
     * @param token JWT token
     * @return Claims payload
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Check if token is expired
     * 
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * Validate JWT token against UserDetails
     * Checks username match and expiration
     * 
     * @param token JWT token
     * @param userDetails User details from Spring Security
     * @return true if valid, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    /**
     * Generate JWT token for user
     * 
     * @param username Username to include in token
     * @return JWT token string
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }
    
    /**
     * Generate JWT token with custom claims
     * Use this to include role, userCode, etc.
     * 
     * @param extraClaims Additional claims (role, userCode, etc.)
     * @param username Username (subject)
     * @return JWT token string
     */
    public String generateToken(Map<String, Object> extraClaims, String username) {
        return createToken(extraClaims, username);
    }
    
    /**
     * Create JWT token with modern builder API
     * Uses Jwts.SIG.HS256 for strongly-typed signature algorithm
     * 
     * @param claims Token claims
     * @param username Username (subject)
     * @return JWT token string
     */
    private String createToken(Map<String, Object> claims, String username) {
        return Jwts
                .builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }
    
    /**
     * Extract role from token claims
     * Useful for authorization checks
     * 
     * @param token JWT token
     * @return Role code (ADM, DN, UV) or null if not present
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    
    /**
     * Extract user code from token claims
     * Used for profile linking (Section 4.5.C)
     * 
     * @param token JWT token
     * @return User code or null if not present
     */
    public String extractUserCode(String token) {
        return extractClaim(token, claims -> claims.get("userCode", String.class));
    }
    
    /**
     * Extract user ID from token claims
     * 
     * @param token JWT token
     * @return User ID or null if not present
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }
}
