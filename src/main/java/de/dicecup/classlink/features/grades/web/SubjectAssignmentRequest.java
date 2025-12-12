package de.dicecup.classlink.features.grades.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record SubjectAssignmentRequest(
        @NotBlank String name,
        @NotNull UUID classId,
        @NotNull UUID termId,
        @NotNull UUID teacherId,
        @NotNull UUID subjectId,
        @NotNull UUID finalGradeAssignmentId,
        @NotNull BigDecimal weighting
) {

}
