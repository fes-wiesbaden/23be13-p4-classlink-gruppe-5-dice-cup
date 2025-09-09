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

    public void publish(String action, Object details) {
        String json = toJson(details);
        springEvents.publishEvent(new ContextAuditEvent(
                auditorAware.getCurrentAuditor().orElse(null),
                action,
                json
        ));
    }

    private String toJson(Object object) {
        try {return new ObjectMapper().writeValueAsString(object); }
        catch (Exception e) { return "{\"_error\":\"json-serialize\"}"; }
    }

    public record ContextAuditEvent(UUID actorId, String action, String detailsJson) {}
}
