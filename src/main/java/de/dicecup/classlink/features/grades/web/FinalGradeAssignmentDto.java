package de.dicecup.classlink.features.grades.web;

import de.dicecup.classlink.features.grades.FinalGradeAssignment;

import java.util.UUID;

// Author: Marcel Plenert
// DTO for Final Grade Assignment
public record FinalGradeAssignmentDto(
        UUID id,
        String name,
        UUID classId,
        UUID termId,
        UUID subjectId,
        UUID teacherId
) {
    public static FinalGradeAssignmentDto from(FinalGradeAssignment assignment) {
        return new FinalGradeAssignmentDto(
                assignment.getId(),
                assignment.getName(),
                assignment.getSchoolClass().getId(),
                assignment.getTerm().getId(),
                assignment.getSubject().getId(),
                assignment.getTeacher().getId()
        );
    }
}

