package de.dicecup.classlink.features.assessments;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionnaireService {

    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public Questionnaire getOrCreate(UUID projectId, UUID teacherId) {
        return questionnaireRepository.findByProjectId(projectId)
                .orElseGet(() -> createNew(projectId, teacherId));
    }

    private Questionnaire createNew(UUID projectId, UUID teacherId) {
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setProjectId(projectId);
        questionnaire.setCreatedByTeacherId(teacherId);
        questionnaire.setStatus(QuestionnaireStatus.DRAFT);
        return questionnaireRepository.save(questionnaire);
    }

    @Transactional
    public Question addQuestion(UUID questionnaireId, String text, int position) {
        Questionnaire questionnaire = loadQuestionnaire(questionnaireId);
        ensureDraft(questionnaire);
        Question question = new Question();
        question.setQuestionnaire(questionnaire);
        question.setText(text);
        question.setPosition(position);
        question.setActive(true);
        return questionRepository.save(question);
    }

    @Transactional
    public Question updateQuestion(UUID questionnaireId, UUID questionId, String text, int position, boolean active) {
        Questionnaire questionnaire = loadQuestionnaire(questionnaireId);
        ensureDraft(questionnaire);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found"));
        if (!question.getQuestionnaire().getId().equals(questionnaireId)) {
            throw new IllegalArgumentException("Question does not belong to questionnaire");
        }
        question.setText(text);
        question.setPosition(position);
        question.setActive(active);
        return questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(UUID questionnaireId, UUID questionId) {
        Questionnaire questionnaire = loadQuestionnaire(questionnaireId);
        ensureDraft(questionnaire);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found"));
        if (!question.getQuestionnaire().getId().equals(questionnaireId)) {
            throw new IllegalArgumentException("Question does not belong to questionnaire");
        }
        questionRepository.delete(question);
    }

    @Transactional
    public Questionnaire openQuestionnaire(UUID projectId) {
        Questionnaire questionnaire = questionnaireRepository.findByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Questionnaire not found"));
        if (questionnaire.getStatus() != QuestionnaireStatus.DRAFT) {
            throw new IllegalStateException("Questionnaire not in DRAFT");
        }
        if (questionRepository.findByQuestionnaireId(questionnaire.getId()).isEmpty()) {
            throw new IllegalStateException("Cannot open questionnaire without questions");
        }
        questionnaire.setStatus(QuestionnaireStatus.OPEN);
        return questionnaire;
    }

    @Transactional
    public Questionnaire closeQuestionnaire(UUID projectId) {
        Questionnaire questionnaire = questionnaireRepository.findByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Questionnaire not found"));
        if (questionnaire.getStatus() != QuestionnaireStatus.OPEN) {
            throw new IllegalStateException("Questionnaire not in OPEN status");
        }
        questionnaire.setStatus(QuestionnaireStatus.CLOSED);
        return questionnaire;
    }

    private Questionnaire loadQuestionnaire(UUID questionnaireId) {
        return questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new EntityNotFoundException("Questionnaire not found"));
    }

    private void ensureDraft(Questionnaire questionnaire) {
        if (questionnaire.getStatus() != QuestionnaireStatus.DRAFT) {
            throw new IllegalStateException("Questionnaire is not editable");
        }
    }
}
