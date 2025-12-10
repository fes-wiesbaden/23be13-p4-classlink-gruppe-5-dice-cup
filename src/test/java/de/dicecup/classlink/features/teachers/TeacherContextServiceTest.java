package de.dicecup.classlink.features.teachers;

import de.dicecup.classlink.features.assessments.AuthHelper;
import de.dicecup.classlink.features.classes.SchoolClass;
import de.dicecup.classlink.features.grades.SubjectAssignment;
import de.dicecup.classlink.features.grades.SubjectAssignmentRepository;
import de.dicecup.classlink.features.projects.Project;
import de.dicecup.classlink.features.projects.ProjectGroup;
import de.dicecup.classlink.features.projects.ProjectGroupMemberRepository;
import de.dicecup.classlink.features.projects.ProjectGroupRepository;
import de.dicecup.classlink.features.projects.ProjectRepository;
import de.dicecup.classlink.features.terms.Term;
import de.dicecup.classlink.features.terms.TermRepository;
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
class TeacherContextServiceTest {

    @Mock
    private AuthHelper authHelper;
    @Mock
    private SubjectAssignmentRepository assignmentRepository;
    @Mock
    private TermRepository termRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectGroupRepository projectGroupRepository;
    @Mock
    private ProjectGroupMemberRepository memberRepository;

    @InjectMocks
    private TeacherContextService service;

    @Test
    void loadContext_returnsAssignedClasses() {
        UUID teacherId = UUID.randomUUID();
        when(authHelper.requireTeacherId()).thenReturn(teacherId);

        SchoolClass clazz = new SchoolClass();
        clazz.setId(UUID.randomUUID());
        clazz.setName("1A");
        Term term = new Term();
        term.setId(UUID.randomUUID());
        term.setName("Term 1");
        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setSchoolClass(clazz);
        assignment.setTerm(term);
        when(assignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of(assignment));
        when(termRepository.findById(term.getId())).thenReturn(java.util.Optional.of(term));

        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setName("Project X");
        project.setSchoolClass(clazz);
        project.setTerm(term);
        when(projectRepository.findBySchoolClassIdAndTermId(clazz.getId(), term.getId())).thenReturn(List.of(project));
        ProjectGroup group = new ProjectGroup();
        group.setId(UUID.randomUUID());
        group.setProject(project);
        when(projectGroupRepository.findByProjectId(project.getId())).thenReturn(List.of(group));
        when(memberRepository.countByProjectGroupId(group.getId())).thenReturn(2L);

        TeacherContextDto dto = service.loadContext();

        assertThat(dto.classes()).hasSize(1);
        assertThat(dto.classes().getFirst().terms()).hasSize(1);
        assertThat(dto.classes().getFirst().terms().getFirst().projectGroups()).hasSize(1);
    }

    @Test
    void loadContext_returnsEmptyWhenNoAssignments() {
        UUID teacherId = UUID.randomUUID();
        when(authHelper.requireTeacherId()).thenReturn(teacherId);
        when(assignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of());

        TeacherContextDto dto = service.loadContext();

        assertThat(dto.classes()).isEmpty();
    }

    @Test
    void loadContext_filtersOutNullAssignments() {
        UUID teacherId = UUID.randomUUID();
        when(authHelper.requireTeacherId()).thenReturn(teacherId);

        SchoolClass clazz = new SchoolClass();
        clazz.setId(UUID.randomUUID());
        clazz.setName("1A");
        Term term = new Term();
        term.setId(UUID.randomUUID());
        term.setName("Term 1");
        SubjectAssignment validAssignment = new SubjectAssignment();
        validAssignment.setSchoolClass(clazz);
        validAssignment.setTerm(term);
        SubjectAssignment nullAssignment = new SubjectAssignment(); // missing class/term should be ignored
        when(assignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of(validAssignment, nullAssignment));
        when(termRepository.findById(term.getId())).thenReturn(java.util.Optional.of(term));

        TeacherContextDto dto = service.loadContext();

        assertThat(dto.classes()).hasSize(1);
        assertThat(dto.classes().getFirst().classId()).isEqualTo(clazz.getId());
    }

    @Test
    void loadContext_throwsWhenNotTeacher() {
        when(authHelper.requireTeacherId()).thenThrow(new AccessDeniedException("not a teacher"));

        assertThatThrownBy(() -> service.loadContext())
                .isInstanceOf(AccessDeniedException.class);
    }
}
