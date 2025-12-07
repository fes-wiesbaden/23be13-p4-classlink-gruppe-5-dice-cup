package de.dicecup.classlink.features.classes.web;

import de.dicecup.classlink.features.classes.ClassFinalGradeAssignment;
import de.dicecup.classlink.features.classes.ClassSubjectAssignment;
import de.dicecup.classlink.features.grades.FinalGrade;
import de.dicecup.classlink.features.grades.Grade;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record ClassFinalGradeAssignmentDto(
        UUID id,
        String name,
        UUID classId,
        UUID termId,
        UUID teacherId,
        List<UUID> subGradeAssignments,
        List<UUID> grades,
        UUID subjectId
) {
    public static ClassFinalGradeAssignmentDto from(ClassFinalGradeAssignment assignment) {
        return new ClassFinalGradeAssignmentDto(
                assignment.getId(),
                assignment.getName(),
                assignment.getSchoolClass().getId(),
                assignment.getTerm().getId(),
                assignment.getTeacher().getId(),
                assignment.getSubGradeAssignments()
                        .stream()
                        .map(ClassSubjectAssignment::getId)
                        .collect(Collectors.toList()),
                assignment.getGrades()
                        .stream()
                        .map(FinalGrade::getId)
                        .collect(Collectors.toList()),
                assignment.getSubject().getId()
        );
    }
}
