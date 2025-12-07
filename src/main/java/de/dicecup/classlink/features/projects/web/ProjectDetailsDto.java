package de.dicecup.classlink.features.projects.web;

import de.dicecup.classlink.features.projects.Project;

public record ProjectDetailsDto(
        ProjectDto project
) {
    public static ProjectDetailsDto from(Project project) {
        return new ProjectDetailsDto(ProjectDto.from(project));
    }
}
