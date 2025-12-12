package de.dicecup.classlink.features.grades;

import de.dicecup.classlink.features.classes.SchoolClassRepository;
import de.dicecup.classlink.features.classes.SchoolClass;
import de.dicecup.classlink.features.subjects.Subject;
import de.dicecup.classlink.features.subjects.SubjectRepository;
import de.dicecup.classlink.features.terms.Term;
import de.dicecup.classlink.features.terms.TermRepository;
import de.dicecup.classlink.features.users.domain.roles.Teacher;
import de.dicecup.classlink.features.users.domain.roles.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// Author: Marcel Plenert
// Management Service for assignments and final assignments, handles more complex operations used by the Controller
@Service
@RequiredArgsConstructor
public class AssignmentManagementService {

    private final TermRepository termRepository;
    private final SubjectAssignmentRepository assignmentRepository;
    private final FinalGradeAssignmentRepository finalGradeAssignmentRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolClassRepository schoolClassRepository;

    @Transactional
    public SubjectAssignment createAndSaveAssignment(String name,
                                                     UUID classId,
                                                     UUID termId,
                                                     UUID subjectId,
                                                     UUID teacherId,
                                                     UUID finalGradeAssignmentId,
                                                     BigDecimal weighting) {
        SchoolClass schoolClass = schoolClassRepository
                .findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Class not found"));
        Term term = termRepository
                .findById(termId)
                .orElseThrow(() -> new EntityNotFoundException("term not found"));
        Subject subject = subjectRepository
                .findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found"));
        Teacher teacher = teacherRepository
                .findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));
        FinalGradeAssignment finalGradeAssignment = finalGradeAssignmentRepository
                .findById(finalGradeAssignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Final grade assignment not found"));

        SubjectAssignment assignment = new SubjectAssignment();

        assignment.setName(name);
        assignment.setSchoolClass(schoolClass);
        assignment.setTerm(term);
        assignment.setSubject(subject);
        assignment.setTeacher(teacher);
        assignment.setWeighting(weighting);
        finalGradeAssignment.getSubGradeAssignments().add(assignment);
        assignment.setFinalGradeAssignment(finalGradeAssignment);
        assignment.setFinalGradeAssignment(finalGradeAssignment);

        return assignmentRepository.save(assignment);
    }

    @Transactional
    public FinalGradeAssignment createAndSaveFinalAssignment(String name,
                                                          UUID classId,
                                                          UUID termId,
                                                          UUID subjectId,
                                                          UUID teacherId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new EntityNotFoundException("Class not found"));
        Term term = termRepository.findById(termId)
                .orElseThrow(() -> new EntityNotFoundException("term not found"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException("Subject not found"));
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

        FinalGradeAssignment assignment = new FinalGradeAssignment();
        assignment.setName(name);
        assignment.setSchoolClass(schoolClass);
        assignment.setTerm(term);
        assignment.setSubject(subject);
        assignment.setTeacher(teacher);

        return finalGradeAssignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public List<SubjectAssignment> listAssignments(UUID classId, UUID termId) {
        return assignmentRepository.findBySchoolClassIdAndTermId(classId, termId);
    }
}
