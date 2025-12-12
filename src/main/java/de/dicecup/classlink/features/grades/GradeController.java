package de.dicecup.classlink.features.grades;

import de.dicecup.classlink.features.grades.web.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

//Author: Marcel Plenert
// Creates API Endpoints for Grade and Assignment functionality
@RestController
@RequestMapping("/api/assignment")
@RequiredArgsConstructor
public class GradeController {

    private final GradeRepository gradeRepository;
    private final FinalGradeRepository finalGradeRepository;
    private final GradeManagementService gradeManagementService;
    private final AssignmentManagementService assignmentManagementService;

    @Operation(
            summary = "Neues Final-Assignment erstellen",
            description = "Final-Assignment erstellen"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Assignment wurde erfolgreich erstellt."),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten.")
    })
    /**
     * Erstellt ein neues Final-Assignment.
     *
     * @param request Anfrage mit AssignmentId, StudentId, Notenwert und erstellendem Lehrer
     * @return ResponseEntity mit dem erstellten FinalGradeAssignmentDto
     */
    @PostMapping("/final")
    public ResponseEntity<FinalGradeAssignmentDto> createFinalAssignment(@RequestBody @Valid FinalGradeAssignmentCreationRequest request) {
        FinalGradeAssignment assignment = assignmentManagementService.createAndSaveFinalAssignment(
                request.name(),
                request.schoolClassId(),
                request.termId(),
                request.subjectId(),
                request.teacherId()
        );
        UUID assignmentId = assignment.getId();
        return ResponseEntity
                .created(java.net.URI.create("/api/assignment/" + assignmentId))
                .body(FinalGradeAssignmentDto.from(assignment));
    }

    @Operation(
            summary = "Final-Assignment abschließen",
            description = "Final-Assignment abschließen und Noten dafür berechnen"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Assignment wurde erfolgreich abgeschlossen."),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten.")
    })
    /**
     * Berechnet die Note eines Final-Assignments.
     *
     * @param assignment_id Id der Final-Assignment
     * @param request Anfrage mit AssignmentId, StudentId, Notenwert und erstellendem Lehrer
     * @return ResponseEntity mit dem erstellten FinalGradeAssignmentDto
     */
    @PostMapping("/final/{assignment_id}")
    public ResponseEntity<List<FinalGradeDto>> createFinalAssignment(@PathVariable UUID assignment_id, @RequestBody @Valid FinalGradeCalculationRequest request) {
        if(gradeManagementService.userIsNotAdmin()){
            throw new IllegalStateException("User is not authorised");
        }

        List<FinalGrade> finalGrades = gradeManagementService.calculateFinalGrades(assignment_id);
        List<FinalGradeDto> finalGradeDtos = finalGrades.stream().map(FinalGradeDto::from).toList();
        return ResponseEntity
                .created(java.net.URI.create("/api/assignment/final" + assignment_id + "/grades"))
                .body(finalGradeDtos);
    }


    @Operation(
            summary = "Neues Assignment erstellen",
            description = "Assignment erstellen"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Note wurde erfolgreich erstellt."),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten.")
    })
    /**
     * Erstellt ein neues Assignment.
     *
     * @param request Anfrage mit AssignmentId, StudentId, Notenwert und erstellendem Lehrer
     * @return ResponseEntity mit dem erstellten SubjectAssignmentDto
     */
    @PostMapping()
    public ResponseEntity<SubjectAssignmentDto> create(@RequestBody @Valid SubjectAssignmentRequest request) {
        SubjectAssignment assignment = assignmentManagementService.createAndSaveAssignment(
                request.name(),
                request.classId(),
                request.termId(),
                request.subjectId(),
                request.teacherId(),
                request.finalGradeAssignmentId(),
                request.weighting()
        );
        return ResponseEntity
                .created(java.net.URI.create("/api/assignment/" + assignment.getId()))
                .body(SubjectAssignmentDto.from(assignment));
    }

    @Operation(
            summary = "Neue Note erstellen",
            description = "Note für einen Schüler erstellen"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Assignment wurde erfolgreich erstellt."),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten.")
    })
    /**
     * Erstellt eine neue Note.
     *
     * @param request Anfrage mit AssignmentId, StudentId, Notenwert und erstellendem Lehrer
     * @return ResponseEntity mit dem erstellten GradeDto
     */
    @PostMapping("/{assignmentId}/grade")
    public ResponseEntity<GradeDto> createGrade(@PathVariable UUID assignmentId, @RequestBody @Valid GradeCreateRequest request) {
        Grade grade = gradeManagementService.createGrade(
                request.requestingTeacherId(),
                request.studentId(),
                request.parentAssignmentId(),
                request.gradeValue()
        );
        return ResponseEntity
                .created(java.net.URI.create("/api/assignment/" + request.parentAssignmentId() + "grade" + grade.getId()))
                .body(GradeDto.from(grade));
    }

    @Operation(
            summary = "Note bearbeiten",
            description = "Assignment für einen Schüler benoten"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Note wurde erfolgreich erstellt."),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten."),
            @ApiResponse(responseCode = "404", description = "Zu ändernde Note nicht gefunden")
    })
    /**
     * Bearbeitet eine Note.
     *
     * @param id      ID der zu bearbeitenden Note
     * @param request Anfrage mit AssignmentId, StudentId, Notenwert und erstellendem Lehrer
     * @return ResponseEntity mit dem erstellten ClassDto
     */
    @PutMapping("/{assignmentId}/grade/{gradeId}")
    public ResponseEntity<GradeDto> updateGrade(@PathVariable UUID gradeId, @PathVariable String assignmentId, @RequestBody @Valid GradeCreateRequest request) {
        if (gradeManagementService.GradeDoesNotMatchAssignment(gradeId, assignmentId)) {
            throw new EntityNotFoundException("Invalid Path");
        }
        Grade gradeToUpdate = gradeManagementService.updateGrade(
                request.requestingTeacherId(),
                gradeId,
                request.gradeValue()
        );
        return ResponseEntity
                .accepted()
                .body(GradeDto.from(gradeToUpdate));
    }
    @Operation(
            summary = "Finale Noten ausgeben",
            description = "Gibt Finale Noten aus, kalkuliert diese wenn noch nicht geschehen"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste aller Finalen Noten"),
            @ApiResponse(responseCode = "404", description = "Assignment wurde nicht gefunden.")
    })
    /**
     * Gibt alle Finalen Noten eines Assignments zurück.
     *
     * @param assignmentId      ID der zu bearbeitenden Note
     * @return ResponseEntity mit dem erstellten ClassDto
     */
    @GetMapping("/final/{assignmentId}/grades")
    public ResponseEntity<List<FinalGradeDto>> getFinalGradesByAssignment(@PathVariable UUID assignmentId) {
        return ResponseEntity.ok(finalGradeRepository
                .findById(assignmentId)
                .stream()
                .map(FinalGradeDto::from)
                .toList());
    }

    @Operation(
            summary = "Noten für Assignment auflisten",
            description = "Gibt alle vorhandenen Noten für eine Assignment zurück."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste aller Noten für eine Assignment"),
            @ApiResponse(responseCode = "404", description = "Assignment wurde nicht gefunden.")
    })
    /**
     * Gibt alle Benotungen eines Assignments zurück.
     *
     * @param assignmentId      ID der zu bearbeitenden Note
     * @return ResponseEntity mit einer Liste der GradeDtos
     */
    @GetMapping("{assignmentId}/grades")
    public ResponseEntity<List<GradeDto>> getGradesByAssignment(@PathVariable UUID assignmentId) {
        return ResponseEntity.ok(gradeRepository
                .findBySubjectAssignmentId(assignmentId)
                .stream()
                .map(GradeDto::from)
                .toList());
    }

    @Operation(
            summary = "Noten für Schüler auflisten",
            description = "Gibt alle vorhandenen Noten für einen Schüler in einem Halbjahr zurück."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste aller Noten für eine Assignment"),
            @ApiResponse(responseCode = "404", description = "Schüler wurde nicht gefunden.")
    })
    @GetMapping("/student/{studentId}/term/{termId}")
    public ResponseEntity<List<GradeDto>> list(@PathVariable UUID studentId, @PathVariable UUID termId) {
        List<Grade> grades = gradeManagementService.GetAllGradesForStudentPerTerm(studentId, termId);
        return ResponseEntity.ok(
                grades.stream()
                .map(GradeDto::from)
                .toList());
    }

    @Operation(
            summary = "Einzelne Note ausgeben",
            description = "Eine Note nach ID zurück."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note"),
            @ApiResponse(responseCode = "404", description = "Note wurde nicht gefunden.")
    })
    @GetMapping("/{assignmentId}/grade/{gradeId}")
    public ResponseEntity<GradeDto> getGrade(@PathVariable UUID gradeId, @PathVariable String assignmentId) {
        if (gradeManagementService.GradeDoesNotMatchAssignment(gradeId, assignmentId)) {
            throw new EntityNotFoundException("Invalid Path");
        }
        return ResponseEntity.ok(gradeRepository
                .findById(gradeId)
                .map(GradeDto::from)
                .orElseThrow(EntityNotFoundException::new));
    }
}
