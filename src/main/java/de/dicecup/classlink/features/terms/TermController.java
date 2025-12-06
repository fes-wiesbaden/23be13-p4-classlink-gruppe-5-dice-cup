package de.dicecup.classlink.features.terms;

import de.dicecup.classlink.features.terms.web.TermDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermController {

    private final TermService termService;

    @Operation(
            summary = "Halbjahre auflisten",
            description = "Listet alle Halbjahre optional gefiltert nach Schuljahr und Status auf."
    )
    @ApiResponse(responseCode = "200", description = "Liste der Halbjahre wurde erfolgreich abgerufen.")
    /**
     * Liefert eine Liste aller Halbjahre, optional gefiltert nach Schuljahr-ID und Status.
     *
     * @param schoolYearId Optionales Schuljahr, nach dem gefiltert werden soll
     * @param status       Optionaler Status (z. B. OPEN oder CLOSED)
     * @return Liste von TermDto-Objekten
     */
    @GetMapping
    public List<TermDto> list(@RequestParam(required = false) UUID schoolYearId,
                              @RequestParam(required = false) TermStatus status) {
        return termService.listTerms(schoolYearId, status).stream()
                .map(TermDto::from)
                .collect(Collectors.toList());
    }

    @Operation(
            summary = "Halbjahr schließen",
            description = "Schließt ein bestehendes Halbjahr anhand der ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Halbjahr wurde erfolgreich geschlossen."),
            @ApiResponse(responseCode = "404", description = "Halbjahr wurde nicht gefunden.")
    })
    /**
     * Schließt ein Halbjahr.
     *
     * @param termId ID des zu schließenden Halbjahres
     * @return ResponseEntity ohne Inhalt bei Erfolg
     */
    @PostMapping("/{termId}/close")
    public ResponseEntity<Void> closeTerm(@PathVariable UUID termId) {
        termService.closeTerm(termId);
        return ResponseEntity.noContent().build();
    }
}
