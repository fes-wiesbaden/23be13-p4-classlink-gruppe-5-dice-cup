package de.dicecup.classlink.features.security.refreshtoken;

import de.dicecup.classlink.features.security.PasswordService;
import de.dicecup.classlink.features.users.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.persistence.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class RefreshTokenApiIntegrationTest extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    PasswordService passwordService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RefreshTokenService refreshTokenService;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Test
    void refreshReturnsNewTokensForValidToken() throws Exception {
        User user = ensureUser("refresh.valid@example.com", "Secret123!");
        RefreshTokenResult result = refreshTokenService.issue(user);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(result.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void refreshFailsWhenTokenExpired() throws Exception {
        User user = ensureUser("refresh.expired@example.com", "Secret123!");
        RefreshTokenResult result = refreshTokenService.issue(user);

        RefreshToken stored = refreshTokenRepository.findById(extractTokenId(result.token())).orElseThrow();
        stored.setExpiresAt(Instant.now().minusSeconds(60));
        refreshTokenRepository.save(stored);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(result.token())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_EXPIRED"));
    }

    @Test
    void refreshFailsWhenTokenAlreadyRotated() throws Exception {
        User user = ensureUser("refresh.rotated@example.com", "Secret123!");
        RefreshTokenResult result = refreshTokenService.issue(user);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(result.token())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload(result.token())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_ALREADY_ROTATED"));
    }

    @Test
    void refreshFailsWithMalformedToken() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload("abc")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_MALFORMED"));
    }

    private User ensureUser(String email, String rawPassword) {
        User user = new User();
        user.setUsername(email);
        user.setEnabled(true);
        user.setPasswordHash(passwordService.hashPassword(rawPassword));

        UserInfo info = new UserInfo();
        info.setUser(user);
        info.setEmail(email);
        user.setUserInfo(info);

        return userRepository.save(user);
    }

    private UUID extractTokenId(String token) {
        int delimiter = token.indexOf('.');
        assertThat(delimiter).isGreaterThan(0);
        return UUID.fromString(token.substring(0, delimiter));
    }

    private String refreshPayload(String token) {
        return "{\"refreshToken\":\"" + token + "\"}";
    }
}
