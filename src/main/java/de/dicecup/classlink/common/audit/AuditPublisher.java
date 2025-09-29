package de.dicecup.classlink.common.audit;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuditPublisher {
    private final ApplicationEventPublisher springEvents;
    private final AuditorAware<UUID> auditorAware;

    public void publish(UUID actorId, String action, Object details, String resource) {
        String json = toJson(details);
        springEvents.publishEvent(new ContextAuditEvent(
                actorId,
                action,
                resource,
                json
        ));
    }
    //TODO: explain this
    public void publish(String action, Object details, String resource) {
        publish(auditorAware.getCurrentAuditor().orElse(null), action, details, resource);
    }

    private String toJson(Object object) {
        try {return new ObjectMapper().writeValueAsString(object); }
        catch (Exception e) { return "{\"_error\":\"json-serialize\"}"; }
    }

    public record ContextAuditEvent(UUID actorId, String action, String resource, String detailsJson) {}
}
