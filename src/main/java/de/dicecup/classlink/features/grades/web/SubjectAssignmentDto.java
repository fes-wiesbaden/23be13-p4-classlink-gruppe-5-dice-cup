package de.dicecup.classlink.features.grades.web;

import de.dicecup.classlink.features.grades.SubjectAssignment;

import java.math.BigDecimal;
import java.util.UUID;

public record SubjectAssignmentDto(
        UUID id,
        String name,
        UUID classId,
        UUID termId,
        UUID subjectId,
        UUID teacherId,
        UUID finalGradeAssignmentId,
        BigDecimal weighting
) {
    public static SubjectAssignmentDto from(SubjectAssignment assignment) {
        return new SubjectAssignmentDto(
                assignment.getId(),
                assignment.getName(),
                assignment.getSchoolClass() != null ? assignment.getSchoolClass().getId() : null,
                assignment.getTerm() != null ? assignment.getTerm().getId() : null,
                assignment.getSubject() != null ? assignment.getSubject().getId() : null,
                assignment.getTeacher() != null ? assignment.getTeacher().getId() : null,
                assignment.getFinalGradeAssignment() != null ? assignment.getFinalGradeAssignment().getId() : null,
                assignment.getWeighting()
        );
    }
}
