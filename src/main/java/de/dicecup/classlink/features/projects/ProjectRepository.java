package de.dicecup.classlink.features.projects;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findBySchoolClassIdAndTermId(UUID classId, UUID termId);

    List<Project> findByTermId(UUID termId);
}
