package de.dicecup.classlink.features.grades.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// Author: Marcel Plenert
// DTO for Final Grade Assignment Creation
public record FinalGradeAssignmentCreationRequest(
        @NotBlank String name,
        @NotNull UUID schoolClassId,
        @NotNull UUID termId,
        @NotNull UUID teacherId,
        @NotNull UUID subjectId
)  {
}
