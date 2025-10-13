package de.dicecup.classlink.features.security.passwordreset.repo;

import de.dicecup.classlink.features.security.passwordreset.domain.PasswordResetToken;
import de.dicecup.classlink.features.security.passwordreset.domain.PasswordResetTokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PasswordResetToken t where t.id = :id")
    Optional<PasswordResetToken> findForUpdate(@Param("id") UUID id);

    @Query("select t from PasswordResetToken t where t.userId = :userId and t.status = :status and t.expiresAt > :now")
    List<PasswordResetToken> findActiveByUserId(@Param("userId") UUID userId,
                                                @Param("status") PasswordResetTokenStatus status,
                                                @Param("now") Instant now);
}
