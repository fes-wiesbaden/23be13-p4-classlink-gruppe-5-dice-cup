package de.dicecup.classlink.features.security.passwordreset.domain;

public enum PasswordResetTokenStatus {
    PENDING,
    REDEEMED,
    REVOKED,
    EXPIRED
}
