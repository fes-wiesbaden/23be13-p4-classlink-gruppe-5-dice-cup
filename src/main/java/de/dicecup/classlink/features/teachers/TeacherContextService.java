package de.dicecup.classlink.features.teachers;

import de.dicecup.classlink.features.assessments.AuthHelper;
import de.dicecup.classlink.features.classes.ClassSubjectAssignment;
import de.dicecup.classlink.features.classes.ClassSubjectAssignmentRepository;
import de.dicecup.classlink.features.classes.ClassTermRepository;
import de.dicecup.classlink.features.projects.ProjectGroupRepository;
import de.dicecup.classlink.features.projects.ProjectRepository;
import de.dicecup.classlink.features.projects.ProjectGroupMemberRepository;
import de.dicecup.classlink.features.terms.Term;
import de.dicecup.classlink.features.terms.TermRepository;
import de.dicecup.classlink.features.terms.TermStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TeacherContextService {

    private final AuthHelper authHelper;
    private final ClassSubjectAssignmentRepository assignmentRepository;
    private final TermRepository termRepository;
    private final ProjectRepository projectRepository;
    private final ProjectGroupRepository projectGroupRepository;
    private final ProjectGroupMemberRepository memberRepository;

    public TeacherContextService(AuthHelper authHelper,
                                 ClassSubjectAssignmentRepository assignmentRepository,
                                 TermRepository termRepository,
                                 ProjectRepository projectRepository,
                                 ProjectGroupRepository projectGroupRepository,
                                 ProjectGroupMemberRepository memberRepository) {
        this.authHelper = authHelper;
        this.assignmentRepository = assignmentRepository;
        this.termRepository = termRepository;
        this.projectRepository = projectRepository;
        this.projectGroupRepository = projectGroupRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public TeacherContextDto loadContext() {
        UUID teacherId = authHelper.requireTeacherId();
        List<ClassSubjectAssignment> assignments = assignmentRepository.findByTeacherId(teacherId);
        if (assignments.isEmpty()) {
            return new TeacherContextDto(teacherId, List.of());
        }

        Map<UUID, List<ClassSubjectAssignment>> byClass = assignments.stream()
                .filter(a -> a.getSchoolClass() != null)
                .collect(Collectors.groupingBy(a -> a.getSchoolClass().getId()));

        List<TeacherClassContextDto> classes = new ArrayList<>();
        for (Map.Entry<UUID, List<ClassSubjectAssignment>> entry : byClass.entrySet()) {
            UUID classId = entry.getKey();
            String className = entry.getValue().stream()
                    .map(a -> a.getSchoolClass().getName())
                    .filter(n -> n != null && !n.isBlank())
                    .findFirst()
                    .orElse(null);

            Map<UUID, String> termNames = entry.getValue().stream()
                    .filter(a -> a.getTerm() != null)
                    .collect(Collectors.toMap(a -> a.getTerm().getId(), a -> a.getTerm().getName(), (a, b) -> a));
            List<TeacherTermContextDto> terms = termNames.entrySet().stream()
                    .map(tEntry -> toTermContext(classId, tEntry.getKey(), tEntry.getValue()))
                    .toList();

            classes.add(new TeacherClassContextDto(classId, className, terms));
        }

        return new TeacherContextDto(teacherId, classes);
    }

    private TeacherTermContextDto toTermContext(UUID classId, UUID termId, String termName) {
        Term term = termRepository.findById(termId).orElse(null);
        boolean isCurrent = false;
        if (term != null && term.getStartDate() != null && term.getEndDate() != null) {
            LocalDate today = LocalDate.now();
            isCurrent = (today.isEqual(term.getStartDate()) || today.isAfter(term.getStartDate()))
                    && (today.isEqual(term.getEndDate()) || today.isBefore(term.getEndDate()))
                    && term.getStatus() == TermStatus.OPEN;
        }

        List<de.dicecup.classlink.features.projects.Project> projects = projectRepository.findBySchoolClassIdAndTermId(classId, termId);
        List<TeacherProjectGroupContextDto> groups = projects.stream()
                .flatMap(p -> projectGroupRepository.findByProjectId(p.getId()).stream()
                        .map(g -> new TeacherProjectGroupContextDto(
                                g.getId(),
                                p.getName(),
                                Math.toIntExact(memberRepository.countByProjectGroupId(g.getId()))
                        )))
                .toList();

        return new TeacherTermContextDto(termId, termName, isCurrent, groups);
    }
}
