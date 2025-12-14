package de.dicecup.classlink.features.grades.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// Author: Marcel Plenert
// DTO to request final grade calculation
public record FinalGradeCalculationRequest(
        @NotNull UUID assignmentId
) {
}
