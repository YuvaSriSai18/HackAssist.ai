package com.hackassist.ai.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret.key}")
    private String jwtSecret;

    @Value("${jwt.expiration.time}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            log.error("ERROR: JWT_SECRET_KEY is invalid! Present: {}, Length: {}", 
                    jwtSecret != null, jwtSecret != null ? jwtSecret.length() : 0);
            throw new IllegalArgumentException("JWT_SECRET_KEY must be at least 32 characters");
        }
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String uid, String email) {
        log.info("============ JwtTokenProvider.generateToken() ============");
        log.info("Generating JWT token for uid: {}, email: {}", uid, email);
        
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);
        
        log.info("Token expires in {} ms", jwtExpirationMs);

        String token = Jwts.builder()
                .setSubject(uid)
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
        
        log.info("✓ JWT token generated successfully. Token preview: {}...", token.substring(0, Math.min(20, token.length())));
        return token;
    }

    public String getUidFromToken(String token) {
        String uid = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        log.debug("Extracted uid from token: {}", uid);
        return uid;
    }

    public String getEmailFromToken(String token) {
        Object email = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("email");
        String result = email == null ? null : email.toString();
        log.debug("Extracted email from token: {}", result);
        return result;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            log.debug("✓ JWT token validated successfully");
            return true;
        } catch (Exception ex) {
            log.warn("✗ JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }
}
