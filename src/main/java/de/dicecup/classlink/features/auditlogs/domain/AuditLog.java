package de.dicecup.classlink.features.auditlogs.domain;

import de.dicecup.classlink.features.users.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table( name = "audit_logs",
        indexes = @Index(name = "ix_audit_logs_actor", columnList = "actor_id"))
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, name = "actor_id")
    private UUID actorId;

    //TODO: explain the read-only nav
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", insertable = false, updatable = false)
    private User user;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = true)
    private String details;

    @Column(nullable = false)
    private String resource;
}
