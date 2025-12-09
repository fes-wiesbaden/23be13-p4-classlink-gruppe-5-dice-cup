package de.dicecup.classlink.features.projects;

import de.dicecup.classlink.features.projects.web.AssignMembersRequest;
import de.dicecup.classlink.features.projects.web.ProjectGroupDto;
import de.dicecup.classlink.features.projects.web.ProjectGroupMemberDto;
import de.dicecup.classlink.features.projects.web.CreateGroupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects/{projectId}/groups")
@RequiredArgsConstructor
public class ProjectGroupController {

    private final ProjectGroupService projectGroupService;

    @Operation(
            summary = "Projektgruppen eines Projekts auflisten",
            description = "Gibt alle Projektgruppen eines Projekts zurück."
    )
    @ApiResponse(responseCode = "200", description = "Projektgruppen wurden erfolgreich geladen.")
    /**
     * Listet alle Projektgruppen eines Projekts auf.
     *
     * @param projectId ID des Projekts
     * @return Liste von ProjectGroupDto
     */
    @GetMapping
    public List<ProjectGroupDto> list(@PathVariable UUID projectId) {
        return projectGroupService.listGroups(projectId).stream()
                .map(ProjectGroupDto::from)
                .collect(Collectors.toList());
    }

    @Operation(
            summary = "Projektgruppe erstellen",
            description = "Erstellt eine neue Projektgruppe für ein Projekt."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Projektgruppe wurde erfolgreich erstellt."),
            @ApiResponse(responseCode = "404", description = "Projekt oder Lehrkraft wurde nicht gefunden.")
    })
    /**
     * Erstellt eine neue Projektgruppe.
     *
     * @param projectId ID des Projekts
     * @param request   Anfrage mit Gruppennummer und optionaler betreuender Lehrkraft
     * @return ResponseEntity mit der erstellten Projektgruppe als DTO
     */
    @PostMapping
    public ResponseEntity<ProjectGroupDto> create(@PathVariable UUID projectId, @RequestBody CreateGroupRequest request) {
        ProjectGroup group = projectGroupService.createGroup(projectId, request.groupNumber());
        return ResponseEntity.created(java.net.URI.create("/api/projects/" + projectId + "/groups/" + group.getId()))
                .body(ProjectGroupDto.from(group));
    }

    @Operation(
            summary = "Mitglieder zu Projektgruppe zuordnen",
            description = "Ordnet einer Projektgruppe Studenten mit Rollen zu."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mitglieder wurden erfolgreich zugeordnet."),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten oder Studenten gehören nicht zur Klasse des Projekts."),
            @ApiResponse(responseCode = "404", description = "Projektgruppe oder Student wurde nicht gefunden.")
    })
    /**
     * Ordnet Mitglieder einer Projektgruppe zu.
     *
     * @param projectId ID des Projekts
     * @param groupId   ID der Projektgruppe
     * @param request   Anfrage mit den Mitgliedern und Rollen
     * @return Liste der gespeicherten Gruppenmitglieder als DTO
     */
    @PostMapping("/{groupId}/members")
    public ResponseEntity<List<ProjectGroupMemberDto>> assignMembers(@PathVariable UUID projectId,
                                                                     @PathVariable UUID groupId,
                                                                     @RequestBody AssignMembersRequest request) {
        projectGroupService.assignStudents(groupId, request.members());
        return ResponseEntity.ok(
                projectGroupService.listGroups(projectId).stream()
                        .filter(g -> g.getId().equals(groupId))
                        .flatMap(g -> g.getMembers().stream())
                        .map(ProjectGroupMemberDto::from)
                        .collect(Collectors.toList())
        );
    }

    @Operation(
            summary = "Mitglied aus Projektgruppe entfernen",
            description = "Entfernt einen Studenten aus einer Projektgruppe."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Mitglied wurde erfolgreich entfernt."),
            @ApiResponse(responseCode = "404", description = "Projektgruppe oder Mitglied wurde nicht gefunden.")
    })
    /**
     * Entfernt ein Mitglied aus einer Projektgruppe.
     *
     * @param projectId ID des Projekts
     * @param groupId   ID der Projektgruppe
     * @param studentId ID des Studenten
     * @return ResponseEntity ohne Inhalt bei Erfolg
     */
    @DeleteMapping("/{groupId}/members/{studentId}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID projectId,
                                             @PathVariable UUID groupId,
                                             @PathVariable UUID studentId) {
        projectGroupService.removeStudent(groupId, studentId);
        return ResponseEntity.noContent().build();
    }
}
