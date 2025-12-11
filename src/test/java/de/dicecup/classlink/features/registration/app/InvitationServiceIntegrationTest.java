package de.dicecup.classlink.features.registration.app;

import de.dicecup.classlink.features.classes.SchoolClass;
import de.dicecup.classlink.features.classes.SchoolClassRepository;
import de.dicecup.classlink.features.registration.InvitationService;
import de.dicecup.classlink.features.registration.domain.*;
import de.dicecup.classlink.features.users.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.features.users.domain.roles.Admin;
import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.persistence.IntegrationTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;

class InvitationServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    InvitationService invitationService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    SchoolClassRepository schoolClassRepository;

    @AfterEach
    void cleanupSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminCanCreateAndRedeemInvite() {
        User admin = persistAdminUser("admin@example.com");
        authenticate(admin);

        CreateInviteRequestDto request = new CreateInviteRequestDto("new.user@example.com", RegistrationInviteRole.STUDENT, null, 1, null);
        InviteCreatedResponseDto created = invitationService.createInvite(request);

        SecurityContextHolder.clearContext();

        InviteValidationResponseDto validation = invitationService.validate(new InviteValidationRequestDto(created.token()));
        assertThat(validation.email()).isEqualTo("new.user@example.com");
        assertThat(validation.remainingUses()).isEqualTo(1);

        InviteRedeemResponseDto redeemed = invitationService.redeem(new InviteRedeemRequestDto(created.token(), "newuser", "Passw0rd!"));
        assertThat(redeemed.jwt()).isNotBlank();
        assertThat(userRepository.findById(redeemed.userId())).isPresent();
    }

    @Test
    void teacherCannotInviteAdmin() {
        User teacher = persistTeacherUser("teacher@example.com");
        authenticate(teacher);

        CreateInviteRequestDto request = new CreateInviteRequestDto("admin.candidate@example.com", RegistrationInviteRole.ADMIN, null, 1, null);
        assertThatThrownBy(() -> invitationService.createInvite(request))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    @Test
    void redeemAssignsClassToStudent() {
        User admin = persistAdminUser("admin2@example.com");
        authenticate(admin);

        SchoolClass clazz = new SchoolClass();
        clazz.setName("10A");
        clazz = schoolClassRepository.save(clazz);

        CreateInviteRequestDto request = new CreateInviteRequestDto("student@example.com", RegistrationInviteRole.STUDENT, clazz.getId(), 1, "note");
        InviteCreatedResponseDto created = invitationService.createInvite(request);
        SecurityContextHolder.clearContext();

        invitationService.redeem(new InviteRedeemRequestDto(created.token(), "student1", "Passw0rd!"));

        User studentUser = userRepository.findByUserInfoEmail("student@example.com").orElseThrow();
        Student student = studentUser.getStudent();
        assertThat(student).isNotNull();
        assertThat(student.getSchoolClass()).isNotNull();
        assertThat(student.getSchoolClass().getId()).isEqualTo(clazz.getId());
    }

    private User persistAdminUser(String email) {
        User user = baseUser(email);

        Admin admin = new Admin();
        admin.setUser(user);
        user.setAdmin(admin);

        return userRepository.save(user);
    }

    private User persistTeacherUser(String email) {
        User user = baseUser(email);

        var teacher = new de.dicecup.classlink.features.users.domain.roles.Teacher();
        teacher.setUser(user);
        user.setTeacher(teacher);
        return userRepository.save(user);
    }

    private User baseUser(String email) {
        User user = new User();
        user.setUsername(email);
        user.setPasswordHash("hash");
        user.setEnabled(true);

        UserInfo info = new UserInfo();
        info.setUser(user);
        info.setEmail(email);
        user.setUserInfo(info);
        return userRepository.save(user);
    }

    private void authenticate(User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
