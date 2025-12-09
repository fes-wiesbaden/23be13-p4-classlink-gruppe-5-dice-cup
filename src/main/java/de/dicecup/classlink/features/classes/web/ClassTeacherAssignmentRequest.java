package de.dicecup.classlink.features.classes.web;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ClassTeacherAssignmentRequest(
        @NotNull UUID subjectId,
        @NotNull UUID teacherId,
        BigDecimal weighting
) {
}
