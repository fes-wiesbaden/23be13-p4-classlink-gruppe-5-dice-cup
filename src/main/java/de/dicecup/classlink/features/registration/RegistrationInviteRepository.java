package de.dicecup.classlink.features.registration;

import de.dicecup.classlink.features.registration.domain.RegistrationInvite;
import de.dicecup.classlink.features.registration.domain.RegistrationInviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistrationInviteRepository extends JpaRepository<RegistrationInvite, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from RegistrationInvite i where i.id = :id")
    Optional<RegistrationInvite> findForUpdate(@Param("id") UUID id);

    @Query("select i from RegistrationInvite i where i.email = :email and i.status = :status and i.expiresAt > :now")
    List<RegistrationInvite> findActiveByEmail(@Param("email") String email,
                                               @Param("status") RegistrationInviteStatus status,
                                               @Param("now") Instant now);
}
