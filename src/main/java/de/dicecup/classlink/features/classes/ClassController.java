package de.dicecup.classlink.features.classes;

import de.dicecup.classlink.features.classes.web.ClassCreateRequest;
import de.dicecup.classlink.features.classes.web.ClassDto;
import de.dicecup.classlink.features.classes.web.ClassTeacherAssignmentDto;
import de.dicecup.classlink.features.grades.AssignmentManagementService;
import de.dicecup.classlink.features.grades.SubjectAssignment;
import de.dicecup.classlink.features.classes.SchoolClass;
import de.dicecup.classlink.features.classes.StudentInClassDto;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {

    private final SchoolClassRepository schoolClassRepository;
    private final AssignmentManagementService assignmentManagementService;
    private final ClassTermRepository classTermRepository;
    private final ClassService classService;

    @Operation(
            summary = "Klassen auflisten",
            description = "Gibt alle vorhandenen Klassen zurück."
    )
    @ApiResponse(responseCode = "200", description = "Liste der Klassen wurde erfolgreich geladen.")
    /**
     * Listet alle vorhandenen Klassen.
     *
     * @return Liste von ClassDto-Objekten
     */
    @GetMapping
    public List<ClassDto> list() {
        return schoolClassRepository.findAll().stream().map(ClassDto::from).collect(Collectors.toList());
    }

    @Operation(
            summary = "Neue Klasse erstellen",
            description = "Erstellt eine neue Klasse mit dem übergebenen Namen."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Klasse wurde erfolgreich erstellt."),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten.")
    })
    /**
     * Erstellt eine neue Klasse.
     *
     * @param request Anfrageobjekt mit dem Namen der Klasse
     * @return ResponseEntity mit dem erstellten ClassDto
     */
    @PostMapping
    public ResponseEntity<ClassDto> create(@RequestBody @Valid ClassCreateRequest request) {
        SchoolClass clazz = new SchoolClass();
        clazz.setName(request.name());
        SchoolClass saved = schoolClassRepository.save(clazz);
        return ResponseEntity.created(java.net.URI.create("/api/classes/" + saved.getId()))
                .body(ClassDto.from(saved));
    }

    @Operation(
            summary = "Klasse aktualisieren",
            description = "Aktualisiert den Namen einer bestehenden Klasse."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Klasse wurde erfolgreich aktualisiert."),
            @ApiResponse(responseCode = "404", description = "Klasse wurde nicht gefunden.")
    })
    /**
     * Aktualisiert eine bestehende Klasse.
     *
     * @param id      ID der Klasse
     * @param request Anfrageobjekt mit dem neuen Namen
     * @return Aktualisierte Klasse
     */
    @PutMapping("/{id}")
    public SchoolClass update(@PathVariable UUID id, @RequestBody ClassCreateRequest request) {
        SchoolClass clazz = schoolClassRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Class not found"));
        clazz.setName(request.name());
        return schoolClassRepository.save(clazz);
    }

    @Operation(
            summary = "Lehrkraft-Zuordnungen einer Klasse abrufen",
            description = "Listet alle Lehrkraft-Zuordnungen für eine Klasse in einem Halbjahr auf."
    )
    @ApiResponse(responseCode = "200", description = "Zuordnungen wurden erfolgreich geladen.")
    /**
     * Listet alle Lehrkraft-Zuordnungen für eine Klasse und ein Halbjahr.
     *
     * @param schoolClassId ID der Klasse
     * @param termId  ID des Halbjahres
     * @return Liste von ClassTeacherAssignmentDto
     */
    @GetMapping("/{classId}/terms/{termId}/teachers")
    public List<ClassTeacherAssignmentDto> listAssignments(@PathVariable UUID classId, @PathVariable UUID termId) {
        return assignmentManagementService.listAssignments(classId, termId).stream()
                .map(ClassTeacherAssignmentDto::from)
                .collect(Collectors.toList());
    }

    @Operation(
            summary = "Halbjahre einer Klasse auflisten",
            description = "Gibt alle Halbjahreszuordnungen einer Klasse zurück."
    )
    @ApiResponse(responseCode = "200", description = "Halbjahreszuordnungen wurden erfolgreich geladen.")
    /**
     * Listet alle Halbjahreszuordnungen einer Klasse.
     *
     * @param schoolClassId ID der Klasse
     * @return Liste von ClassTerm-Objekten
     */
    @GetMapping("/{classId}/terms")
    public List<ClassTerm> listClassTerms(@PathVariable UUID classId) {
        return classTermRepository.findBySchoolClassId(classId);
    }

    @Operation(
            summary = "Schüler einer Klasse auflisten",
            description = "Gibt alle Schüler aus derselben Klasse zurücl."
    )
    @ApiResponse(responseCode = "200", description = "Schülerliste wurde erfolgreich geladen.")
    /**
     * Listet alle Schüler einer Klasse.
     *
     * @param classId
     * @return Liste von Usern
     */
    @GetMapping("/{classId}/students")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<StudentInClassDto>> listClassStudents(@PathVariable UUID classId) {
        List<StudentInClassDto> students = classService.loadStudentsOfClass(classId);
        return ResponseEntity.ok(students);
    }
}
