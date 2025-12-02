package de.dicecup.classlink.features.security.refreshtoken;

import de.dicecup.classlink.features.users.domain.User;

import java.time.Instant;

public record RefreshRotationResult(User user, String refreshToken, Instant refreshExpiresAt) {
}
