package de.dicecup.classlink.features.projects.web;

import de.dicecup.classlink.features.projects.ProjectGroupService;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignMembersRequest(
        @NotNull List<ProjectGroupService.MemberAssignment> members
) {
}
