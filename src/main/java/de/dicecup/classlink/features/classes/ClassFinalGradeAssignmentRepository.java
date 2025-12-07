package de.dicecup.classlink.features.classes;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClassFinalGradeAssignmentRepository extends JpaRepository<ClassFinalGradeAssignment, UUID> {
    List<ClassFinalGradeAssignment> findBySchoolClassIdAndTermId(UUID classId, UUID termId);
}
