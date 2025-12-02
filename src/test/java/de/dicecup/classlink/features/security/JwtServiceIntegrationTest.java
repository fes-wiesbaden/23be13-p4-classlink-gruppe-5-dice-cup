package de.dicecup.classlink.features.security;

import de.dicecup.classlink.persistence.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    JwtService jwtService;

    @Test
    void generatedTokenIsValidAndCarriesClaims() {
        UserDetails user = createUser("jwt.user@example.com");
        Map<String, Object> claims = Map.of("role", "ADMIN");

        String token = jwtService.generateToken(claims, user);

        assertThat(jwtService.extractUsername(token)).isEqualTo(user.getUsername());
        String role = jwtService.extractClaim(token, jwtClaims -> jwtClaims.get("role", String.class));
        assertThat(role).isEqualTo("ADMIN");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
        assertThat(jwtService.getExpirationTime()).isGreaterThan(0L);
    }

    @Test
    void tokenGeneratedForAnotherUserIsInvalid() {
        UserDetails owner = createUser("owner@example.com");
        UserDetails other = createUser("other@example.com");

        String token = jwtService.generateToken(owner);

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void expiredTokenIsConsideredInvalid() {
        UserDetails user = createUser("expired@example.com");
        long originalExpiration = jwtService.getExpirationTime();
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);

        try {
            String token = jwtService.generateToken(user);
            assertThatThrownBy(() -> jwtService.isTokenValid(token, user))
                    .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
        } finally {
            ReflectionTestUtils.setField(jwtService, "jwtExpiration", originalExpiration);
        }
    }

    private UserDetails createUser(String username) {
        return User.withUsername(username)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }
}
