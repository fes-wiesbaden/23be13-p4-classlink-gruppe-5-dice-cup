package de.dicecup.classlink.features.security.dev;

import de.dicecup.classlink.features.users.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.features.users.domain.roles.Teacher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("dev")
public class DevUsersBootstrapConfiguration {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEV_TEACHER_EMAIL = "clara.lehrwerk@classlink.dev";
    private static final String DEV_TEACHER_PASSWORD = "Teach3rR0cks!";

    private static final String DEV_STUDENT_EMAIL = "max.mustermann@classlink.dev";
    private static final String DEV_STUDENT_PASSWORD = "Studi3sHard!";

    @Bean
    public ApplicationRunner devUsersInitializer() {
        return args -> {
            createTeacherIfMissing();
            createStudentIfMissing();
        };
    }

    private void createTeacherIfMissing() {
        if (userRepository.findByUserInfoEmail(DEV_TEACHER_EMAIL).isPresent()) {
            return;
        }

        User user = new User();
        user.setUsername(DEV_TEACHER_EMAIL);
        user.setPasswordHash(passwordEncoder.encode(DEV_TEACHER_PASSWORD));
        user.setEnabled(true);

        UserInfo info = new UserInfo();
        info.setFirstName("Clara");
        info.setLastName("Lehrwerk");
        info.setEmail(DEV_TEACHER_EMAIL);
        info.setUser(user);
        user.setUserInfo(info);

        Teacher teacher = new Teacher();
        teacher.setUser(user);
        user.setTeacher(teacher);

        userRepository.save(user);
        log.info("Dev teacher user created: {}", DEV_TEACHER_EMAIL);
    }

    private void createStudentIfMissing() {
        if (userRepository.findByUserInfoEmail(DEV_STUDENT_EMAIL).isPresent()) {
            return;
        }

        User user = new User();
        user.setUsername(DEV_STUDENT_EMAIL);
        user.setPasswordHash(passwordEncoder.encode(DEV_STUDENT_PASSWORD));
        user.setEnabled(true);

        UserInfo info = new UserInfo();
        info.setFirstName("Max");
        info.setLastName("Mustermann");
        info.setEmail(DEV_STUDENT_EMAIL);
        info.setUser(user);
        user.setUserInfo(info);

        Student student = new Student();
        student.setUser(user);
        user.setStudent(student);

        userRepository.save(user);
        log.info("Dev student user created: {}", DEV_STUDENT_EMAIL);
    }
}
