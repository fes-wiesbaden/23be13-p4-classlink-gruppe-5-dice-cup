package de.dicecup.classlink.features.classes;

import de.dicecup.classlink.features.classes.web.ClassCreateRequest;
import de.dicecup.classlink.features.classes.web.ClassDto;
import de.dicecup.classlink.features.classes.web.ClassTeacherAssignmentDto;
import de.dicecup.classlink.features.classes.web.ClassTeacherAssignmentRequest;
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

    private final ClassRepository classRepository;
    private final ClassManagementService classManagementService;
    private final ClassTermRepository classTermRepository;

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
        return classRepository.findAll().stream().map(ClassDto::from).collect(Collectors.toList());
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
        Class clazz = new Class();
        clazz.setName(request.name());
        Class saved = classRepository.save(clazz);
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
    public Class update(@PathVariable UUID id, @RequestBody ClassCreateRequest request) {
        Class clazz = classRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Class not found"));
        clazz.setName(request.name());
        return classRepository.save(clazz);
    }

    @Operation(
            summary = "Lehrkraft einer Klasse zuordnen",
            description = "Ordnet einer Klasse und einem Halbjahr eine Lehrkraft und ein Fach zu."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Zuordnung wurde erfolgreich erstellt."),
            @ApiResponse(responseCode = "404", description = "Klasse, Halbjahr, Fach oder Lehrkraft wurde nicht gefunden.")
    })
    /**
     * Ordnet einer Klasse in einem Halbjahr eine Lehrkraft und ein Fach zu.
     *
     * @param classId ID der Klasse
     * @param termId  ID des Halbjahres
     * @param request Anfrage mit Fach, Lehrkraft und optionaler Gewichtung
     * @return ResponseEntity mit der erzeugten Zuordnung als DTO
     */
    @PostMapping("/{classId}/terms/{termId}/teachers")
    public ResponseEntity<ClassTeacherAssignmentDto> assignTeacher(@PathVariable UUID classId,
                                                                   @PathVariable UUID termId,
                                                                   @RequestBody @Valid ClassTeacherAssignmentRequest request) {
        ClassSubjectAssignment assignment = classManagementService.assignTeacher(classId, termId, request.subjectId(), request.teacherId(), request.weighting());
        return ResponseEntity.created(java.net.URI.create("/api/classes/" + classId + "/terms/" + termId + "/teachers/" + assignment.getId()))
                .body(ClassTeacherAssignmentDto.from(assignment));
    }

    @Operation(
            summary = "Lehrkraft-Zuordnungen einer Klasse abrufen",
            description = "Listet alle Lehrkraft-Zuordnungen für eine Klasse in einem Halbjahr auf."
    )
    @ApiResponse(responseCode = "200", description = "Zuordnungen wurden erfolgreich geladen.")
    /**
     * Listet alle Lehrkraft-Zuordnungen für eine Klasse und ein Halbjahr.
     *
     * @param classId ID der Klasse
     * @param termId  ID des Halbjahres
     * @return Liste von ClassTeacherAssignmentDto
     */
    @GetMapping("/{classId}/terms/{termId}/teachers")
    public List<ClassTeacherAssignmentDto> listAssignments(@PathVariable UUID classId, @PathVariable UUID termId) {
        return classManagementService.listAssignments(classId, termId).stream()
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
     * @param classId ID der Klasse
     * @return Liste von ClassTerm-Objekten
     */
    @GetMapping("/{classId}/terms")
    public List<ClassTerm> listClassTerms(@PathVariable UUID classId) {
        return classTermRepository.findBySchoolClassId(classId);
    }
}
