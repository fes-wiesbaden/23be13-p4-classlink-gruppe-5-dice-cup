package de.dicecup.classlink.features.grades;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
// Author: Marcel Plenert
public interface GradeRepository extends JpaRepository<Grade, UUID> {
    List<Grade> findByStudentId(UUID studentId);
    List<Grade> findBySubjectAssignmentId(UUID assignmentId);
    Optional<Grade> findByStudentIdAndSubjectAssignmentId(UUID studentId, UUID assignmentId);
    List<Grade> findByStudentIdAndSubjectAssignmentIdIn(UUID studentId, List<UUID> assignmentId);
}
