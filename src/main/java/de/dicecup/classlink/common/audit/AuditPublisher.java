package de.dicecup.classlink.common.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.UUID;

public interface AuditPublisher {
    void publish(UUID actorId, String action, String resource, Object details);

    default void publish(String action, String resource, Object details) {
        publish(null, action, resource, details);
    }

    record ContextAuditEvent(UUID actorId, String action, String resource, String detailsJson) {
    }

    @Component
    class Default implements AuditPublisher {
        private final ApplicationEventPublisher springEvents;
        private final AuditorAware<UUID> auditorAware;
        private final ObjectMapper objectMapper;

        public Default(ApplicationEventPublisher springEvents,
                       AuditorAware<UUID> auditorAware,
                       ObjectMapper objectMapper) {
            this.springEvents = springEvents;
            this.auditorAware = auditorAware;
            this.objectMapper = objectMapper;
        }

        @Override
        public void publish(UUID actorId, String action, String resource, Object details) {
            UUID effectiveActor = actorId != null ? actorId : auditorAware.getCurrentAuditor().orElse(null);
            String json = toJson(details);
            springEvents.publishEvent(new ContextAuditEvent(
                    effectiveActor,
                    action,
                    resource,
                    json
            ));
        }

        private String toJson(Object object) {
            try {
                return objectMapper.writeValueAsString(object);
            } catch (Exception e) {
                return "{\"_error\":\"json-serialize\"}";
            }
        }
    }
}
