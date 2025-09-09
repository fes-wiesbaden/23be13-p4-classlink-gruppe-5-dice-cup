package de.dicecup.classlink.features.auditlogs.domain;

import de.dicecup.classlink.features.users.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
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

    @Column(nullable = false, name = "actor_id")
    private UUID actorId;

    //TODO: explain the read-only nav
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", insertable = false, updatable = false)
    private User actor;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private OffsetDateTime timestamp = OffsetDateTime.now();

    @Column(nullable = false)
    private String details;
}
