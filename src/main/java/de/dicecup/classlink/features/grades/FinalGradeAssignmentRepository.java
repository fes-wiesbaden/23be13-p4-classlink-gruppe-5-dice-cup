package de.dicecup.classlink.features.grades;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// Author: Marcel Plenert
public interface FinalGradeAssignmentRepository extends JpaRepository<FinalGradeAssignment, UUID> {
    List<FinalGradeAssignment> findBySchoolClassIdAndTermId(UUID classId, UUID termId);
}
