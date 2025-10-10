package de.dicecup.classlink.features.auditlogs;

import de.dicecup.classlink.common.audit.AuditPublisher;
import de.dicecup.classlink.features.auditlogs.app.AuditLogRepository;
import de.dicecup.classlink.features.auditlogs.domain.AuditLog;
import de.dicecup.classlink.features.auditlogs.domain.Audited;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {
    @Mock
    AuditPublisher auditPublisher;
    @InjectMocks
    AuditAspect aspect;

    static class Dummy {
        @Audited(action = "USER_CREATED", resource = "/users", detail = "created", actorIdArgIndex = 0)
        public String create(UUID actorId, String payload) {
            return "ok";
        }

        @Audited(action = "CRON_CLEANUP", resource = "cron://cleanup")
        public void cronTask() {
        }

        @Audited(action = "BROKEN", resource = "broken://resource", detail = "{0", actorIdArgIndex = 0)
        public void broken(UUID actorId) {
        }
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void saves_log_with_actor_from_arg() throws Throwable {
        // arrange
        Method m = Dummy.class.getMethod("create", UUID.class, String.class);
        Audited ann = m.getAnnotation(Audited.class);

        var pjp = mock(ProceedingJoinPoint.class);
        UUID userId = UUID.randomUUID();
        when(pjp.getArgs()).thenReturn(new Object[]{userId, "{\"x\":1}"});

        // act
        aspect.logAudited(pjp, ann);
        // assert
        verify(auditPublisher).publish(
                eq(userId),
                eq("USER_CREATED"),
                eq("/users"),
                eq("created")
        );
        verifyNoMoreInteractions(auditPublisher);
        // Persistence is handled by the event listener; verify it in a dedicated listener/repository test, not here.
    }

    @Test
    void resolves_actor_from_security_context_when_index_missing() throws Throwable {
        // arrange
        Method m = Dummy.class.getMethod("cronTask");
        Audited ann = m.getAnnotation(Audited.class);

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.getArgs()).thenReturn(new Object[0]);

        UUID actorId = UUID.randomUUID();
        var user = new de.dicecup.classlink.features.users.domain.User();
        user.setId(actorId);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        // act
        aspect.logAudited(pjp, ann);

        // assert
        verify(auditPublisher).publish(
                eq(actorId),
                eq("CRON_CLEANUP"),
                eq("cron://cleanup"),
                eq("")
        );
        verifyNoMoreInteractions(auditPublisher);
    }

    @Test
    void keeps_template_when_detail_formatting_fails() throws Throwable {
        // arrange
        Method m = Dummy.class.getMethod("broken", UUID.class);
        Audited ann = m.getAnnotation(Audited.class);
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        UUID actorId = UUID.randomUUID();
        when(pjp.getArgs()).thenReturn(new Object[]{actorId});

        // act
        aspect.logAudited(pjp, ann);

        // assert
        verify(auditPublisher).publish(
                eq(actorId),
                eq("BROKEN"),
                eq("broken://resource"),
                eq("{0")
        );
        verifyNoMoreInteractions(auditPublisher);
    }

    @Test
    void swallows_publish_exceptions() throws Throwable {
        // arrange
        var dummy = new Dummy();
        Method m = Dummy.class.getMethod("create", UUID.class, String.class);
        Audited ann = m.getAnnotation(Audited.class);
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        UUID actorId = UUID.randomUUID();
        when(pjp.getArgs()).thenReturn(new Object[]{actorId, "{}"});

        doThrow(new RuntimeException("boom"))
                .when(auditPublisher)
                .publish(eq(actorId), eq("USER_CREATED"), eq("/users"), eq("created"));

        // act + assert
        assertThatCode(() -> aspect.logAudited(pjp, ann)).doesNotThrowAnyException();

        verify(auditPublisher).publish(
                eq(actorId),
                eq("USER_CREATED"),
                eq("/users"),
                eq("created")
        );
        verifyNoMoreInteractions(auditPublisher);
    }
}
