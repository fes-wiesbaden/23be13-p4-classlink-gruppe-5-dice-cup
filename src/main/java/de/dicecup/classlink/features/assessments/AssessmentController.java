package de.dicecup.classlink.features.assessments;

import de.dicecup.classlink.features.assessments.dto.AssessmentAnswerDto;
import de.dicecup.classlink.features.assessments.dto.AssessmentSubmissionRequest;
import de.dicecup.classlink.features.assessments.dto.ProjectAssessmentOverviewDTO;
import de.dicecup.classlink.features.assessments.dto.QuestionnaireDto;
import de.dicecup.classlink.features.projects.ProjectGroupMemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final AssessmentAggregationService aggregationService;
    private final AuthHelper authHelper;
    private final ProjectGroupMemberRepository projectGroupMemberRepository;

    @Operation(summary = "Eigene Self-Assessment abrufen")
    @ApiResponse(responseCode = "200", description = "Self-Assessment geladen.")
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/projects/{projectId}/assessments/self")
    public Map<String, Object> getSelf(@PathVariable UUID projectId) {
        UUID studentId = authHelper.requireStudentId();
        ensureStudentInProject(projectId, studentId);
        Questionnaire questionnaire = questionnaireRepository.findByProjectId(projectId)
                .orElseThrow();
        List<Question> questions = questionRepository.findByQuestionnaireId(questionnaire.getId());
        Optional<Assessment> assessmentOpt = assessmentRepository.findByQuestionnaireIdAndAssessorStudentIdAndAssesseeStudentId(
                questionnaire.getId(), studentId, studentId).stream().findFirst();
        List<AssessmentAnswerDto> answers = assessmentOpt.map(a -> assessmentAnswerRepository.findByAssessmentId(a.getId()).stream()
                .map(ans -> new AssessmentAnswerDto(ans.getQuestion().getId(), ans.getScore()))
                .toList()).orElse(List.of());
        Map<String, Object> response = new HashMap<>();
        response.put("questionnaire", QuestionnaireDto.from(questionnaire, questions));
        response.put("answers", answers);
        return response;
    }

    @Operation(summary = "Self-Assessment speichern")
    @ApiResponse(responseCode = "200", description = "Self-Assessment gespeichert.")
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/projects/{projectId}/assessments/self")
    public void submitSelf(@PathVariable UUID projectId, @RequestBody @Valid AssessmentSubmissionRequest request) {
        UUID studentId = authHelper.requireStudentId();
        List<AssessmentService.AnswerCommand> cmds = request.answers().stream()
                .map(dto -> new AssessmentService.AnswerCommand(dto.questionId(), dto.score()))
                .collect(Collectors.toList());
        assessmentService.submitSelf(projectId, studentId, cmds);
    }

    @Operation(summary = "Peer-Assessment abrufen")
    @ApiResponse(responseCode = "200", description = "Peer-Assessment geladen.")
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/projects/{projectId}/assessments/peer/{assesseeStudentId}")
    public Map<String, Object> getPeer(@PathVariable UUID projectId,
                                       @PathVariable UUID assesseeStudentId) {
        UUID assessorStudentId = authHelper.requireStudentId();
        ensureStudentInProject(projectId, assessorStudentId);
        ensureStudentInProject(projectId, assesseeStudentId);
        Questionnaire questionnaire = questionnaireRepository.findByProjectId(projectId)
                .orElseThrow();
        List<Question> questions = questionRepository.findByQuestionnaireId(questionnaire.getId());
        Optional<Assessment> assessmentOpt = assessmentRepository.findByQuestionnaireIdAndTypeAndAssessorStudentIdAndAssesseeStudentId(
                questionnaire.getId(), AssessmentType.PEER, assessorStudentId, assesseeStudentId);
        List<AssessmentAnswerDto> answers = assessmentOpt.map(a -> assessmentAnswerRepository.findByAssessmentId(a.getId()).stream()
                .map(ans -> new AssessmentAnswerDto(ans.getQuestion().getId(), ans.getScore()))
                .toList()).orElse(List.of());
        Map<String, Object> response = new HashMap<>();
        response.put("questionnaire", QuestionnaireDto.from(questionnaire, questions));
        response.put("answers", answers);
        return response;
    }

    @Operation(summary = "Peer-Assessment speichern")
    @ApiResponse(responseCode = "200", description = "Peer-Assessment gespeichert.")
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/projects/{projectId}/assessments/peer/{assesseeStudentId}")
    public void submitPeer(@PathVariable UUID projectId,
                           @PathVariable UUID assesseeStudentId,
                           @RequestBody @Valid AssessmentSubmissionRequest request) {
        UUID studentId = authHelper.requireStudentId();
        List<AssessmentService.AnswerCommand> cmds = request.answers().stream()
                .map(dto -> new AssessmentService.AnswerCommand(dto.questionId(), dto.score()))
                .collect(Collectors.toList());
        assessmentService.submitPeer(projectId, studentId, assesseeStudentId, cmds);
    }

    @Operation(summary = "Assessment-Übersicht für Projekt abrufen")
    @ApiResponse(responseCode = "200", description = "Übersicht berechnet.")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/projects/{projectId}/assessment-overview")
    public ProjectAssessmentOverviewDTO overview(@PathVariable UUID projectId,
                                                 @RequestParam List<UUID> studentIds) {
        Set<UUID> studentSet = new HashSet<>(studentIds);
        return aggregationService.buildOverview(projectId, studentSet, Map.of(), Map.of());
    }

    private void ensureStudentInProject(UUID projectId, UUID studentId) {
        if (!projectGroupMemberRepository.existsByProjectGroupProjectIdAndStudentId(projectId, studentId)) {
            throw new AccessDeniedException("Student not part of project");
        }
    }
}
