package de.dicecup.classlink.features.assessments.dto;

import java.util.List;
import java.util.UUID;

public record StudentSubjectAssessmentDto(
        UUID studentId,
        String studentName,
        UUID projectGroupId,
        String projectName,
        UUID questionnaireId,
        List<SubjectScoreDto> subjects
) {
}
