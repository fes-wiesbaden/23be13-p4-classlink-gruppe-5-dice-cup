package de.dicecup.classlink.features.grades;

import de.dicecup.classlink.features.grades.web.GradeCreateRequest;
import de.dicecup.classlink.features.grades.web.GradeDto;
import de.dicecup.classlink.features.users.domain.roles.StudentRepository;
import de.dicecup.classlink.features.users.domain.roles.TeacherRepository;
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

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeRepository gradeRepository;
    private final SubjectAssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final GradeManagementService gradeManagementService;
    private final AssignmentManagementService assignmentManagementService;

    @Operation(
            summary = "Neue Note erstellen",
            description = "Assignment für einen Schüler benoten"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Note wurde erfolgreich erstellt."),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten.")
    })
    /**
     * Erstellt eine neue Note.
     *
     * @param request Anfrageobjekt mit dem Namen der Klasse
     * @return ResponseEntity mit dem erstellten ClassDto
     */
    @PostMapping
    public ResponseEntity<GradeDto> create(@RequestBody @Valid GradeCreateRequest request) {
        Grade grade = new Grade();
        grade.setGradeValue(request.gradeValue());
        grade.setSubjectAssignment(
                assignmentRepository
                        .findBySubjectId(request.parentAssignmentId())
                        .orElseThrow(() -> new EntityNotFoundException("Assignment not found")));
        grade.setStudent(
                studentRepository
                        .findByUserId(request.studentId())
                        .orElseThrow(() -> new EntityNotFoundException("Student not found")));
        grade.setChangedBy(
                teacherRepository
                        .findByUserId(request.requestingTeacherId())
                        .orElseThrow(() -> new EntityNotFoundException("Teacher not found")));
        Grade saved = gradeRepository.save(grade);
        return ResponseEntity.created(java.net.URI.create("/api/grades/" + saved.getId()))
                .body(GradeDto.from(saved));
    }

    @Operation(
            summary = "Neue Note erstellen",
            description = "Assignment für einen Schüler benoten"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Note wurde erfolgreich erstellt."),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten."),
            @ApiResponse(responseCode = "404", description = "Zu ändernde Note nicht gefunden")
    })
    /**
     * Bearbeitet eine neue Note.
     *
     * @param id      ID der zu bearbeitenden Note
     * @param request Anfrageobjekt mit dem Namen der Klasse
     * @return ResponseEntity mit dem erstellten ClassDto
     */
    @PutMapping("/{id}")
    public Grade update(@PathVariable UUID id, @RequestBody @Valid GradeCreateRequest request) {
        Grade gradeToUpdate = gradeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Grade not found"));
        gradeToUpdate.setGradeValue(request.gradeValue());
        gradeToUpdate.setSubjectAssignment(
                assignmentRepository
                        .findBySubjectId(request.parentAssignmentId())
                        .orElseThrow(() -> new EntityNotFoundException("Assignment not found")));
        gradeToUpdate.setStudent(
                studentRepository
                        .findByUserId(request.studentId())
                        .orElseThrow(() -> new EntityNotFoundException("Student not found")));
        gradeToUpdate.setChangedBy(
                teacherRepository
                        .findByUserId(request.requestingTeacherId())
                        .orElseThrow(() -> new EntityNotFoundException("Teacher not found")));
        return gradeRepository.save(gradeToUpdate);
    }

    @Operation(
            summary = "Noten für Assignment auflisten",
            description = "Gibt alle vorhandenen Noten für eine Assignment zurück."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste aller Noten für eine Assignment"),
            @ApiResponse(responseCode = "404", description = "Assignment wurde nicht gefunden.")
    })
    @GetMapping("assignment/{id}")
    public List<GradeDto> getGradesByAssignment(@PathVariable UUID assignmentId) {
        return gradeRepository
                .findByClassSubjectAssignmentId(assignmentId)
                .stream()
                .map(GradeDto::from)
                .toList();
    }

    @Operation(
            summary = "Noten für Schüler auflisten",
            description = "Gibt alle vorhandenen Noten für einen Schüler zurück."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste aller Noten für eine Assignment"),
            @ApiResponse(responseCode = "404", description = "Schüler wurde nicht gefunden.")
    })
    @GetMapping("/student/{id}")
    public List<GradeDto> list(@PathVariable UUID id, @RequestBody @Valid UUID assignmentId) {
        return gradeRepository
                .findByClassSubjectAssignmentId(assignmentId)
                .stream()
                .map(GradeDto::from)
                .toList();
    }

    @Operation(
            summary = "Einzelne Note ausgeben",
            description = "Eine Note nach ID zurück."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note"),
            @ApiResponse(responseCode = "404", description = "Note wurde nicht gefunden.")
    })
    @GetMapping("{id}")
    public GradeDto getGrade(@PathVariable UUID id) {
        return gradeRepository
                .findById(id)
                .map(GradeDto::from)
                .orElseThrow(EntityNotFoundException::new);
    }
}
