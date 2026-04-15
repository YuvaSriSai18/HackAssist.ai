package com.hackassist.ai.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecretHashingUtil {

    private static final String ALGORITHM = "SHA-256";
    private static final int SECRET_BYTES = 32;

    /**
     * Generate a random 32-byte secret encoded in Base64
     */
    public static String generateRandomSecret() {
        byte[] randomBytes = new byte[SECRET_BYTES];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }

    /**
     * Hash a secret string using SHA-256
     */
    public static String hashSecret(String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception ex) {
            log.error("Failed to hash secret: {}", ex.getMessage());
            throw new RuntimeException("Failed to hash secret", ex);
        }
    }

    /**
     * Verify a secret against its hash using constant-time comparison
     */
    public static boolean verifySecret(String plainSecret, String storedHash) {
        try {
            String computedHash = hashSecret(plainSecret);
            return constantTimeEquals(computedHash, storedHash);
        } catch (Exception ex) {
            log.error("Failed to verify secret: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Constant-time string comparison to prevent timing attacks
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
