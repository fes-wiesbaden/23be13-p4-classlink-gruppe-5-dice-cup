package de.dicecup.classlink.features.auditlogs;

import de.dicecup.classlink.common.audit.AuditPublisher;
import de.dicecup.classlink.features.auditlogs.domain.AuditLog;
import de.dicecup.classlink.features.auditlogs.domain.Audited;
import de.dicecup.classlink.features.users.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.UUID;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Order(200)
public class AuditAspect {

    private final AuditPublisher auditPublisher;
    @Pointcut("@annotation(audited)")
    public void auditedMethods(Audited audited) {}
    @Before(value = "auditedMethods(audited)", argNames = "jp,audited")
    public void logAudited(JoinPoint jp, Audited audited) {
        UUID actorId = resolveActorId(jp, audited.actorIdArgIndex());
        String details = renderDetails(audited.detail(), jp.getArgs());

        try {
            auditPublisher.publish(
                    actorId,
                    audited.action(),
                    audited.resource(),
                    details
            );
        } catch (Exception e) {
            log.debug("Audit skipped: {}", e.toString());
        }
    }

    private UUID resolveActorId(JoinPoint jp, int idx) {
        if (idx >= 0) {
            Object[] args = jp.getArgs();
            if (idx < args.length && args[idx] instanceof UUID u) return u;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User u) return u.getId();
        return null;
    }

    private String renderDetails(String template, Object[] args) {
        if (template == null || template.isBlank()) return "";
        try {
            return MessageFormat.format(template, args);
        } catch (Exception e) {
            return template;
        }
    }
}
