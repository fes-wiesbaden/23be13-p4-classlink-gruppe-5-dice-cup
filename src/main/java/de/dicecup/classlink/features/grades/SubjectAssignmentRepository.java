package de.dicecup.classlink.features.grades;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Author: Marcel Plenert
public interface SubjectAssignmentRepository extends JpaRepository<SubjectAssignment, UUID> {
    List<SubjectAssignment> findBySchoolClassIdAndTermId(UUID classId, UUID termId);
    Optional<SubjectAssignment> findBySubjectId(UUID subjectId);

    List<SubjectAssignment> findByTeacherId(UUID teacherId);
}
