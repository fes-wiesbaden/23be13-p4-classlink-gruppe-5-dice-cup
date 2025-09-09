package de.dicecup.classlink.features.registration.app;

import de.dicecup.classlink.common.audit.AuditPublisher;
import de.dicecup.classlink.features.registration.domain.InviteRequestDto;
import de.dicecup.classlink.features.registration.domain.InviteResponseDto;
import de.dicecup.classlink.features.registration.domain.RegistrationInvite;
import de.dicecup.classlink.features.registration.domain.RegristrationRequesDto;
import de.dicecup.classlink.features.registration.repo.RegistrationInviteRepository;
import de.dicecup.classlink.features.security.PasswordService;
import de.dicecup.classlink.features.users.app.UserInfoRepository;
import de.dicecup.classlink.features.users.app.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.features.users.dto.CreateUserInfoDto;
import de.dicecup.classlink.features.users.dto.UserDto;
import de.dicecup.classlink.features.users.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RegistrationService {
    private final RegistrationInviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final AuditPublisher auditPublisher;
    private final AuditorAware<UUID> auditor;
    private final PasswordService passwordService;

    @Transactional
    public UserDto create(RegristrationRequesDto request, UserMapper userMapper) {
        RegistrationInvite invite = inviteRepository
                .findByIdAndUsedAtIsNullAndExpiresAtAfter(request.inviteId(), OffsetDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Invite invalid or expired"));
        if (!generateSHA256Hash(request.token()).equals(invite.getTokenHash())) {
            throw new IllegalArgumentException("Invite token mismatch");
        }
        if (userInfoRepository.existsByEmailIgnoreCase(invite.getEmail())) {
            throw new IllegalStateException("User already exits for email");
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(request.username());
        user.setEnabled(true);
        user.setPasswordHash(passwordService.hashPassword(request.password()));

        CreateUserInfoDto requestInfo = request.userInfo();
        UserInfo info = new UserInfo(
                user.getId(),
                user,
                requestInfo.firstName(),
                requestInfo.lastName(),
                requestInfo.dateOfBirth(),
                invite.getEmail()
        );
        user.setUserInfo(info);

        User saved = userRepository.save(user);
        invite.setUsedAt(OffsetDateTime.now());
        inviteRepository.save(invite);

        auditPublisher.publish("USER_REGISTERED", Map.of(
                "userId", saved.getId(), "email", invite.getEmail(),
                "role", invite.getRole(), "inviteId", invite.getId()
        ));
        return userMapper.toDto(saved);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public InviteResponseDto invite(InviteRequestDto request) {
        if ("ADMIN".equalsIgnoreCase(request.role())
            && SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Only ADMIN can invite ADMIN");
        }
        if (inviteRepository.existsByEmailAndUsedAtIsNullAndExpiresAtAfter(request.email(), OffsetDateTime.now())) {
            throw new IllegalStateException("Invite already active for this mail");
        }
        UUID id = UUID.randomUUID();
        String plaintext = UUID.randomUUID().toString().replace("-", "");
        String hash = generateSHA256Hash(plaintext);

        RegistrationInvite invite = new RegistrationInvite();
        invite.setId(id);
        invite.setEmail(request.email());
        invite.setRole(request.role());
        invite.setTokenHash(hash);
        invite.setExpiresAt(OffsetDateTime.from(Instant.now().plus(Duration.ofMinutes(60))));
        invite.setCreatedBy(auditor.getCurrentAuditor().orElse(null));
        inviteRepository.save(invite);

        String link = "https://classlink.schule.lan/register?id="+id+"&t="+plaintext;

        return new InviteResponseDto(id, invite.getExpiresAt(), link);
    }
    public static String generateSHA256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] encodedhash = digest.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
