package de.dicecup.classlink.features.projects;

import de.dicecup.classlink.features.classes.SchoolClass;
import de.dicecup.classlink.features.classes.ClassTerm;
import de.dicecup.classlink.features.classes.ClassTermRepository;
import de.dicecup.classlink.features.classes.ClassTermStatus;
import de.dicecup.classlink.features.terms.Term;
import de.dicecup.classlink.features.terms.TermRepository;
import de.dicecup.classlink.features.users.domain.roles.StudentRepository;
import de.dicecup.classlink.features.users.domain.roles.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TermRepository termRepository;
    @Mock
    private ClassTermRepository classTermRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void createProject_throwsWhenClassTermMissing() {
        UUID classId = UUID.randomUUID();
        UUID termId = UUID.randomUUID();
        var request = new ProjectService.ProjectRequest("Project", "Desc");

        when(classTermRepository.findBySchoolClassIdAndTermId(classId, termId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.createProject(classId, termId, request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(projectRepository, never()).save(any());
    }

    @Test
    void createProject_throwsWhenClassTermInactive() {
        UUID classId = UUID.randomUUID();
        UUID termId = UUID.randomUUID();
        var request = new ProjectService.ProjectRequest("Project", "Desc");

        ClassTerm classTerm = new ClassTerm();
        classTerm.setStatus(ClassTermStatus.INACTIVE);
        when(classTermRepository.findBySchoolClassIdAndTermId(classId, termId)).thenReturn(Optional.of(classTerm));

        assertThatThrownBy(() -> projectService.createProject(classId, termId, request))
                .isInstanceOf(IllegalStateException.class);

        verify(projectRepository, never()).save(any());
    }

    @Test
    void createProject_succeedsWhenClassTermActive() {
        UUID classId = UUID.randomUUID();
        UUID termId = UUID.randomUUID();
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(classId);
        Term term = new Term();
        term.setId(termId);
        ClassTerm classTerm = new ClassTerm();
        classTerm.setSchoolClass(schoolClass);
        classTerm.setTerm(term);
        classTerm.setStatus(ClassTermStatus.ACTIVE);
        var request = new ProjectService.ProjectRequest("Project", "Desc");

        when(classTermRepository.findBySchoolClassIdAndTermId(classId, termId)).thenReturn(Optional.of(classTerm));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project project = projectService.createProject(classId, termId, request);

        assertThat(project.getSchoolClass()).isEqualTo(schoolClass);
        assertThat(project.getTerm()).isEqualTo(term);
        assertThat(project.getName()).isEqualTo("Project");
        assertThat(project.getDescription()).isEqualTo("Desc");
        assertThat(project.isActive()).isTrue();
    }

    @Test
    void archiveProject_setsActiveFalse() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        project.setActive(true);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        projectService.archiveProject(projectId);

        assertThat(project.isActive()).isFalse();
        verify(projectRepository).findById(projectId);
    }

    @Test
    void reassignTerm_setsNewTerm() {
        UUID projectId = UUID.randomUUID();
        UUID newTermId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        Term newTerm = new Term();
        newTerm.setId(newTermId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(termRepository.findById(newTermId)).thenReturn(Optional.of(newTerm));

        Project result = projectService.reassignTerm(projectId, newTermId);

        assertThat(result.getTerm()).isEqualTo(newTerm);
        verify(projectRepository).findById(projectId);
        verify(termRepository).findById(newTermId);
    }
}
