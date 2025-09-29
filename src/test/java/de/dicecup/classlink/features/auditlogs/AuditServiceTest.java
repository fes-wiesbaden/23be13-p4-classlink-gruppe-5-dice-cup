package de.dicecup.classlink.features.auditlogs;

import de.dicecup.classlink.common.audit.AuditPublisher;
import de.dicecup.classlink.features.auditlogs.app.AuditLogRepository;
import de.dicecup.classlink.features.auditlogs.domain.AuditLog;
import de.dicecup.classlink.features.auditlogs.domain.Audited;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {
    @Mock
    AuditLogRepository logRepository;
    @Mock
    AuditPublisher auditPublisher;
    @InjectMocks
    AuditAspect aspect;
    @Captor
    ArgumentCaptor<AuditLog> logCaptor;

    static class Dummy {
        @Audited(action = "USER_CREATED", resource = "/users", detail = "created", actorIdArgIndex = 0)
        public String create(UUID actorId, String payload) {
            return "ok";
        }

        @Audited(action = "CRON_CLEANUP", resource = "cron://cleanup")
        public void cronTask() {
        }
    }

    @Test
    void saves_log_with_actor_from_arg() throws Throwable {
        // arrange
        var dummy = new Dummy();
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
}
