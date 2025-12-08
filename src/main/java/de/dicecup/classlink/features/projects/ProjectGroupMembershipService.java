package de.dicecup.classlink.features.projects;

import de.dicecup.classlink.features.assessments.AuthHelper;
import de.dicecup.classlink.features.classes.ClassSubjectAssignmentRepository;
import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.features.users.domain.roles.StudentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProjectGroupMembershipService {

    private final AuthHelper authHelper;
    private final ProjectGroupRepository projectGroupRepository;
    private final ProjectGroupMemberRepository memberRepository;
    private final StudentRepository studentRepository;
    private final ClassSubjectAssignmentRepository assignmentRepository;

    public ProjectGroupMembershipService(AuthHelper authHelper,
                                         ProjectGroupRepository projectGroupRepository,
                                         ProjectGroupMemberRepository memberRepository,
                                         StudentRepository studentRepository,
                                         ClassSubjectAssignmentRepository assignmentRepository) {
        this.authHelper = authHelper;
        this.projectGroupRepository = projectGroupRepository;
        this.memberRepository = memberRepository;
        this.studentRepository = studentRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional
    public void addMember(UUID projectGroupId, UUID studentId) {
        UUID teacherId = authHelper.requireTeacherId();
        ProjectGroup group = loadGroup(projectGroupId);
        ensureTeacherAssigned(teacherId, group);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Student not found"));
        UUID classId = group.getProject().getSchoolClass().getId();
        if (!classId.equals(student.getClazz().getId())) {
            throw new AccessDeniedException("Student not part of class");
        }

        if (memberRepository.existsByProjectGroupIdAndStudentId(projectGroupId, studentId)) {
            return; // idempotent
        }

        ProjectGroupMember member = new ProjectGroupMember();
        member.setProjectGroup(group);
        member.setStudent(student);
        memberRepository.save(member);
    }

    @Transactional
    public void removeMember(UUID projectGroupId, UUID studentId) {
        UUID teacherId = authHelper.requireTeacherId();
        ProjectGroup group = loadGroup(projectGroupId);
        ensureTeacherAssigned(teacherId, group);

        if (!memberRepository.existsByProjectGroupIdAndStudentId(projectGroupId, studentId)) {
            return; // idempotent removal
        }

        memberRepository.deleteByProjectGroupIdAndStudentId(projectGroupId, studentId);
    }

    private ProjectGroup loadGroup(UUID projectGroupId) {
        return projectGroupRepository.findById(projectGroupId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Project group not found"));
    }

    private void ensureTeacherAssigned(UUID teacherId, ProjectGroup group) {
        UUID classId = group.getProject().getSchoolClass().getId();
        UUID termId = group.getProject().getTerm().getId();
        boolean allowed = assignmentRepository.findByTeacherId(teacherId).stream()
                .anyMatch(a -> a.getSchoolClass() != null && a.getTerm() != null
                        && classId.equals(a.getSchoolClass().getId())
                        && termId.equals(a.getTerm().getId()));
        if (!allowed) {
            throw new AccessDeniedException("Teacher not assigned to class/term");
        }
    }
}
