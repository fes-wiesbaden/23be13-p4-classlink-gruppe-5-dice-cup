package de.dicecup.classlink.features.auditlogs.app;

import de.dicecup.classlink.features.auditlogs.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByUser(UUID actorId, Pageable pageable);
    Page<AuditLog> findByAction(String action, Pageable pageable);

}
