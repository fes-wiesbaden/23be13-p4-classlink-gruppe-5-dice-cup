package de.dicecup.classlink.features.assessments.dto;

import java.util.UUID;

public record AssessmentAnswerDto(UUID questionId, int score) {
}
