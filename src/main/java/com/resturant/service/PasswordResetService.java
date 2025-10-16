package com.resturant.service;


import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PasswordResetService {

    private final Map<String, PasswordResetToken> resetTokens = new HashMap<>();
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    private static class PasswordResetToken {
        String email;
        Date expiresAt;

        PasswordResetToken(String email, Date expiresAt) {
            this.email = email;
            this.expiresAt = expiresAt;
        }
    }

    public String generateResetToken(String email) {
        // Generate a secure random token
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String token = base64Encoder.encodeToString(randomBytes);

        // Set expiration (1 hour from now)
        Date expiresAt = new Date(System.currentTimeMillis() + 60 * 60 * 1000);

        // Store token
        resetTokens.put(token, new PasswordResetToken(email, expiresAt));

        // Clean up expired tokens
        cleanUpExpiredTokens();

        return token;
    }

    public boolean validateResetToken(String token) {
        cleanUpExpiredTokens();
        PasswordResetToken resetToken = resetTokens.get(token);
        return resetToken != null && resetToken.expiresAt.after(new Date());
    }

    public String getEmailFromToken(String token) {
        PasswordResetToken resetToken = resetTokens.get(token);
        if (resetToken != null && resetToken.expiresAt.after(new Date())) {
            return resetToken.email;
        }
        return null;
    }

    public void invalidateToken(String token) {
        resetTokens.remove(token);
    }

    private void cleanUpExpiredTokens() {
        Date now = new Date();
        resetTokens.entrySet().removeIf(entry -> entry.getValue().expiresAt.before(now));
    }
}

