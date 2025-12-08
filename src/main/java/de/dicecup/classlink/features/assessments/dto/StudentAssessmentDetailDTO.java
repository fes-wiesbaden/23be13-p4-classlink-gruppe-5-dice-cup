package de.dicecup.classlink.features.assessments.dto;

import de.dicecup.classlink.features.assessments.TendencyLabel;

import java.util.List;
import java.util.UUID;

public record StudentAssessmentDetailDTO(
        UUID studentId,
        String studentName,
        Double selfAvg,
        Double peerAvg,
        Double combinedAvg,
        Double teacherGrade,
        Double delta,
        TendencyLabel tendencyLabel,
        List<StudentQuestionAssessmentDTO> questions
) {
}
