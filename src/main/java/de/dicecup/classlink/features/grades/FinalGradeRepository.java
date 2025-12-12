package de.dicecup.classlink.features.grades;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// A
public interface FinalGradeRepository extends JpaRepository<FinalGrade, UUID> {
    List<FinalGrade> findByStudentId(UUID studentId);
    List<FinalGrade> findByClassFinalGradeAssignmentId(UUID assignmentId);
    List<FinalGrade> findByStudentIdAndClassFinalGradeAssignmentId(UUID studentId, UUID assignmentId);
}
