package de.dicecup.classlink.features.security.passwordreset;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "password_reset")
public class PasswordResetToken {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, columnDefinition = "bytea")
    private byte[] tokenHash;

    @Column(name = "token_salt", nullable = false, columnDefinition = "bytea")
    private byte[] tokenSalt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PasswordResetTokenStatus status = PasswordResetTokenStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "note")
    private String note;

    @Column(name = "public_token")
    private String publicToken;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }
}
