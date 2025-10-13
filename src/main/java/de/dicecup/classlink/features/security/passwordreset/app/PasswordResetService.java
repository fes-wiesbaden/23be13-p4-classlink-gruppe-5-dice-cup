package de.dicecup.classlink.features.security.passwordreset.app;

import de.dicecup.classlink.features.security.PasswordService;
import de.dicecup.classlink.features.security.TokenService;
import de.dicecup.classlink.features.security.TokenService.TokenBundle;
import de.dicecup.classlink.features.security.passwordreset.domain.PasswordResetToken;
import de.dicecup.classlink.features.security.passwordreset.domain.PasswordResetTokenStatus;
import de.dicecup.classlink.features.security.passwordreset.repo.PasswordResetTokenRepository;
import de.dicecup.classlink.features.security.passwordreset.web.PasswordResetCommitRequestDto;
import de.dicecup.classlink.features.security.passwordreset.web.PasswordResetCreateRequestDto;
import de.dicecup.classlink.features.security.passwordreset.web.PasswordResetCreateResponseDto;
import de.dicecup.classlink.features.security.passwordreset.web.PasswordResetValidateRequestDto;
import de.dicecup.classlink.features.security.passwordreset.web.PasswordResetValidateResponseDto;
import de.dicecup.classlink.features.users.app.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    public PasswordResetCreateResponseDto createResetToken(PasswordResetCreateRequestDto request) {
        UUID userId = request.userId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        revokeExisting(userId);

        TokenBundle bundle = tokenService.generateToken();
        PasswordResetToken token = new PasswordResetToken();
        UUID tokenId = UUID.randomUUID();
        token.setId(tokenId);
        token.setUserId(userId);
        token.setTokenSalt(bundle.salt());
        token.setTokenHash(bundle.hash());
        token.setStatus(PasswordResetTokenStatus.PENDING);
        token.setExpiresAt(Instant.now().plus(60, ChronoUnit.MINUTES));
        String publicToken = publicToken(tokenId, bundle.token());
        token.setPublicToken(publicToken);
        tokenRepository.save(token);

        return new PasswordResetCreateResponseDto(tokenId, publicToken, token.getExpiresAt(), userId);
    }

    public PasswordResetValidateResponseDto validate(PasswordResetValidateRequestDto request) {
        ParsedToken parsed = parseToken(request.token());
        PasswordResetToken token = tokenRepository.findById(parsed.tokenId())
                .orElseThrow(() -> new EntityNotFoundException("Token not found"));
        ensureUsable(token, parsed);
        return new PasswordResetValidateResponseDto(token.getId(), token.getUserId(), token.getExpiresAt());
    }

    public void commit(PasswordResetCommitRequestDto request) {
        ParsedToken parsed = parseToken(request.token());
        PasswordResetToken token = tokenRepository.findForUpdate(parsed.tokenId())
                .orElseThrow(() -> new EntityNotFoundException("Token not found"));
        ensureUsable(token, parsed);

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setPasswordHash(passwordService.hashPassword(request.newPassword()));

        token.setStatus(PasswordResetTokenStatus.REDEEMED);
        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public PasswordResetToken getToken(UUID id) {
        return tokenRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Token not found"));
    }

    private void ensureUsable(PasswordResetToken token, ParsedToken parsed) {
        if (token.getStatus() == PasswordResetTokenStatus.REVOKED || token.getStatus() == PasswordResetTokenStatus.REDEEMED) {
            throw new IllegalStateException("Token already used");
        }
        if (token.isExpired()) {
            token.setStatus(PasswordResetTokenStatus.EXPIRED);
            tokenRepository.save(token);
            throw new IllegalStateException("Token expired");
        }
        byte[] calculated = tokenService.hash(token.getTokenSalt(), parsed.rawBytes());
        if (!tokenService.constantTimeEquals(token.getTokenHash(), calculated)) {
            throw new IllegalArgumentException("Invalid token");
        }
    }

    private void revokeExisting(UUID userId) {
        List<PasswordResetToken> active = tokenRepository.findActiveByUserId(userId, PasswordResetTokenStatus.PENDING, Instant.now());
        active.forEach(token -> token.setStatus(PasswordResetTokenStatus.REVOKED));
        tokenRepository.saveAll(active);
    }

    private String publicToken(UUID tokenId, byte[] token) {
        return tokenId + "." + tokenService.encode(token);
    }

    private ParsedToken parseToken(String publicToken) {
        if (publicToken == null || !publicToken.contains(".")) {
            throw new IllegalArgumentException("Malformed token");
        }
        String[] parts = publicToken.split("\\.", 2);
        UUID id = UUID.fromString(parts[0]);
        byte[] raw = tokenService.decode(parts[1]);
        return new ParsedToken(id, raw);
    }

    private record ParsedToken(UUID tokenId, byte[] rawBytes) {
    }
}
