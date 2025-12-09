package de.dicecup.classlink.features.security.auth;

import de.dicecup.classlink.features.security.JwtService;
import de.dicecup.classlink.features.users.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/dev")
@Profile("dev")
@RequiredArgsConstructor
public class DevTokenController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Entwickler-Token generieren",
            description = "Erzeugt ein JWT für einen Benutzer im Dev-Profil."
    )
    @ApiResponse(responseCode = "200", description = "Token wurde erfolgreich generiert.")
    /**
     * Generiert ein JWT für einen Benutzer im Dev-Modus.
     *
     * @param username          Benutzername, für den das Token erzeugt wird (Standard: admin)
     * @param expirationMillis  Gültigkeitsdauer des Tokens in Millisekunden
     * @return Signiertes JWT
     */
    @GetMapping("/token")
    public String getDevToken(
            @RequestParam(defaultValue = "admin") String username,
            @RequestParam(defaultValue = "${security.jwt.dev-expiration-time}") long expirationMillis
    ) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        return jwtService.generateToken(user, expirationMillis);
    }
}
