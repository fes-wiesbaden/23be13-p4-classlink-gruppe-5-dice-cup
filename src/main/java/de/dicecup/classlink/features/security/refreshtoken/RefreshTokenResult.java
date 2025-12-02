package de.dicecup.classlink.features.security.refreshtoken;

import java.time.Instant;

public record RefreshTokenResult(String token, Instant expiresAt) {
}
