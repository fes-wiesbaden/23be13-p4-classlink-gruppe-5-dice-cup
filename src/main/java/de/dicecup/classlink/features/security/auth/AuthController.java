package de.dicecup.classlink.features.security.auth;

import de.dicecup.classlink.features.security.JwtService;
import de.dicecup.classlink.features.security.refreshtoken.RefreshRotationResult;
import de.dicecup.classlink.features.security.refreshtoken.RefreshTokenResult;
import de.dicecup.classlink.features.security.refreshtoken.RefreshTokenService;
import de.dicecup.classlink.features.users.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Operation(
            summary = "Anmeldung durchführen",
            description = "Authentifiziert einen Benutzer und gibt Access- und Refresh-Token zurück."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anmeldung erfolgreich, Tokens wurden ausgegeben."),
            @ApiResponse(responseCode = "401", description = "Ungültige Anmeldedaten.")
    })
    /**
     * Führt die Benutzeranmeldung durch und gibt JWT-Access- und Refresh-Tokens aus.
     *
     * @param request Login-Daten mit E-Mail und Passwort
     * @return Antwort mit Access- und Refresh-Token
     */
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password())
            );

            User user = (User) authentication.getPrincipal();
            RefreshTokenResult refreshToken = refreshTokenService.issue(user);
            String accessToken = jwtService.generateToken(buildRoleClaims(user), user);

            return new TokenResponse(accessToken, refreshToken.token());
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    @Operation(
            summary = "Tokens erneuern",
            description = "Erneuert den Access-Token mithilfe eines gültigen Refresh-Tokens."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token wurde erfolgreich erneuert."),
            @ApiResponse(responseCode = "401", description = "Refresh-Token ist ungültig oder abgelaufen.")
    })
    /**
     * Erneuert Tokens anhand eines gültigen Refresh-Tokens.
     *
     * @param request Anfrage mit dem vorhandenen Refresh-Token
     * @return Neue Access- und Refresh-Token
     */
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshRotationResult rotation = refreshTokenService.rotate(request.refreshToken());
        String accessToken = jwtService.generateToken(buildRoleClaims(rotation.user()), rotation.user());
        return new TokenResponse(accessToken, rotation.refreshToken());
    }

    private Map<String, Object> buildRoleClaims(User user) {
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return Map.of("roles", roles);
    }

    public record LoginRequest(
            @NotBlank String email,
            @NotBlank String password
    ) {
    }

    public record RefreshRequest(
            @NotBlank String refreshToken
    ) {

    }
}
