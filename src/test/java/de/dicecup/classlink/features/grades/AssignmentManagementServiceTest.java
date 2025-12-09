package de.dicecup.classlink.features.grades;

import de.dicecup.classlink.features.classes.ClassTerm;
import de.dicecup.classlink.features.classes.ClassTermRepository;
import de.dicecup.classlink.features.classes.SchoolClass;
import de.dicecup.classlink.features.subjects.Subject;
import de.dicecup.classlink.features.subjects.SubjectRepository;
import de.dicecup.classlink.features.terms.Term;
import de.dicecup.classlink.features.users.domain.roles.Teacher;
import de.dicecup.classlink.features.users.domain.roles.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentManagementServiceTest {

    @Mock
    private ClassTermRepository classTermRepository;
    @Mock
    private SubjectAssignmentRepository assignmentRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private FinalGradeAssignmentRepository finalGradeAssignmentRepository;

    @InjectMocks
    private AssignmentManagementService assignmentManagementService;

    @Test
    void createAssignment_createsAndSaveAssignmentWhenDependenciesExist() {
        String name = UUID.randomUUID().toString();
        UUID classId = UUID.randomUUID();
        UUID termId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID finalGradeAssignmentId = UUID.randomUUID();

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(classId);
        Term term = new Term();
        term.setId(termId);
        ClassTerm classTerm = new ClassTerm();
        classTerm.setSchoolClass(schoolClass);
        classTerm.setTerm(term);

        Subject subject = new Subject();
        subject.setId(subjectId);
        Teacher teacher = new Teacher();
        teacher.setId(teacherId);
        FinalGradeAssignment finalGradeAssignment = new FinalGradeAssignment();
        finalGradeAssignment.setId(finalGradeAssignmentId);


        when(classTermRepository.findBySchoolClassIdAndTermId(classId, termId)).thenReturn(Optional.of(classTerm));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(assignmentRepository.save(any(SubjectAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(finalGradeAssignmentRepository.findById(finalGradeAssignmentId)).thenReturn(Optional.of(finalGradeAssignment));
        when(finalGradeAssignmentRepository.save(any(FinalGradeAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubjectAssignment assignment = assignmentManagementService.createAndSaveAssignment(
                name, classId, termId, subjectId, teacherId, finalGradeAssignmentId, BigDecimal.ONE);

        assertThat(assignment.getName()).isEqualTo(name);
        assertThat(assignment.getSchoolClass()).isEqualTo(schoolClass);
        assertThat(assignment.getTerm()).isEqualTo(term);
        assertThat(assignment.getSubject()).isEqualTo(subject);
        assertThat(assignment.getTeacher()).isEqualTo(teacher);
        assertThat(assignment.getFinalGradeAssignment()).isEqualTo(finalGradeAssignment);
        assertThat(assignment.getWeighting()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void createAndSaveAssignment_throwsWhenClassTermNotFound() {
        String name = UUID.randomUUID().toString();
        UUID classId = UUID.randomUUID();
        UUID termId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID finalGradeAssignmentId = UUID.randomUUID();

        when(classTermRepository.findBySchoolClassIdAndTermId(classId, termId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentManagementService.createAndSaveAssignment(name, classId, termId, subjectId, teacherId, finalGradeAssignmentId, BigDecimal.ONE))
                .isInstanceOf(EntityNotFoundException.class);

        verify(subjectRepository, never()).findById(any());
        verify(teacherRepository, never()).findById(any());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAndSaveAssignment_throwsWhenSubjectNotFound() {
        String name = UUID.randomUUID().toString();
        UUID classId = UUID.randomUUID();
        UUID termId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID finalGradeAssignmentId = UUID.randomUUID();

        ClassTerm classTerm = new ClassTerm();
        classTerm.setSchoolClass(new SchoolClass());
        classTerm.setTerm(new Term());

        when(classTermRepository.findBySchoolClassIdAndTermId(classId, termId)).thenReturn(Optional.of(classTerm));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentManagementService.createAndSaveAssignment(name, classId, termId, subjectId, teacherId, finalGradeAssignmentId, BigDecimal.ONE))
                .isInstanceOf(EntityNotFoundException.class);

        verify(teacherRepository, never()).findById(any());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAndSaveAssignmentNotFound() {
        String name = UUID.randomUUID().toString();
        UUID classId = UUID.randomUUID();
        UUID termId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID finalGradeAssignmentId = UUID.randomUUID();

        ClassTerm classTerm = new ClassTerm();
        classTerm.setSchoolClass(new SchoolClass());
        classTerm.setTerm(new Term());

        Subject subject = new Subject();

        when(classTermRepository.findBySchoolClassIdAndTermId(classId, termId)).thenReturn(Optional.of(classTerm));
        when(subjectRepository.findById(any())).thenReturn(Optional.of(subject));
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentManagementService.createAndSaveAssignment(name, classId, termId, subjectId, teacherId, finalGradeAssignmentId, BigDecimal.ONE))
                .isInstanceOf(EntityNotFoundException.class);

        verify(assignmentRepository, never()).save(any());
    }
}
