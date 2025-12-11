package de.dicecup.classlink.features.classes;

import de.dicecup.classlink.features.assessments.AuthHelper;
import de.dicecup.classlink.features.grades.SubjectAssignment;
import de.dicecup.classlink.features.grades.SubjectAssignmentRepository;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.features.users.domain.roles.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private SubjectAssignmentRepository assignmentRepository;
    @Mock
    private AuthHelper authHelper;

    @InjectMocks
    private ClassService classService;

    @Test
    void loadStudentsOfClass_returnsDtos_whenTeacherHasAccess() {
        UUID classId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(classId);
        schoolClass.setName("1A");

        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setSchoolClass(schoolClass);

        Student student = new Student();
        student.setId(UUID.randomUUID());
        student.setSchoolClass(schoolClass);

        User user = new User();
        UserInfo info = new UserInfo();
        info.setFirstName("Alice");
        info.setLastName("Anderson");
        info.setUser(user);
        user.setUserInfo(info);
        student.setUser(user);

        when(authHelper.requireTeacherId()).thenReturn(teacherId);
        when(assignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of(assignment));
        when(studentRepository.findByClassId(classId)).thenReturn(List.of(student));

        List<StudentInClassDto> result = classService.loadStudentsOfClass(classId);

        assertThat(result).hasSize(1);
        StudentInClassDto dto = result.getFirst();
        assertThat(dto.studentId()).isEqualTo(student.getId());
        assertThat(dto.classId()).isEqualTo(classId);
        assertThat(dto.firstName()).isEqualTo("Alice");
        assertThat(dto.className()).isEqualTo("1A");
    }

    @Test
    void loadStudentsOfClass_deniesAccess_whenTeacherNotAssigned() {
        UUID classId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();

        when(authHelper.requireTeacherId()).thenReturn(teacherId);
        when(assignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of());

        assertThatThrownBy(() -> classService.loadStudentsOfClass(classId))
                .isInstanceOf(AccessDeniedException.class);
    }
}
