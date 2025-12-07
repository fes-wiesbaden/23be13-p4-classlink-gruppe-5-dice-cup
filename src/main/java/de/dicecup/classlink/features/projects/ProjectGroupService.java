package de.dicecup.classlink.features.projects;

import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.features.users.domain.roles.StudentRepository;
import de.dicecup.classlink.features.users.domain.roles.Teacher;
import de.dicecup.classlink.features.users.domain.roles.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectGroupService {

    private final ProjectRepository projectRepository;
    private final ProjectGroupRepository projectGroupRepository;
    private final ProjectGroupMemberRepository memberRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public ProjectGroup createGroup(UUID projectId, int groupNumber) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        ProjectGroup group = new ProjectGroup();
        group.setProject(project);
        group.setGroupNumber(groupNumber);

        return projectGroupRepository.save(group);
    }

    @Transactional
    public void assignStudents(UUID projectGroupId, List<MemberAssignment> assignments) {
        ProjectGroup group = projectGroupRepository.findById(projectGroupId)
                .orElseThrow(() -> new EntityNotFoundException("Project group not found"));
        UUID classId = group.getProject().getSchoolClass().getId();
        if (assignments == null) {
            return;
        }
        for (MemberAssignment assignment : assignments) {
            Student student = studentRepository.findById(assignment.studentId())
                    .orElseThrow(() -> new EntityNotFoundException("Student not found"));
            if (!classId.equals(student.getClazz().getId())) {
                throw new IllegalStateException("Student does not belong to project class");
            }
            ProjectGroupMember member = new ProjectGroupMember();
            member.setProjectGroup(group);
            member.setStudent(student);
            member.setRole(assignment.role());
            memberRepository.save(member);
        }
    }

    @Transactional
    public void removeStudent(UUID projectGroupId, UUID studentId) {
        memberRepository.deleteByProjectGroupIdAndStudentId(projectGroupId, studentId);
    }

    @Transactional(readOnly = true)
    public List<ProjectGroup> listGroups(UUID projectId) {
        return projectGroupRepository.findByProjectId(projectId);
    }

    public record MemberAssignment(UUID studentId, MemberRole role) {
    }
}
