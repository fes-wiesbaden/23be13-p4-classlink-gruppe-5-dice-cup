package de.dicecup.classlink.features.registration.app;

import de.dicecup.classlink.features.classes.Class;
import de.dicecup.classlink.features.classes.ClassRepository;
import de.dicecup.classlink.features.registration.domain.*;
import de.dicecup.classlink.features.registration.repo.RegistrationInviteRepository;
import de.dicecup.classlink.features.security.JwtService;
import de.dicecup.classlink.features.security.PasswordService;
import de.dicecup.classlink.features.security.TokenService;
import de.dicecup.classlink.features.security.TokenService.TokenBundle;
import de.dicecup.classlink.features.users.app.UserInfoRepository;
import de.dicecup.classlink.features.users.app.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.features.users.domain.roles.Admin;
import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.features.users.domain.roles.Teacher;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InvitationService {

    private final RegistrationInviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final ClassRepository classRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final TokenService tokenService;

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public InviteCreatedResponseDto createInvite(CreateInviteRequestDto request) {
        RegistrationInviteRole role = Objects.requireNonNull(request.role(), "role");
        ensureInvitePolicy(role);

        if (!inviteRepository.findActiveByEmail(request.email(), RegistrationInviteStatus.PENDING, Instant.now()).isEmpty()) {
            throw new IllegalStateException("Active invite already exists for email");
        }

        UUID inviteId = UUID.randomUUID();
        TokenBundle bundle = tokenService.generateToken();
        String publicToken = publicToken(inviteId, bundle.token());

        RegistrationInvite invite = new RegistrationInvite();
        invite.setId(inviteId);
        invite.setEmail(request.email());
        invite.setRole(role);
        invite.setTokenSalt(bundle.salt());
        invite.setTokenHash(bundle.hash());
        invite.setPublicToken(publicToken);
        invite.setStatus(RegistrationInviteStatus.PENDING);
        invite.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        invite.setCreatedBy(currentUserId());
        invite.setUsesCount(0);
        invite.setMaxUses(request.maxUses() != null && request.maxUses() > 0 ? request.maxUses() : 1);
        invite.setClassId(request.classId());
        invite.setNote(request.note());

        if (invite.getClassId() != null) {
            classRepository.findById(invite.getClassId())
                    .orElseThrow(() -> new EntityNotFoundException("Class %s not found".formatted(invite.getClassId())));
        }

        inviteRepository.save(invite);
        String qrPath = "/admin/invites/" + inviteId + "/qrcode?format=png";

        return new InviteCreatedResponseDto(inviteId, publicToken, qrPath, invite.getExpiresAt());
    }

    public InviteValidationResponseDto validate(InviteValidationRequestDto request) {
        ParsedToken parsed = parseToken(request.token());
        RegistrationInvite invite = inviteRepository.findById(parsed.inviteId())
                .orElseThrow(() -> new EntityNotFoundException("Invite not found"));

        ensureInviteUsable(invite, parsed);

        int remainingUses = Math.max(invite.getMaxUses() - invite.getUsesCount(), 0);
        return new InviteValidationResponseDto(invite.getId(), invite.getEmail(), invite.getRole(), invite.getExpiresAt(), remainingUses);
    }

    public InviteRedeemResponseDto redeem(InviteRedeemRequestDto request) {
        ParsedToken parsed = parseToken(request.token());
        RegistrationInvite invite = inviteRepository.findForUpdate(parsed.inviteId())
                .orElseThrow(() -> new EntityNotFoundException("Invite not found"));

        ensureInviteUsable(invite, parsed);

        if (userInfoRepository.existsByEmailIgnoreCase(invite.getEmail())) {
            throw new IllegalStateException("User already exists for email");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEnabled(true);
        user.setPasswordHash(passwordService.hashPassword(request.password()));

        UserInfo info = new UserInfo();
        info.setUser(user);
        info.setEmail(invite.getEmail());
        user.setUserInfo(info);

        switch (invite.getRole()) {
            case ADMIN -> assignAdminRole(user);
            case LEHRER -> assignTeacherRole(user);
            case SCHUELER -> assignStudentRole(user, invite.getClassId());
            default -> throw new IllegalStateException("Unsupported role " + invite.getRole());
        }

        User saved = userRepository.save(user);

        invite.setUsesCount(invite.getUsesCount() + 1);
        invite.setUsedAt(Instant.now());
        invite.setStatus(invite.getUsesCount() >= invite.getMaxUses() ? RegistrationInviteStatus.REDEEMED : RegistrationInviteStatus.PENDING);
        inviteRepository.save(invite);

        String jwt = jwtService.generateToken(saved);
        return new InviteRedeemResponseDto(saved.getId(), jwt, invite.getEmail(), invite.getRole());
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @Transactional(readOnly = true)
    public RegistrationInvite getInvite(UUID id) {
        return inviteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Invite not found"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @Transactional(readOnly = true)
    public List<RegistrationInvite> getInvites(List<UUID> ids) {
        return inviteRepository.findAllById(ids);
    }

    private void ensureInvitePolicy(RegistrationInviteRole role) {
        if (hasRole("ROLE_ADMIN")) {
            return;
        }
        if (hasRole("ROLE_TEACHER")) {
            if (role != RegistrationInviteRole.SCHUELER) {
                throw new AccessDeniedException("Teacher can only invite SCHUELER");
            }
            return;
        }
        throw new AccessDeniedException("Unauthorized to create invites");
    }

    private void ensureInviteUsable(RegistrationInvite invite, ParsedToken parsed) {
        if (invite.getStatus() == RegistrationInviteStatus.REVOKED || invite.getStatus() == RegistrationInviteStatus.REDEEMED) {
            throw new IllegalStateException("Invite no longer usable");
        }
        if (invite.isExpired()) {
            invite.setStatus(RegistrationInviteStatus.EXPIRED);
            inviteRepository.save(invite);
            throw new IllegalStateException("Invite expired");
        }
        if (invite.getUsesCount() >= invite.getMaxUses()) {
            invite.setStatus(RegistrationInviteStatus.REDEEMED);
            inviteRepository.save(invite);
            throw new IllegalStateException("Invite uses exceeded");
        }

        byte[] calculatedHash = tokenService.hash(invite.getTokenSalt(), parsed.tokenBytes());
        if (!tokenService.constantTimeEquals(invite.getTokenHash(), calculatedHash)) {
            throw new IllegalArgumentException("Invalid invite token");
        }
    }

    private void assignAdminRole(User user) {
        Admin admin = new Admin();
        admin.setUser(user);
        user.setAdmin(admin);
    }

    private void assignTeacherRole(User user) {
        Teacher teacher = new Teacher();
        teacher.setUser(user);
        user.setTeacher(teacher);
    }

    private void assignStudentRole(User user, UUID classId) {
        Student student = new Student();
        student.setUser(user);
        if (classId != null) {
            Class clazz = classRepository.findById(classId)
                    .orElseThrow(() -> new EntityNotFoundException("Class %s not found".formatted(classId)));
            student.setClazz(clazz);
        }
        user.setStudent(student);
    }

    private String publicToken(UUID inviteId, byte[] token) {
        return inviteId + "." + tokenService.encode(token);
    }

    private ParsedToken parseToken(String token) {
        if (token == null || !token.contains(".")) {
            throw new IllegalArgumentException("Malformed token");
        }
        String[] parts = token.split("\\.", 2);
        UUID inviteId = UUID.fromString(parts[0]);
        byte[] tokenBytes = tokenService.decode(parts[1]);
        return new ParsedToken(inviteId, tokenBytes);
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }

    private UUID currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return UUID.randomUUID();
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return user.getId();
        }
        if (principal instanceof UUID uuid) {
            return uuid;
        }
        if (principal instanceof UserDetails details && details.getUsername() != null) {
            return userRepository.findByUserInfoEmail(details.getUsername())
                    .map(User::getId)
                    .orElseGet(UUID::randomUUID);
        }
        return UUID.randomUUID();
    }

    private record ParsedToken(UUID inviteId, byte[] tokenBytes) {
    }
}
