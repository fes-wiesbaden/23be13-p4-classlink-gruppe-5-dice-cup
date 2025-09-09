package de.dicecup.classlink.features.registration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter @Setter
@Entity
@Table(name = "registration_invites")
public class RegistrationInvite {
    @Id private UUID id;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String role;
    @Column(nullable = false, name = "token_hash")
    private String tokenHash;
    @Column(nullable = false, name = "expires_at")
    private OffsetDateTime expiresAt;
    @Column(nullable = false, name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(name = "created_by")
    private UUID createdBy;
    @Column(name = "used_at")
    private OffsetDateTime usedAt;

}
