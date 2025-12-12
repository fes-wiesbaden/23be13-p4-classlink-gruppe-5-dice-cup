package de.dicecup.classlink.features.grades.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

// Author: Marcel Plenert+
// DTO for Grade Creation
public record GradeCreateRequest(
        @NotNull UUID parentAssignmentId,
        @NotNull UUID studentId,
        @NotNull BigDecimal gradeValue,
        @NotNull UUID requestingTeacherId
        ) {
}
