package de.dicecup.classlink.features.assessments.dto;

import java.util.List;

public record AssessmentSubmissionRequest(List<AssessmentAnswerDto> answers) {
}
