package de.dicecup.classlink.features.projects.web;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record ProjectRequestDto(
        @NotBlank String name,
        String description,
        UUID responsibleTeacherId
) {
}
