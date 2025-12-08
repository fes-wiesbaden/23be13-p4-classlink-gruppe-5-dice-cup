package de.dicecup.classlink.features.assessments;

import de.dicecup.classlink.features.users.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.roles.TeacherRepository;
import de.dicecup.classlink.features.users.domain.roles.StudentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuthHelper {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public AuthHelper(UserRepository userRepository, TeacherRepository teacherRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }

    public UUID requireUserId() {
        return resolveCurrentUserId().orElseThrow(() -> new AccessDeniedException("User not authenticated"));
    }

    public UUID requireStudentId() {
        UUID userId = requireUserId();
        if (!studentRepository.existsById(userId)) {
            throw new AccessDeniedException("User is not a student");
        }
        return userId;
    }

    public UUID requireTeacherId() {
        UUID userId = requireUserId();
        if (!teacherRepository.existsById(userId)) {
            throw new AccessDeniedException("User is not a teacher");
        }
        return userId;
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private Optional<UUID> resolveCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return Optional.ofNullable(user.getId());
        }
        if (principal instanceof UUID uuid) {
            return Optional.of(uuid);
        }
        if (principal instanceof UserDetails details && details.getUsername() != null) {
            return userRepository.findByUserInfoEmail(details.getUsername()).map(User::getId);
        }
        return Optional.empty();
    }
}
