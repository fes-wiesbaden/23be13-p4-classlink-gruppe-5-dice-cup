package de.dicecup.classlink.features.projects;

import de.dicecup.classlink.features.assessments.AuthHelper;
import de.dicecup.classlink.features.classes.SchoolClass;
import de.dicecup.classlink.features.grades.SubjectAssignment;
import de.dicecup.classlink.features.grades.SubjectAssignmentRepository;
import de.dicecup.classlink.features.terms.Term;
import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.features.users.domain.roles.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectGroupMembershipServiceTest {

    @Mock
    private AuthHelper authHelper;
    @Mock
    private ProjectGroupRepository projectGroupRepository;
    @Mock
    private ProjectGroupMemberRepository memberRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private SubjectAssignmentRepository assignmentRepository;

    @InjectMocks
    private ProjectGroupMembershipService service;

    @Test
    void addMember_deniesWhenTeacherNotAssigned() {
        UUID groupId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        when(authHelper.requireTeacherId()).thenReturn(teacherId);
        when(projectGroupRepository.findById(groupId)).thenReturn(Optional.of(buildGroup()));
        when(assignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of());

        assertThatThrownBy(() -> service.addMember(groupId, UUID.randomUUID()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void addMember_assignsStudent() {
        UUID groupId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        ProjectGroup group = buildGroup();
        Student student = buildStudent(group.getProject().getSchoolClass());
        when(authHelper.requireTeacherId()).thenReturn(teacherId);
        when(projectGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setSchoolClass(group.getProject().getSchoolClass());
        assignment.setTerm(group.getProject().getTerm());
        when(assignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of(assignment));
        when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(memberRepository.existsByProjectGroupIdAndStudentId(groupId, student.getId())).thenReturn(false);

        service.addMember(groupId, student.getId());

        verify(memberRepository).save(any(ProjectGroupMember.class));
    }

    @Test
    void addMember_deniesStudentFromOtherClass() {
        UUID groupId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        ProjectGroup group = buildGroup();
        SchoolClass otherClass = new SchoolClass();
        otherClass.setId(UUID.randomUUID());
        Student student = buildStudent(otherClass);
        when(authHelper.requireTeacherId()).thenReturn(teacherId);
        when(projectGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setSchoolClass(group.getProject().getSchoolClass());
        assignment.setTerm(group.getProject().getTerm());
        when(assignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of(assignment));
        when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> service.addMember(groupId, student.getId()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void addMember_isIdempotentWhenAlreadyAssigned() {
        UUID groupId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        ProjectGroup group = buildGroup();
        Student student = buildStudent(group.getProject().getSchoolClass());
        when(authHelper.requireTeacherId()).thenReturn(teacherId);
        when(projectGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setSchoolClass(group.getProject().getSchoolClass());
        assignment.setTerm(group.getProject().getTerm());
        when(assignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of(assignment));
        when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(memberRepository.existsByProjectGroupIdAndStudentId(groupId, student.getId())).thenReturn(true);

        service.addMember(groupId, student.getId());

        verify(memberRepository, never()).save(any());
    }

    @Test
    void removeMember_deniesWhenTeacherNotAssigned() {
        UUID groupId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        when(authHelper.requireTeacherId()).thenReturn(teacherId);
        when(projectGroupRepository.findById(groupId)).thenReturn(Optional.of(buildGroup()));
        when(assignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of());

        assertThatThrownBy(() -> service.removeMember(groupId, UUID.randomUUID()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void removeMember_isIdempotentWhenNotMember() {
        UUID groupId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        ProjectGroup group = buildGroup();
        when(authHelper.requireTeacherId()).thenReturn(teacherId);
        when(projectGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setSchoolClass(group.getProject().getSchoolClass());
        assignment.setTerm(group.getProject().getTerm());
        when(assignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of(assignment));
        when(memberRepository.existsByProjectGroupIdAndStudentId(any(), any())).thenReturn(false);

        service.removeMember(groupId, UUID.randomUUID());

        verify(memberRepository, never()).deleteByProjectGroupIdAndStudentId(any(), any());
    }

    @Test
    void removeMember_deletesWhenPresent() {
        UUID groupId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        ProjectGroup group = buildGroup();
        UUID studentId = UUID.randomUUID();
        when(authHelper.requireTeacherId()).thenReturn(teacherId);
        when(projectGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setSchoolClass(group.getProject().getSchoolClass());
        assignment.setTerm(group.getProject().getTerm());
        when(assignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of(assignment));
        when(memberRepository.existsByProjectGroupIdAndStudentId(groupId, studentId)).thenReturn(true);

        service.removeMember(groupId, studentId);

        verify(memberRepository).deleteByProjectGroupIdAndStudentId(groupId, studentId);
    }

    private ProjectGroup buildGroup() {
        SchoolClass clazz = new SchoolClass();
        clazz.setId(UUID.randomUUID());
        Term term = new Term();
        term.setId(UUID.randomUUID());
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setSchoolClass(clazz);
        project.setTerm(term);
        ProjectGroup group = new ProjectGroup();
        group.setProject(project);
        return group;
    }

    private Student buildStudent(SchoolClass clazz) {
        Student student = new Student();
        student.setId(UUID.randomUUID());
        student.setSchoolClass(clazz);
        return student;
    }
}
