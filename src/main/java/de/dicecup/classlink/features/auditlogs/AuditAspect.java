package de.dicecup.classlink.features.auditlogs;

import de.dicecup.classlink.features.auditlogs.app.AuditLogRepository;
import de.dicecup.classlink.features.auditlogs.domain.AuditLog;
import de.dicecup.classlink.features.users.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    @Pointcut(
            "(execution(* de.dicecup.classlink.features..service..*(..)) || " +
                    "execution(* de.dicecup.classlink.features..services..*(..))) && " +
                    "!within(de.dicecup.classlink.features.auditlogs..*) && " +
                    "!within(org.springframework.data.repository..*)"
    )
    public void anyFeatureService() {}

    private void persistAudit(JoinPoint jp, UUID actorId, String details) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setActorId(actorId);
            auditLog.setAction(jp.getSignature().toShortString());
            auditLog.setTimestamp(OffsetDateTime.now());
            auditLog.setDetails(details != null ? details : "");
            auditLogRepository.save(auditLog);
        } catch (Exception exception) {
            log.debug("Audit skipped: {}", exception.toString());
        }
    }

    private UUID resolveActorIdFromSecurity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof User u) {
            return u.getId();
        }
        return null;
    }

    @Before("anyFeatureService()")
    public void logWithSecurityContext(JoinPoint jp) {
        UUID actorId = resolveActorIdFromSecurity();
        persistAudit(jp, actorId, null);
    }
}
