package de.dicecup.classlink.features.grades.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record FinalGradeChangeRequest(
        @NotBlank UUID parentAssignmentId,
        @NotBlank UUID studentId,
        @NotNull BigDecimal gradeValue,
        @NotBlank UUID requestingTeacherId
        ) {
}
