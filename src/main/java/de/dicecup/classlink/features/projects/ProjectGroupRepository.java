package de.dicecup.classlink.features.projects;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectGroupRepository extends JpaRepository<ProjectGroup, UUID> {
    List<ProjectGroup> findByProjectId(UUID projectId);
}
