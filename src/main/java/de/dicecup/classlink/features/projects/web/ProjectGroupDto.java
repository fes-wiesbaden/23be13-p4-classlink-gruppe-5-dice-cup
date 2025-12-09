package de.dicecup.classlink.features.projects.web;

import de.dicecup.classlink.features.projects.ProjectGroup;

import java.util.UUID;

public record ProjectGroupDto(
        UUID id,
        int groupNumber,
        UUID projectId,
        UUID supervisingTeacherId
) {
    public static ProjectGroupDto from(ProjectGroup group) {
        return new ProjectGroupDto(
                group.getId(),
                group.getGroupNumber(),
                group.getProject() != null ? group.getProject().getId() : null,
                group.getSupervisingTeacher() != null ? group.getSupervisingTeacher().getId() : null
        );
    }
}
