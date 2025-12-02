package de.dicecup.classlink.features.security.auth;

public record TokenResponse(String accessToken, String refreshToken) {
}
