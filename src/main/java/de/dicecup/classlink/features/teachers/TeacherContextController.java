package de.dicecup.classlink.features.teachers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teachers/me")
public class TeacherContextController {

    private final TeacherContextService teacherContextService;

    public TeacherContextController(TeacherContextService teacherContextService) {
        this.teacherContextService = teacherContextService;
    }

    @Operation(summary = "Current teacher context", description = "Returns classes, terms and project groups supervised by the current teacher.")
    @ApiResponse(responseCode = "200", description = "Context loaded")
    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/context")
    public TeacherContextDto getContext() {
        return teacherContextService.loadContext();
    }
}
