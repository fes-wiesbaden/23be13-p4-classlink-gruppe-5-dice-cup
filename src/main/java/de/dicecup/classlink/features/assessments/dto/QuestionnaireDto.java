package de.dicecup.classlink.features.assessments.dto;

import de.dicecup.classlink.features.assessments.Question;
import de.dicecup.classlink.features.assessments.Questionnaire;
import de.dicecup.classlink.features.assessments.QuestionnaireStatus;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record QuestionnaireDto(
        UUID id,
        UUID projectId,
        QuestionnaireStatus status,
        List<QuestionDto> questions
) {
    public static QuestionnaireDto from(Questionnaire q, List<Question> questions) {
        return new QuestionnaireDto(
                q.getId(),
                q.getProjectId(),
                q.getStatus(),
                questions.stream().map(QuestionDto::from).collect(Collectors.toList())
        );
    }

    public record QuestionDto(UUID id, int position, String text, boolean active) {
        public static QuestionDto from(Question question) {
            return new QuestionDto(question.getId(), question.getPosition(), question.getText(), question.isActive());
        }
    }
}
