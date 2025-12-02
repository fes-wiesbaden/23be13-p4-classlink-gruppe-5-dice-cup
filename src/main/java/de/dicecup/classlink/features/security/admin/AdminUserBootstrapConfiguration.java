package de.dicecup.classlink.features.security.admin;

import de.dicecup.classlink.features.users.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile({"dev", "prod"})
public class AdminUserBootstrapConfiguration {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_ADMIN_EMAIL = "admin@dicecup.local";
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final int DEFAULT_PASSWORD_LENGTH = 16;

    @Bean
    public ApplicationRunner adminUserInitializer() {
        return args -> {
            boolean adminExists = userRepository.findByUserInfoEmail(DEFAULT_ADMIN_EMAIL).isPresent();
            if (adminExists) {
                log.info("Admin user '{}' already exists. Skipping creation.", DEFAULT_ADMIN_EMAIL);
                return;
            }
            String randomPassword = generateRandomPassword();
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode(randomPassword));
            admin.setEnabled(true);

            UserInfo userInfo = new UserInfo();
            userInfo.setFirstName("DiceCup");
            userInfo.setLastName("Admin");
            userInfo.setEmail("admin@dicecup.local");

            userInfo.setUser(admin);
            admin.setUserInfo(userInfo);

            userRepository.save(admin);

            log.warn("======================================================");
            log.warn(" INITIAL ADMIN USER CREATED");
            log.warn("   email    : {}", DEFAULT_ADMIN_EMAIL);
            log.warn("   username : {}", DEFAULT_ADMIN_USERNAME);
            log.warn("   PASSWORD : {}", randomPassword);
            log.warn("   -> Bitte dieses Passwort sicher notieren und dann");
            log.warn("      im System nach dem ersten Login ändern.");
            log.warn("======================================================");

        };
    }

    private String generateRandomPassword() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[DEFAULT_PASSWORD_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
