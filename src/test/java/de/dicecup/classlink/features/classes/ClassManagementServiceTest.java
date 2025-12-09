package de.dicecup.classlink.features.classes;

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
class ClassManagementServiceTest {

    @Mock
    private ClassTermRepository classTermRepository;
    @Mock
    private ClassSubjectAssignmentRepository assignmentRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private ClassManagementService classManagementService;

    @Test
    void assignTeacher_createsAssignmentWhenDependenciesExist() {
        UUID classId = UUID.randomUUID();
        UUID termId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();

        Class schoolClass = new Class();
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

        when(classTermRepository.findBySchoolClassIdAndTermId(classId, termId)).thenReturn(Optional.of(classTerm));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(subject));
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(assignmentRepository.save(any(ClassSubjectAssignment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClassSubjectAssignment assignment = classManagementService.assignTeacher(
                classId, termId, subjectId, teacherId, BigDecimal.ONE);

        assertThat(assignment.getSchoolClass()).isEqualTo(schoolClass);
        assertThat(assignment.getTerm()).isEqualTo(term);
        assertThat(assignment.getSubject()).isEqualTo(subject);
        assertThat(assignment.getTeacher()).isEqualTo(teacher);
        assertThat(assignment.getWeighting()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void assignTeacher_throwsWhenClassTermNotFound() {
        UUID classId = UUID.randomUUID();
        UUID termId = UUID.randomUUID();

        when(classTermRepository.findBySchoolClassIdAndTermId(classId, termId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> classManagementService.assignTeacher(classId, termId, UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ONE))
                .isInstanceOf(EntityNotFoundException.class);

        verify(subjectRepository, never()).findById(any());
        verify(teacherRepository, never()).findById(any());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void assignTeacher_throwsWhenSubjectNotFound() {
        UUID classId = UUID.randomUUID();
        UUID termId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        ClassTerm classTerm = new ClassTerm();
        classTerm.setSchoolClass(new Class());
        classTerm.setTerm(new Term());

        when(classTermRepository.findBySchoolClassIdAndTermId(classId, termId)).thenReturn(Optional.of(classTerm));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> classManagementService.assignTeacher(classId, termId, subjectId, UUID.randomUUID(), BigDecimal.ONE))
                .isInstanceOf(EntityNotFoundException.class);

        verify(teacherRepository, never()).findById(any());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void assignTeacher_throwsWhenTeacherNotFound() {
        UUID classId = UUID.randomUUID();
        UUID termId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();

        ClassTerm classTerm = new ClassTerm();
        classTerm.setSchoolClass(new Class());
        classTerm.setTerm(new Term());

        Subject subject = new Subject();

        when(classTermRepository.findBySchoolClassIdAndTermId(classId, termId)).thenReturn(Optional.of(classTerm));
        when(subjectRepository.findById(any())).thenReturn(Optional.of(subject));
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> classManagementService.assignTeacher(classId, termId, UUID.randomUUID(), teacherId, BigDecimal.ONE))
                .isInstanceOf(EntityNotFoundException.class);

        verify(assignmentRepository, never()).save(any());
    }
}
