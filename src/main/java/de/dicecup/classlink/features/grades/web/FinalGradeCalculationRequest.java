package de.dicecup.classlink.features.grades.web;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

// Author: Marcel Plenert
// DTO to request final grade calculation
public record FinalGradeCalculationRequest(
        UUID studentId,
        @NotBlank UUID teacherId
) {
}
