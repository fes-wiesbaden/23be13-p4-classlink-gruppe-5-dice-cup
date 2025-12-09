package de.dicecup.classlink.features.grades;

import de.dicecup.classlink.features.classes.SchoolClass;
import de.dicecup.classlink.features.classes.SchoolClassRepository;
import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.features.users.domain.roles.StudentRepository;
import de.dicecup.classlink.features.users.domain.roles.Teacher;
import de.dicecup.classlink.features.users.domain.roles.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final FinalGradeAssignmentRepository finalGradeAssignmentRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final FinalGradeRepository finalGradeRepository;

    @Transactional
    public Grade createGrade(UUID teacherId,
                             UUID studentId,
                             UUID assignmentId,
                             BigDecimal gradeValue
    ) {

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        SubjectAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        if (assignment.getTeacher().getId() != teacher.getId() && userIsNotAdmin()) {
            throw new IllegalStateException("User is not privileged");
        }

        Grade grade = new Grade();
        grade.setChangedBy(teacher);
        grade.setStudent(student);
        grade.setGradeValue(gradeValue);
        grade.setSubjectAssignment(assignment);

        return gradeRepository.save(grade);
    }

    @Transactional
    public Grade updateGrade(
            UUID teacherId,
            UUID gradeId,
            BigDecimal gradeValue
    ) {
        if(userIsNotAdmin()){
            throw new IllegalStateException("User is not an admin");
        }

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new EntityNotFoundException("Grade not found"));

        if (grade.getSubjectAssignment()
                .getTeacher()
                .getId() != teacher.getId() && userIsNotAdmin()){
            throw new IllegalStateException("Teacher does not own grade");
        }

        grade.setChangedBy(teacher);
        grade.setGradeValue(gradeValue);

        return gradeRepository.save(grade);
    }

    @Transactional
    public List<FinalGrade> calculateFinalGrades(UUID assignment_id) {
        FinalGradeAssignment finalGradeAssignment = finalGradeAssignmentRepository.findById(assignment_id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
        SchoolClass schoolClass = finalGradeAssignment.getSchoolClass();
        List<Student> students = studentRepository
                .findBySchoolClassId(schoolClass.getId())
                .orElseThrow(() -> new EntityNotFoundException("Class not found"));
        return students.stream()
                .map(student -> calculateFinalGradePerStudentFromId(assignment_id, student.getId()))
                .toList();
    }


    @Transactional
    public FinalGrade calculateFinalGradePerStudentFromId(UUID assignment_id, UUID student_id) {
        FinalGradeAssignment finalGradeAssignment = finalGradeAssignmentRepository.findById(assignment_id)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
        Student student = studentRepository.findById(student_id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        List<Grade> subGrades = finalGradeAssignment
                .getSubGradeAssignments()
                .stream()
                .map(
                        assignment ->
                                gradeRepository
                                        .findByStudentIdAndSubjectAssignmentId(student_id, assignment.getId())
                                        .orElseThrow(() -> new EntityNotFoundException("Grade not found"))
                )
                .toList();
        List<SubjectAssignment> subGradeAssignments = finalGradeAssignment.getSubGradeAssignments();

        if(subGrades.size() != subGradeAssignments.size()){
            throw new IllegalStateException(
                    "Ungraded assignments found for: " +
                            student.getUser().getUserInfo().getFirstName() +
                            student.getUser().getUserInfo().getLastName()
            );
        }

        BigDecimal weightedTotal = subGrades.stream()
                .map(Grade -> Grade.getGradeValue().multiply(
                        Grade.getSubjectAssignment().getWeighting()
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        FinalGrade calculatedGrade = new FinalGrade();
        calculatedGrade.setStudent(student);
        calculatedGrade.setGradeValue(weightedTotal);

        List<FinalGrade> temp = finalGradeAssignment.getGrades();
        temp.add(calculatedGrade);
        finalGradeAssignment.setGrades(temp);
        finalGradeAssignmentRepository.save(finalGradeAssignment);

        return finalGradeRepository.save(calculatedGrade);
    }



    @Transactional(readOnly = true)
    public List<Grade> listGradesByStudent(UUID studentId) {
        return gradeRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<Grade> listGradesByAssignment(UUID assignmentId) {
        return gradeRepository.findBySubjectAssignmentId(assignmentId);
    }

    public List<Grade> GetAllGradesForStudentPerTerm(UUID studentId, UUID termId){
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        SchoolClass studentClass = student.getSchoolClass();
        List<UUID> allAssignmentIdsForClassInTerm =
                assignmentRepository
                        .findBySchoolClassIdAndTermId(studentClass.getId(), termId)
                        .stream()
                        .map(SubjectAssignment::getId)
                        .toList();

        List<Grade> termGrades = gradeRepository.findByStudentIdAndSubjectAssignmentIdIn(studentId, allAssignmentIdsForClassInTerm);
        if (termGrades.isEmpty()){
            throw new EntityNotFoundException("No grades found for student");
        }
        return termGrades;
    }

    public boolean GradeDoesNotMatchAssignment(UUID gradeId, String assignmentId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new EntityNotFoundException("Grade not found"));
        SubjectAssignment assignment = assignmentRepository.findById(UUID.fromString(assignmentId))
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        return !grade.getSubjectAssignment().getId().equals(assignment.getId());
    }

    public boolean userIsNotAdmin(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN"));
    }
}
