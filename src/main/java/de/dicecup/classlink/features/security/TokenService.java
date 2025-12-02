package de.dicecup.classlink.features.security;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class TokenService {
    private static final int RAW_TOKEN_LENGTH = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public TokenBundle generateToken() {
        byte[] token = new byte[RAW_TOKEN_LENGTH];
        SECURE_RANDOM.nextBytes(token);

        byte[] salt = new byte[randomSaltLength()];
        SECURE_RANDOM.nextBytes(salt);

        byte[] hash = hash(salt, token);
        return new TokenBundle(token, salt, hash);
    }

    public byte[] hash(byte[] salt, byte[] token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            return digest.digest(token);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(a, b);
    }

    public String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    public byte[] decode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    private int randomSaltLength() {
        return 16 + SECURE_RANDOM.nextInt(17);
    }

    public record TokenBundle(byte[] token, byte[] salt, byte[] hash) {
    }
}
