package de.dicecup.classlink.features.registration.repo;

import de.dicecup.classlink.features.registration.domain.RegistrationInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RegistrationInviteRepository extends JpaRepository<RegistrationInvite, UUID> {
    Optional<RegistrationInvite> findByIdAndUsedAtIsNullAndExpiresAtAfter(UUID id, OffsetDateTime now);
    boolean existsByEmailAndUsedAtIsNullAndExpiresAtAfter(String email, OffsetDateTime now);
}
