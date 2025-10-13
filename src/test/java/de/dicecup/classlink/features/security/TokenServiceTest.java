package de.dicecup.classlink.features.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {

    private final TokenService tokenService = new TokenService();

    @Test
    void generateTokenProducesRandomValues() {
        var first = tokenService.generateToken();
        var second = tokenService.generateToken();

        assertThat(first.token()).isNotEqualTo(second.token());
        assertThat(first.salt()).isNotEqualTo(second.salt());
        assertThat(first.hash()).isNotEqualTo(second.hash());
    }

    @Test
    void hashMatchesAndConstantTimeComparisonWorks() {
        var bundle = tokenService.generateToken();
        var hash = tokenService.hash(bundle.salt(), bundle.token());

        assertThat(tokenService.constantTimeEquals(hash, bundle.hash())).isTrue();
        assertThat(tokenService.constantTimeEquals(hash, tokenService.generateToken().hash())).isFalse();
    }

    @Test
    void encodeAndDecodeRoundtrip() {
        var bundle = tokenService.generateToken();
        String encoded = tokenService.encode(bundle.token());
        assertThat(tokenService.decode(encoded)).isEqualTo(bundle.token());
    }
}
