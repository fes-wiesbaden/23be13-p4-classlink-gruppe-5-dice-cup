package de.dicecup.classlink.features.projects.web;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateGroupRequest(
        @NotNull Integer groupNumber,
        UUID supervisingTeacherId
) {
}
