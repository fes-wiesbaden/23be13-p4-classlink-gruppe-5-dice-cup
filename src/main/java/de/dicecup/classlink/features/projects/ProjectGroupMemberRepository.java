package de.dicecup.classlink.features.projects;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectGroupMemberRepository extends JpaRepository<ProjectGroupMember, UUID> {
    void deleteByProjectGroupIdAndStudentId(UUID projectGroupId, UUID studentId);

    boolean existsByProjectGroupProjectIdAndStudentId(UUID projectId, UUID studentId);

    boolean existsByProjectGroupIdAndStudentId(UUID projectGroupId, UUID studentId);

    List<ProjectGroupMember> findByProjectGroupId(UUID projectGroupId);

    long countByProjectGroupId(UUID projectGroupId);
}
