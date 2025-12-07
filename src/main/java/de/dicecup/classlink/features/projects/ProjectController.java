package de.dicecup.classlink.features.projects;

import de.dicecup.classlink.features.projects.web.ProjectDetailsDto;
import de.dicecup.classlink.features.projects.web.ProjectDto;
import de.dicecup.classlink.features.projects.web.ProjectRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(
            summary = "Projekte einer Klasse und eines Halbjahres auflisten",
            description = "Gibt alle Projekte für die angegebene Klasse und das angegebene Halbjahr zurück."
    )
    @ApiResponse(responseCode = "200", description = "Projekte wurden erfolgreich geladen.")
    /**
     * Listet alle Projekte zu einer Klasse und einem Halbjahr auf.
     *
     * @param classId ID der Klasse
     * @param termId  ID des Halbjahres
     * @return Liste von ProjectDto
     */
    @GetMapping("/classes/{classId}/terms/{termId}/projects")
    public List<ProjectDto> listProjects(@PathVariable UUID classId, @PathVariable UUID termId) {
        return projectService.listProjects(classId, termId).stream()
                .map(ProjectDto::from)
                .collect(Collectors.toList());
    }

    @Operation(
            summary = "Neues Projekt erstellen",
            description = "Erstellt ein neues Projekt für die angegebene Klasse und das angegebene Halbjahr."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Projekt wurde erfolgreich erstellt."),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten."),
            @ApiResponse(responseCode = "404", description = "Benötigte Ressourcen wurden nicht gefunden.")
    })
    /**
     * Erstellt ein neues Projekt.
     *
     * @param classId ID der Klasse
     * @param termId  ID des Halbjahres
     * @param request Anfrageobjekt mit Projektdaten
     * @return ResponseEntity mit dem erstellten ProjectDto
     */
    @PostMapping("/classes/{classId}/terms/{termId}/projects")
    public ResponseEntity<ProjectDto> createProject(@PathVariable UUID classId,
                                                    @PathVariable UUID termId,
                                                    @RequestBody @Valid ProjectRequestDto request) {
        Project project = projectService.createProject(classId, termId, new ProjectService.ProjectRequest(request.name(), request.description()));
        return ResponseEntity.created(java.net.URI.create("/api/projects/" + project.getId()))
                .body(ProjectDto.from(project));
    }

    @Operation(
            summary = "Projekt nach ID abrufen",
            description = "Gibt die Details eines Projekts anhand der ID zurück."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projekt wurde erfolgreich geladen."),
            @ApiResponse(responseCode = "404", description = "Projekt wurde nicht gefunden.")
    })
    /**
     * Ruft ein Projekt anhand seiner ID ab.
     *
     * @param projectId ID des Projekts
     * @return ProjectDetailsDto mit Projektdaten
     */
    @GetMapping("/projects/{projectId}")
    public ProjectDetailsDto getProject(@PathVariable UUID projectId) {
        return ProjectDetailsDto.from(projectService.getById(projectId));
    }

    @Operation(
            summary = "Projekt aktualisieren",
            description = "Aktualisiert die Daten eines bestehenden Projekts."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projekt wurde erfolgreich aktualisiert."),
            @ApiResponse(responseCode = "404", description = "Projekt wurde nicht gefunden.")
    })
    /**
     * Aktualisiert ein Projekt.
     *
     * @param projectId ID des Projekts
     * @param request   Anfrageobjekt mit aktualisierten Projektdaten
     * @return Aktualisiertes ProjectDto
     */
    @PutMapping("/projects/{projectId}")
    public ProjectDto updateProject(@PathVariable UUID projectId,
                                    @RequestBody @Valid ProjectRequestDto request) {
        Project updated = projectService.updateProject(projectId, new ProjectService.ProjectRequest(request.name(), request.description()));
        return ProjectDto.from(updated);
    }

    @Operation(
            summary = "Projekt archivieren",
            description = "Archiviert ein Projekt anhand seiner ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Projekt wurde erfolgreich archiviert."),
            @ApiResponse(responseCode = "404", description = "Projekt wurde nicht gefunden.")
    })
    /**
     * Archiviert ein Projekt.
     *
     * @param projectId ID des Projekts
     * @return ResponseEntity ohne Inhalt bei Erfolg
     */
    @PostMapping("/projects/{projectId}/archive")
    public ResponseEntity<Void> archiveProject(@PathVariable UUID projectId) {
        projectService.archiveProject(projectId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Projekte eines Schülers auflisten",
            description = "Listet Projekte eines Schülers für ein bestimmtes Halbjahr auf."
    )
    @ApiResponse(responseCode = "200", description = "Projekte wurden erfolgreich geladen.")
    /**
     * Listet Projekte für einen Schüler in einem Halbjahr.
     *
     * @param studentId ID des Schülers
     * @param termId    ID des Halbjahres
     * @return Liste von ProjectDto
     */
    @GetMapping("/students/{studentId}/terms/{termId}/projects")
    public List<ProjectDto> listProjectsForStudent(@PathVariable UUID studentId, @PathVariable UUID termId) {
        return projectService.listProjectsForStudent(studentId, termId).stream()
                .map(ProjectDto::from)
                .collect(Collectors.toList());
    }
}
