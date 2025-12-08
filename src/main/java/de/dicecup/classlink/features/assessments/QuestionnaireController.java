package de.dicecup.classlink.features.assessments;

import de.dicecup.classlink.features.assessments.dto.QuestionRequest;
import de.dicecup.classlink.features.assessments.dto.QuestionnaireCreateRequest;
import de.dicecup.classlink.features.assessments.dto.QuestionnaireDto;
import de.dicecup.classlink.features.projects.ProjectGroupRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;
    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final AuthHelper authHelper;
    private final ProjectGroupRepository projectGroupRepository;

    @Operation(summary = "Fragebogen eines Projekts abrufen")
    @ApiResponse(responseCode = "200", description = "Fragebogen wurde geladen.")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/projects/{projectId}/questionnaire")
    public QuestionnaireDto getQuestionnaire(@PathVariable UUID projectId) {
        Questionnaire q = questionnaireRepository.findByProjectId(projectId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Questionnaire not found"));
        List<Question> questions = questionRepository.findByQuestionnaireId(q.getId());
        return QuestionnaireDto.from(q, questions);
    }

    @Operation(summary = "Fragebogen für Projekt erstellen oder laden")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fragebogen vorhanden."),
            @ApiResponse(responseCode = "201", description = "Fragebogen erstellt.")
    })
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PostMapping("/projects/{projectId}/questionnaire")
    public QuestionnaireDto createQuestionnaire(@PathVariable UUID projectId, @RequestBody @Valid QuestionnaireCreateRequest request) {
        UUID teacherId = authHelper.requireTeacherId();
        Questionnaire q = questionnaireService.getOrCreate(projectId, teacherId);
        List<Question> questions = questionRepository.findByQuestionnaireId(q.getId());
        return QuestionnaireDto.from(q, questions);
    }

    @Operation(summary = "Frage zu Fragebogen hinzufügen")
    @ApiResponse(responseCode = "200", description = "Frage hinzugefügt.")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PostMapping("/questionnaires/{questionnaireId}/questions")
    public QuestionnaireDto addQuestion(@PathVariable UUID questionnaireId, @RequestBody @Valid QuestionRequest request) {
        ensureTeacherOwnsQuestionnaire(questionnaireId);
        questionnaireService.addQuestion(questionnaireId, request.text(), request.position());
        Questionnaire q = questionnaireRepository.findById(questionnaireId).orElseThrow();
        List<Question> questions = questionRepository.findByQuestionnaireId(questionnaireId);
        return QuestionnaireDto.from(q, questions);
    }

    @Operation(summary = "Frage in Fragebogen ändern")
    @ApiResponse(responseCode = "200", description = "Frage aktualisiert.")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PutMapping("/questionnaires/{questionnaireId}/questions/{questionId}")
    public QuestionnaireDto updateQuestion(@PathVariable UUID questionnaireId,
                                           @PathVariable UUID questionId,
                                           @RequestBody @Valid QuestionRequest request) {
        ensureTeacherOwnsQuestionnaire(questionnaireId);
        questionnaireService.updateQuestion(questionnaireId, questionId, request.text(), request.position(), request.active());
        Questionnaire q = questionnaireRepository.findById(questionnaireId).orElseThrow();
        List<Question> questions = questionRepository.findByQuestionnaireId(questionnaireId);
        return QuestionnaireDto.from(q, questions);
    }

    @Operation(summary = "Frage löschen")
    @ApiResponse(responseCode = "204", description = "Frage gelöscht.")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @DeleteMapping("/questionnaires/{questionnaireId}/questions/{questionId}")
    public void deleteQuestion(@PathVariable UUID questionnaireId, @PathVariable UUID questionId) {
        ensureTeacherOwnsQuestionnaire(questionnaireId);
        questionnaireService.deleteQuestion(questionnaireId, questionId);
    }

    @Operation(summary = "Fragebogen öffnen")
    @ApiResponse(responseCode = "200", description = "Fragebogen geöffnet.")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PostMapping("/questionnaires/{questionnaireId}/open")
    public QuestionnaireDto open(@PathVariable UUID questionnaireId) {
        ensureTeacherOwnsQuestionnaire(questionnaireId);
        Questionnaire q = questionnaireService.openById(questionnaireId);
        List<Question> questions = questionRepository.findByQuestionnaireId(questionnaireId);
        return QuestionnaireDto.from(q, questions);
    }

    @Operation(summary = "Fragebogen schließen")
    @ApiResponse(responseCode = "200", description = "Fragebogen geschlossen.")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @PostMapping("/questionnaires/{questionnaireId}/close")
    public QuestionnaireDto close(@PathVariable UUID questionnaireId) {
        ensureTeacherOwnsQuestionnaire(questionnaireId);
        Questionnaire q = questionnaireService.closeById(questionnaireId);
        List<Question> questions = questionRepository.findByQuestionnaireId(questionnaireId);
        return QuestionnaireDto.from(q, questions);
    }

    private void ensureTeacherOwnsQuestionnaire(UUID questionnaireId) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Questionnaire not found"));
        if (authHelper.isAdmin()) {
            return;
        }
        UUID teacherId = authHelper.requireTeacherId();
        boolean owns = teacherId.equals(questionnaire.getCreatedByTeacherId())
                || projectGroupRepository.existsByProjectIdAndSupervisingTeacherId(questionnaire.getProjectId(), teacherId);
        if (!owns) {
            throw new AccessDeniedException("Not allowed to manage this questionnaire");
        }
    }
}
