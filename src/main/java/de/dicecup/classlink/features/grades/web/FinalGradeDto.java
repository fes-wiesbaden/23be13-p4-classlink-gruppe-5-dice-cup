package de.dicecup.classlink.features.grades.web;

import de.dicecup.classlink.features.grades.FinalGrade;

import java.math.BigDecimal;
import java.util.UUID;

public record FinalGradeDto(
        UUID id,
        UUID classFinalGradeAssignmentId,
        UUID studentId,
        BigDecimal gradeValue,
        UUID changedBy
) {
    public static FinalGradeDto from(FinalGrade grade) {
        return new FinalGradeDto(
                grade.getId(),
                grade.getClassFinalGradeAssignment().getId(),
                grade.getStudent().getId(),
                grade.getGradeValue(),
                grade.getChangedBy().getId()
        );
    }
}
