package de.dicecup.classlink.features.grades.web;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record FinalGradeCalculationRequest(
        UUID studentId,
        @NotBlank UUID teacherId
) {
}
