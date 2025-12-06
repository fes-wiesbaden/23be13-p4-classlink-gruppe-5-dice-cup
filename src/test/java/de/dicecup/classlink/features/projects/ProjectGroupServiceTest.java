package de.dicecup.classlink.features.projects;

import de.dicecup.classlink.features.classes.Class;
import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.features.users.domain.roles.StudentRepository;
import de.dicecup.classlink.features.users.domain.roles.Teacher;
import de.dicecup.classlink.features.users.domain.roles.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectGroupServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectGroupRepository projectGroupRepository;
    @Mock
    private ProjectGroupMemberRepository memberRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private ProjectGroupService projectGroupService;

    @Test
    void createGroup_setsProjectAndTeacher() {
        UUID projectId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        Teacher teacher = new Teacher();
        teacher.setId(teacherId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(projectGroupRepository.save(any(ProjectGroup.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProjectGroup group = projectGroupService.createGroup(projectId, 1, teacherId);

        assertThat(group.getProject()).isEqualTo(project);
        assertThat(group.getGroupNumber()).isEqualTo(1);
        assertThat(group.getSupervisingTeacher()).isEqualTo(teacher);
    }

    @Test
    void assignStudents_throwsWhenStudentOutsideProjectClass() {
        UUID groupId = UUID.randomUUID();
        ProjectGroup group = new ProjectGroup();
        Project project = new Project();
        Class classA = new Class();
        classA.setId(UUID.randomUUID());
        Class classB = new Class();
        classB.setId(UUID.randomUUID());
        project.setSchoolClass(classA);
        group.setProject(project);

        UUID studentId = UUID.randomUUID();
        Student student = new Student();
        student.setId(studentId);
        student.setClazz(classB);

        when(projectGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        ProjectGroupService.MemberAssignment assignment = new ProjectGroupService.MemberAssignment(studentId, MemberRole.MEMBER);

        assertThatThrownBy(() -> projectGroupService.assignStudents(groupId, List.of(assignment)))
                .isInstanceOf(IllegalStateException.class);

        verify(memberRepository, never()).save(any());
    }

    @Test
    void assignStudents_savesMembersForValidClass() {
        UUID groupId = UUID.randomUUID();
        ProjectGroup group = new ProjectGroup();
        Project project = new Project();
        Class classA = new Class();
        classA.setId(UUID.randomUUID());
        project.setSchoolClass(classA);
        group.setProject(project);

        UUID studentId1 = UUID.randomUUID();
        UUID studentId2 = UUID.randomUUID();
        Student student1 = new Student();
        student1.setId(studentId1);
        student1.setClazz(classA);
        Student student2 = new Student();
        student2.setId(studentId2);
        student2.setClazz(classA);

        when(projectGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(studentRepository.findById(studentId1)).thenReturn(Optional.of(student1));
        when(studentRepository.findById(studentId2)).thenReturn(Optional.of(student2));

        ProjectGroupService.MemberAssignment assignment1 = new ProjectGroupService.MemberAssignment(studentId1, MemberRole.MEMBER);
        ProjectGroupService.MemberAssignment assignment2 = new ProjectGroupService.MemberAssignment(studentId2, MemberRole.LEADER);

        projectGroupService.assignStudents(groupId, List.of(assignment1, assignment2));

        verify(memberRepository, times(2)).save(any(ProjectGroupMember.class));
    }

    @Test
    void assignStudents_throwsWhenGroupNotFound() {
        UUID groupId = UUID.randomUUID();
        when(projectGroupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectGroupService.assignStudents(groupId, List.of()))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
