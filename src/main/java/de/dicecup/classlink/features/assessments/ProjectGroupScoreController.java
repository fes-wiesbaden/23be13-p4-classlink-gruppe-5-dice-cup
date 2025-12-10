package de.dicecup.classlink.features.assessments;

import de.dicecup.classlink.features.assessments.dto.ProjectGroupStudentScoreOverviewDto;
import de.dicecup.classlink.features.assessments.dto.StudentSubjectAssessmentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/project-groups")
public class ProjectGroupScoreController {

    private final ProjectGroupScoreService projectGroupScoreService;

    public ProjectGroupScoreController(ProjectGroupScoreService projectGroupScoreService) {
        this.projectGroupScoreService = projectGroupScoreService;
    }

    @Operation(summary = "Score overview for project group students")
    @ApiResponse(responseCode = "200", description = "Scores loaded")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @GetMapping("/{projectGroupId}/scores")
    public List<ProjectGroupStudentScoreOverviewDto> listScores(@PathVariable UUID projectGroupId) {
        return projectGroupScoreService.listGroupScores(projectGroupId);
    }

    @Operation(summary = "Subject scores for a student in a project group")
    @ApiResponse(responseCode = "200", description = "Subject scores loaded")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @GetMapping("/{projectGroupId}/students/{studentId}/subjects")
    public StudentSubjectAssessmentDto subjectScores(@PathVariable UUID projectGroupId,
                                                     @PathVariable UUID studentId,
                                                     @RequestParam(value = "questionnaireId", required = false) UUID questionnaireId) {
        return projectGroupScoreService.subjectScores(projectGroupId, studentId, questionnaireId);
    }
}
