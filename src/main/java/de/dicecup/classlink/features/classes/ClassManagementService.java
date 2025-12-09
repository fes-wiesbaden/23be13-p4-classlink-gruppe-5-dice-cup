package de.dicecup.classlink.features.classes;

import de.dicecup.classlink.features.subjects.Subject;
import de.dicecup.classlink.features.subjects.SubjectRepository;
import de.dicecup.classlink.features.users.domain.roles.Teacher;
import de.dicecup.classlink.features.users.domain.roles.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassManagementService {

    private final ClassTermRepository classTermRepository;
    private final ClassSubjectAssignmentRepository assignmentRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;

    @Transactional
    public ClassSubjectAssignment assignTeacher(UUID classId,
                                                UUID termId,
                                                UUID subjectId,
                                                UUID teacherId,
                                                BigDecimal weighting) {
        ClassTerm classTerm = classTermRepository.findBySchoolClassIdAndTermId(classId, termId)
                .orElseThrow(() -> new EntityNotFoundException("Class term not found"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found"));
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

        ClassSubjectAssignment assignment = new ClassSubjectAssignment();
        assignment.setSchoolClass(classTerm.getSchoolClass());
        assignment.setTerm(classTerm.getTerm());
        assignment.setSubject(subject);
        assignment.setTeacher(teacher);
        assignment.setWeighting(weighting);
        return assignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public List<ClassSubjectAssignment> listAssignments(UUID classId, UUID termId) {
        return assignmentRepository.findBySchoolClassIdAndTermId(classId, termId);
    }
}
