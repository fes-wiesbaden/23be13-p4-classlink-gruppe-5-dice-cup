package de.dicecup.classlink.features.grades;

import de.dicecup.classlink.features.classes.ClassTerm;
import de.dicecup.classlink.features.classes.ClassTermRepository;
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
public class AssignmentManagementService {

    private final ClassTermRepository classTermRepository;
    private final SubjectAssignmentRepository assignmentRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;

    @Transactional
    public SubjectAssignment assignTeacher(UUID classId,
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

        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setSchoolClass(classTerm.getSchoolClass());
        assignment.setTerm(classTerm.getTerm());
        assignment.setSubject(subject);
        assignment.setTeacher(teacher);
        assignment.setWeighting(weighting);
        return assignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public List<SubjectAssignment> listAssignments(UUID classId, UUID termId) {
        return assignmentRepository.findBySchoolClassIdAndTermId(classId, termId);
    }
}
