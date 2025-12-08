package de.dicecup.classlink.features.classes;

import de.dicecup.classlink.features.assessments.AuthHelper;
import de.dicecup.classlink.features.classes.Class;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.features.users.domain.roles.StudentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClassService {

    private final StudentRepository studentRepository;
    private final ClassSubjectAssignmentRepository assignmentRepository;
    private final AuthHelper authHelper;

    public ClassService(StudentRepository studentRepository,
                        ClassSubjectAssignmentRepository assignmentRepository,
                        AuthHelper authHelper) {
        this.studentRepository = studentRepository;
        this.assignmentRepository = assignmentRepository;
        this.authHelper = authHelper;
    }

    @Transactional(readOnly = true)
    public List<StudentInClassDto> loadStudentsOfClass(UUID classId) {
        UUID teacherId = authHelper.requireTeacherId();
        ensureTeacherAssignedToClass(teacherId, classId);

        return studentRepository.findByClassId(classId).stream()
                .map(this::toDto)
                .toList();
    }

    private void ensureTeacherAssignedToClass(UUID teacherId, UUID classId) {
        boolean hasAssignment = assignmentRepository.findByTeacherId(teacherId).stream()
                .anyMatch(assignment -> assignment.getSchoolClass() != null
                        && classId.equals(assignment.getSchoolClass().getId()));

        if (!hasAssignment) {
            throw new AccessDeniedException("Teacher not assigned to class");
        }
    }

    private StudentInClassDto toDto(Student student) {
        Class clazz = student.getClazz();
        if (clazz == null) {
            throw new IllegalStateException("Student is not assigned to a class");
        }
        UserInfo info = student.getUser().getUserInfo();
        String firstName = info != null ? info.getFirstName() : null;
        String lastName = info != null ? info.getLastName() : null;
        return new StudentInClassDto(
                student.getId(),
                firstName,
                lastName,
                clazz.getId(),
                clazz.getName()
        );
    }
}
