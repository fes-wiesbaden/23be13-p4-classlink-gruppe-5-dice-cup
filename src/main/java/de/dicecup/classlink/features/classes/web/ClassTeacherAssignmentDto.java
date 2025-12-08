package de.dicecup.classlink.features.classes.web;

import de.dicecup.classlink.features.grades.SubjectAssignment;

import java.math.BigDecimal;
import java.util.UUID;

public record ClassTeacherAssignmentDto(
        UUID id,
        String name,
        UUID classId,
        UUID termId,
        UUID teacherId,
        UUID subjectId,
        BigDecimal weighting
) {
    public static ClassTeacherAssignmentDto from(SubjectAssignment assignment) {
        return new ClassTeacherAssignmentDto(
                assignment.getId(),
                assignment.getName(),
                assignment.getSchoolClass().getId(),
                assignment.getTerm().getId(),
                assignment.getTeacher().getId(),
                assignment.getSubject().getId(),
                assignment.getWeighting()
        );
    }
}
