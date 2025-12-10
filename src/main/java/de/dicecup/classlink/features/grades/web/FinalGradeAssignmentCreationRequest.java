package de.dicecup.classlink.features.grades.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FinalGradeAssignmentCreationRequest(
        @NotBlank String name,
        @NotNull UUID schoolClassId,
        @NotNull UUID termId,
        @NotNull UUID teacherId,
        @NotNull UUID subjectId
)  {
}
