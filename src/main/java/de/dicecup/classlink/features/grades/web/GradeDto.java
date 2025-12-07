package de.dicecup.classlink.features.grades.web;

import de.dicecup.classlink.features.grades.Grade;

import java.math.BigDecimal;
import java.util.UUID;

public record GradeDto(
        UUID id,
        UUID classSubjectAssignmentId,
        UUID studentId,
        BigDecimal gradeValue,
        UUID changedBy
) {
    public static GradeDto from(Grade grade) {
        return new GradeDto(
                grade.getId(),
                grade.getClassSubjectAssignment().getId(),
                grade.getStudent().getId(),
                grade.getGradeValue(),
                grade.getChangedBy().getId()
        );
    }
}
