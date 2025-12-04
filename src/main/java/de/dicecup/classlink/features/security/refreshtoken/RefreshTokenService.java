package de.dicecup.classlink.features.security.refreshtoken;

import de.dicecup.classlink.features.security.TokenService;
import de.dicecup.classlink.features.users.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;

    @Value("${security.refresh-token.expiration:30d}")
    private Duration refreshTokenTtl;

    public RefreshTokenResult issue(User user) {
        revokeActiveTokens(user);
        return createRefreshToken(user);
    }

    public RefreshRotationResult rotate(String presentedToken) {
        ParsedToken parsed = parseToken(presentedToken);
        RefreshToken stored = refreshTokenRepository.findById(parsed.id())
                .orElseThrow(() -> new RefreshTokenException(RefreshTokenException.Reason.NOT_FOUND, "Refresh token not found"));

        Instant now = Instant.now();
        byte[] calculatedHash = tokenService.hash(stored.getTokenSalt(), parsed.value());
        if (!tokenService.constantTimeEquals(calculatedHash, stored.getTokenHash())) {
            markReuseIfNecessary(stored, now);
            throw new RefreshTokenException(RefreshTokenException.Reason.INVALID, "Invalid refresh token");
        }

        if (stored.getExpiresAt().isBefore(now)) {
            stored.setRevokedAt(now);
            throw new RefreshTokenException(RefreshTokenException.Reason.EXPIRED, "Refresh token expired");
        }

        if (stored.getReusedAt() != null) {
            throw new RefreshTokenException(RefreshTokenException.Reason.REUSED, "Refresh token reuse detected");
        }

        if (stored.getRevokedAt() != null) {
            markReuseIfNecessary(stored, now);
            throw new RefreshTokenException(RefreshTokenException.Reason.ROTATED, "Refresh token already rotated");
        }

        stored.setRevokedAt(now);
        var user = stored.getUser();
        // Ensure the associated user is initialized before leaving the transaction
        user.getUsername();
        user.getAuthorities();
        RefreshTokenResult newToken = createRefreshToken(user);
        return new RefreshRotationResult(user, newToken.token(), newToken.expiresAt());
    }

    private RefreshTokenResult createRefreshToken(User user) {
        TokenService.TokenBundle bundle = tokenService.generateToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(UUID.randomUUID());
        refreshToken.setUser(user);
        refreshToken.setTokenSalt(bundle.salt());
        refreshToken.setTokenHash(bundle.hash());
        refreshToken.setExpiresAt(Instant.now().plus(refreshTokenTtl));

        refreshTokenRepository.save(refreshToken);
        String publicToken = buildPublicToken(refreshToken.getId(), bundle.token());
        return new RefreshTokenResult(publicToken, refreshToken.getExpiresAt());
    }

    private void revokeActiveTokens(User user) {
        Instant now = Instant.now();
        List<RefreshToken> activeTokens = refreshTokenRepository.findAllByUserIdAndRevokedAtIsNull(user.getId());
        activeTokens.stream()
                .filter(token -> token.getExpiresAt().isAfter(now))
                .forEach(token -> token.setRevokedAt(now));
    }

    private void markReuseIfNecessary(RefreshToken token, Instant when) {
        if (token.getReusedAt() == null) {
            token.setReusedAt(when);
            revokeActiveTokens(token.getUser());
        }
    }

    private ParsedToken parseToken(String value) {
        if (value == null || !value.contains(".")) {
            throw new RefreshTokenException(RefreshTokenException.Reason.MALFORMED, "Malformed refresh token");
        }
        try {
            String[] parts = value.split("\\.", 2);
            UUID id = UUID.fromString(parts[0]);
            byte[] tokenBytes = tokenService.decode(parts[1]);
            return new ParsedToken(id, tokenBytes);
        } catch (IllegalArgumentException ex) {
            throw new RefreshTokenException(RefreshTokenException.Reason.MALFORMED, "Malformed refresh token");
        }
    }

    private String buildPublicToken(UUID id, byte[] token) {
        return id + "." + tokenService.encode(token);
    }

    private record ParsedToken(UUID id, byte[] value) {
    }
}
