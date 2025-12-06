package de.dicecup.classlink.features.projects;

import de.dicecup.classlink.features.classes.ClassTerm;
import de.dicecup.classlink.features.classes.ClassTermRepository;
import de.dicecup.classlink.features.classes.ClassTermStatus;
import de.dicecup.classlink.features.terms.Term;
import de.dicecup.classlink.features.terms.TermRepository;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TermRepository termRepository;
    private final ClassTermRepository classTermRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public Project createProject(UUID classId, UUID termId, ProjectRequest request) {
        ClassTerm classTerm = classTermRepository.findBySchoolClassIdAndTermId(classId, termId)
                .orElseThrow(() -> new EntityNotFoundException("Class term not found"));
        if (classTerm.getStatus() == ClassTermStatus.INACTIVE) {
            throw new IllegalStateException("Class term is inactive");
        }
        Project project = new Project();
        project.setSchoolClass(classTerm.getSchoolClass());
        project.setTerm(classTerm.getTerm());
        project.setName(request.name());
        project.setDescription(request.description());
        project.setActive(true);
        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProject(UUID projectId, ProjectRequest request) {
        Project project = getById(projectId);
        project.setName(request.name());
        project.setDescription(request.description());
        return project;
    }

    @Transactional
    public void archiveProject(UUID projectId) {
        Project project = getById(projectId);
        project.setActive(false);
    }

    @Transactional(readOnly = true)
    public List<Project> listProjects(UUID classId, UUID termId) {
        return projectRepository.findBySchoolClassIdAndTermId(classId, termId);
    }

    @Transactional(readOnly = true)
    public List<Project> listProjectsForStudent(UUID studentId, UUID termId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        UUID classId = student.getClazz().getId();
        return projectRepository.findBySchoolClassIdAndTermId(classId, termId);
    }

    @Transactional(readOnly = true)
    public Project getById(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
    }

    @Transactional
    public Project reassignTerm(UUID projectId, UUID termId) {
        Project project = getById(projectId);
        Term term = termRepository.findById(termId)
                .orElseThrow(() -> new EntityNotFoundException("Term not found"));
        project.setTerm(term);
        return project;
    }

    public record ProjectRequest(String name, String description, UUID responsibleTeacherId) {
    }
}
