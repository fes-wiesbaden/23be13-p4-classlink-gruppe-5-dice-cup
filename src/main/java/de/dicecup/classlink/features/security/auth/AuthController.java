package de.dicecup.classlink.features.security.auth;

import de.dicecup.classlink.features.security.JwtService;
import de.dicecup.classlink.features.security.refreshtoken.RefreshRotationResult;
import de.dicecup.classlink.features.security.refreshtoken.RefreshTokenException;
import de.dicecup.classlink.features.security.refreshtoken.RefreshTokenResult;
import de.dicecup.classlink.features.security.refreshtoken.RefreshTokenService;
import de.dicecup.classlink.features.users.domain.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password())
            );

            User user = (User) authentication.getPrincipal();
            RefreshTokenResult refreshToken = refreshTokenService.issue(user);
            String accessToken = jwtService.generateToken(user);

            return new TokenResponse(accessToken, refreshToken.token());
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        try {
            RefreshRotationResult rotation = refreshTokenService.rotate(request.refreshToken());
            String accessToken = jwtService.generateToken(rotation.user());
            return new TokenResponse(accessToken, rotation.refreshToken());
        } catch (RefreshTokenException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex);
        }
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
