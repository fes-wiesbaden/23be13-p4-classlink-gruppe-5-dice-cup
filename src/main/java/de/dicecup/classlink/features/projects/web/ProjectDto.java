package de.dicecup.classlink.features.projects.web;

import de.dicecup.classlink.features.projects.Project;

import java.util.UUID;

public record ProjectDto(
        UUID id,
        String name,
        String description,
        boolean active,
        UUID classId,
        UUID termId
) {
    public static ProjectDto from(Project project) {
        return new ProjectDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.isActive(),
                project.getSchoolClass() != null ? project.getSchoolClass().getId() : null,
                project.getTerm() != null ? project.getTerm().getId() : null
        );
    }
}
