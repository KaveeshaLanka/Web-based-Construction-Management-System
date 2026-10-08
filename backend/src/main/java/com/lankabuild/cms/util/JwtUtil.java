package com.lankabuild.cms.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Handles creating and validating JWT tokens for login sessions.
 * IMPORTANT: In production, load SECRET from an environment variable, not hardcoded.
 */
public class JwtUtil {

    // 256-bit secret key. Change this to your own long random string for real deployments.
    private static final String SECRET = "lankabuild-construction-mgmt-super-secret-key-change-me-123456";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    private static final long EXPIRATION_MS = 1000L * 60 * 60 * 8; // 8 hours

    public static String generateToken(int userId, String email, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims validateAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
