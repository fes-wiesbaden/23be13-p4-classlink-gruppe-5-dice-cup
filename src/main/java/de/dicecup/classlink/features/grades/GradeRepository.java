package de.dicecup.classlink.features.grades;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GradeRepository extends JpaRepository<Grade, UUID> {
    List<Grade> findByStudentId(UUID studentId);
    List<Grade> findByClassSubjectAssignmentId(UUID assignmentId);
    List<Grade> findByStudentIdAndClassSubjectAssignmentId(UUID studentId, UUID assignmentId);
}
