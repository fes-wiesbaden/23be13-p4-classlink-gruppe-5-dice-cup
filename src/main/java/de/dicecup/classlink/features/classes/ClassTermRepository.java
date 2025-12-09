package de.dicecup.classlink.features.classes;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassTermRepository extends JpaRepository<ClassTerm, UUID> {
    Optional<ClassTerm> findBySchoolClassIdAndTermId(UUID classId, UUID termId);

    List<ClassTerm> findByTermId(UUID termId);

    List<ClassTerm> findBySchoolClassId(UUID classId);
}
