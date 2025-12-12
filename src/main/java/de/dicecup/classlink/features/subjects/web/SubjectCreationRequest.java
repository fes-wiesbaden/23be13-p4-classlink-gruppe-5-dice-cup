package de.dicecup.classlink.features.subjects.web;

import jakarta.validation.constraints.NotBlank;

public record SubjectCreationRequest(
        @NotBlank String name,
        @NotBlank String description
) {
}
