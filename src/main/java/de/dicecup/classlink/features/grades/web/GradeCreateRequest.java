package de.dicecup.classlink.features.grades.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

// Author: Marcel Plenert+
// DTO for Grade Creation
public record GradeCreateRequest(
        @NotBlank UUID parentAssignmentId,
        @NotBlank UUID studentId,
        @NotNull BigDecimal gradeValue,
        @NotBlank UUID requestingTeacherId
        ) {
}
