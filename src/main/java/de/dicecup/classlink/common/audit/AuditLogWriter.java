package de.dicecup.classlink.common.audit;

import de.dicecup.classlink.features.auditlogs.app.AuditLogRepository;
import de.dicecup.classlink.features.auditlogs.domain.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class AuditLogWriter {
    private final AuditLogRepository logRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAudit(AuditPublisher.ContextAuditEvent event) {
        AuditLog log = new AuditLog();
        log.setActorId(event.actorId());
        log.setAction(event.action());
        log.setDetails(event.detailsJson());
        log.setTimestamp(Instant.now());
        log.setResource(event.resource());
        logRepository.save(log);
    }
}
