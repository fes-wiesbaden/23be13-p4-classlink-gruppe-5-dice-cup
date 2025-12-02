package de.dicecup.classlink.features.registration.domain;

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
@Table(name = "registration_invites")
public class RegistrationInvite {
    @Id
    private UUID id;

    @Column(nullable = false, columnDefinition = "citext")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationInviteRole role;

    @Column(name = "class_id")
    private UUID classId;

    @Column(name = "public_token")
    private String publicToken;

    @Column(name = "token_hash", nullable = false, columnDefinition = "bytea")
    private byte[] tokenHash;

    @Column(name = "token_salt", nullable = false, columnDefinition = "bytea")
    private byte[] tokenSalt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationInviteStatus status = RegistrationInviteStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "uses_count", nullable = false)
    private int usesCount = 0;

    @Column(name = "max_uses", nullable = false)
    private int maxUses = 1;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "note")
    private String note;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (expiresAt == null) {
            throw new IllegalStateException("expiresAt must be set before persisting");
        }
        if (tokenHash == null || tokenHash.length == 0) {
            throw new IllegalStateException("tokenHash must be set before persisting");
        }
        if (tokenSalt == null || tokenSalt.length == 0) {
            throw new IllegalStateException("tokenSalt must be set before persisting");
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public boolean canBeUsed() {
        return status == RegistrationInviteStatus.PENDING && !isExpired() && usesCount < maxUses;
    }
}
