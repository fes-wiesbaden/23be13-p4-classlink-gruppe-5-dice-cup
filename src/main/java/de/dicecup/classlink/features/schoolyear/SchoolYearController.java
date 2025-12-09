package de.dicecup.classlink.features.schoolyear;

import de.dicecup.classlink.features.schoolyear.web.SchoolYearCreateRequest;
import de.dicecup.classlink.features.schoolyear.web.SchoolYearDto;
import de.dicecup.classlink.features.terms.TermService;
import de.dicecup.classlink.features.terms.TermService.TermCreateRequest;
import de.dicecup.classlink.features.terms.web.TermCreateWebRequest;
import de.dicecup.classlink.features.terms.web.TermDto;
import de.dicecup.classlink.features.terms.TermStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/school-years")
@RequiredArgsConstructor
@Validated
public class SchoolYearController {

    private final SchoolYearService schoolYearService;
    private final TermService termService;

    @Operation(
            summary = "Schuljahre auflisten",
            description = "Gibt alle Schuljahre zurück, optional gefiltert nach Status."
    )
    @ApiResponse(responseCode = "200", description = "Liste der Schuljahre wurde erfolgreich abgerufen.")
    /**
     * Listet alle Schuljahre, optional nach Status gefiltert.
     *
     * @param status Optionaler Status des Schuljahres (ACTIVE oder CLOSED)
     * @return Liste von SchoolYearDto-Objekten
     */
    @GetMapping
    public List<SchoolYearDto> list(@RequestParam(required = false) SchoolYearStatus status) {
        return schoolYearService.list(status).stream().map(SchoolYearDto::from).collect(Collectors.toList());
    }

    @Operation(
            summary = "Neues Schuljahr erstellen",
            description = "Erstellt ein neues Schuljahr mit Namen und Zeitraum."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Schuljahr wurde erfolgreich erstellt."),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten.")
    })
    /**
     * Erstellt ein neues Schuljahr.
     *
     * @param request Anfrage mit Name, Start- und Enddatum des Schuljahres
     * @return ResponseEntity mit dem erstellten Schuljahr als DTO
     */
    @PostMapping
    public ResponseEntity<SchoolYearDto> createYear(@RequestBody @Valid SchoolYearCreateRequest request) {
        SchoolYear created = schoolYearService.create(new SchoolYearService.SchoolYearRequest(request.name(), request.startDate(), request.endDate()));
        return ResponseEntity.created(
                        java.net.URI.create("/api/school-years/" + created.getId()))
                .body(SchoolYearDto.from(created));
    }

    @Operation(
            summary = "Schuljahr schließen",
            description = "Schließt ein bestehendes Schuljahr anhand seiner ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Schuljahr wurde erfolgreich geschlossen."),
            @ApiResponse(responseCode = "404", description = "Schuljahr wurde nicht gefunden.")
    })
    /**
     * Schließt ein Schuljahr.
     *
     * @param id ID des Schuljahres
     * @return ResponseEntity ohne Inhalt bei Erfolg
     */
    @PostMapping("/{id}/close")
    public ResponseEntity<Void> closeYear(@PathVariable UUID id) {
        schoolYearService.close(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Halbjahr für Schuljahr eröffnen",
            description = "Erstellt ein neues Halbjahr innerhalb eines Schuljahres."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Halbjahr wurde erfolgreich erstellt."),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten."),
            @ApiResponse(responseCode = "404", description = "Schuljahr wurde nicht gefunden."),
            @ApiResponse(responseCode = "409", description = "Konflikt, z. B. es existiert bereits ein offenes Halbjahr.")
    })
    /**
     * Öffnet ein neues Halbjahr in einem bestehenden Schuljahr.
     *
     * @param id      ID des Schuljahres
     * @param request Anfrage mit Halbjahresdaten
     * @return ResponseEntity mit dem erstellten Halbjahr als DTO
     */
    @PostMapping("/{id}/terms")
    public ResponseEntity<TermDto> createTerm(@PathVariable UUID id, @RequestBody @Valid TermCreateWebRequest request) {
        var created = termService.openTerm(id, new TermCreateRequest(request.name(), request.sequenceNumber(), request.startDate(), request.endDate()));
        return ResponseEntity.created(java.net.URI.create("/api/terms/" + created.getId()))
                .body(TermDto.from(created));
    }

    @Operation(
            summary = "Halbjahre eines Schuljahres auflisten",
            description = "Listet alle Halbjahre eines Schuljahres, optional gefiltert nach Status."
    )
    @ApiResponse(responseCode = "200", description = "Halbjahre wurden erfolgreich geladen.")
    /**
     * Listet alle Halbjahre eines Schuljahres.
     *
     * @param id     ID des Schuljahres
     * @param status Optionaler Status der Halbjahre
     * @return Liste von TermDto-Objekten
     */
    @GetMapping("/{id}/terms")
    public List<TermDto> listTerms(@PathVariable UUID id, @RequestParam(required = false) TermStatus status) {
        return termService.listTerms(id, status).stream().map(TermDto::from).collect(Collectors.toList());
    }
}
