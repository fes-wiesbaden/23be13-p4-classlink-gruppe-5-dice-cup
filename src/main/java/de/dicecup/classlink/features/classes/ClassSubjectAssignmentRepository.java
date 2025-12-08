package de.dicecup.classlink.features.classes;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClassSubjectAssignmentRepository extends JpaRepository<ClassSubjectAssignment, UUID> {
    List<ClassSubjectAssignment> findBySchoolClassIdAndTermId(UUID classId, UUID termId);

    List<ClassSubjectAssignment> findBySchoolClassId(UUID classId);

    List<ClassSubjectAssignment> findByTeacherId(UUID teacherId);
}
