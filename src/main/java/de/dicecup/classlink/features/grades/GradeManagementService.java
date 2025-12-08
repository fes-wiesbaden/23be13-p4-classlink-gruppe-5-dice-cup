package de.dicecup.classlink.features.grades;

import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.features.users.domain.roles.StudentRepository;
import de.dicecup.classlink.features.users.domain.roles.Teacher;
import de.dicecup.classlink.features.users.domain.roles.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GradeManagementService {

    private final GradeRepository gradeRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final SubjectAssignmentRepository assignmentRepository;

    @Transactional
    public Grade createGrade(UUID teacherId,
                             UUID studentId,
                             UUID assignmentId,
                             BigDecimal gradeValue) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        SubjectAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        Grade grade = new Grade();
        grade.setChangedBy(teacher);
        grade.setStudent(student);
        grade.setGradeValue(gradeValue);
        grade.setSubjectAssignment(assignment);

        return gradeRepository.save(grade);
    }

    @Transactional(readOnly = true)
    public List<Grade> listGradesByStudent(UUID studentId) {
        return gradeRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<Grade> listGradesByAssignment(UUID assignmentId) {
        return gradeRepository.findByClassSubjectAssignmentId(assignmentId);
    }
}
