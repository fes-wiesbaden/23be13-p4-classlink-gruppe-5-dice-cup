package de.dicecup.classlink.features.security.auth;

import de.dicecup.classlink.features.security.JwtService;
import de.dicecup.classlink.features.users.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
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
