package de.dicecup.classlink.features.assessments.dto;

import java.util.UUID;

public record StudentQuestionAssessmentDTO(
        UUID questionId,
        String questionText,
        Integer selfScore,
        Double peerAvgScore
) {
}
