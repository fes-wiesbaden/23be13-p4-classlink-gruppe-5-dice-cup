package de.dicecup.classlink.features.auditlogs;

import de.dicecup.classlink.common.audit.AuditPublisher;
import de.dicecup.classlink.features.users.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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

    @Around(value = "auditedMethods(audited)", argNames = "pjp,audited")
    public Object logAudited(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        UUID actorId = resolveActorId(pjp, audited.actorIdArgIndex());
        String details = renderDetails(audited.detail(), pjp.getArgs());

        Object result = pjp.proceed();

        try {
            auditPublisher.publish(actorId, audited.action(), audited.resource(), details);
        } catch (Exception e) {
            log.debug("Audit skipped: {}", e.toString());
        }

        return result;
    }

    private UUID resolveActorId(ProceedingJoinPoint jp, int idx) {
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
