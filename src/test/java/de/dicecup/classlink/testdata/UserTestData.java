package de.dicecup.classlink.testdata;

import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.UserInfo;
import net.datafaker.Faker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.*;

public class UserTestData {
    private final Faker faker;
    private final PasswordEncoder passwordEncoder;

    public UserTestData() {
        this.faker = new Faker(Locale.GERMAN, new Random(42L));
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User userWithInfo() {
        User user = new User();
        user.setUsername(safeUsername());
        user.setPasswordHash(passwordEncoder.encode(faker.internet().password(12, 18)));
        user.setEnabled(true);
        user.setCreatedAt(Instant.now());
        user.setCreatedBy(UUID.randomUUID());
        user.setVersion(0);

        UserInfo userInfo = new UserInfo();
        userInfo.setFirstName(faker.name().firstName());
        userInfo.setLastName(faker.name().lastName());
        userInfo.setEmail(faker.internet().emailAddress());

        userInfo.setUser(user);
        user.setUserInfo(userInfo);

        return user;
    }

    public List<User> usersWithInfo(int n) {
        List<User> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(userWithInfo());
        }
        return list;
    }

    private String safeUsername() {
        return faker.name().username().replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
