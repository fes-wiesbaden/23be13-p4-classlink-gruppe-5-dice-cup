package de.dicecup.classlink;

import de.dicecup.classlink.features.users.domain.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<UUID> auditorAware() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .map(this::resolvePrincipalId)
                .flatMap(optional -> optional);
    }

    private Optional<UUID> resolvePrincipalId(Object principal) {
        if (principal instanceof UUID uuid) {
            return Optional.of(uuid);
        }
        if (principal instanceof User user) {
            return Optional.ofNullable(user.getId());
        }
        if (principal instanceof UserDetails details) {
            return Optional.ofNullable(parseUuid(details.getUsername()));
        }
        if (principal instanceof String str) {
            return Optional.ofNullable(parseUuid(str));
        }
        return Optional.empty();
    }

    private UUID parseUuid(String value) {
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
