package de.dicecup.classlink.features.subjects;

import de.dicecup.classlink.features.subjects.web.SubjectCreationRequest;
import de.dicecup.classlink.features.subjects.web.SubjectDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectRepository subjectRepository;
    private final SubjectManagementService subjectService;

    @Operation(
            summary = "Erstellt ein Lernfeld/Schulfach",
            description = "Hier werden die einzelnen Lernfelder angelegt, welche später benotet werden."
    )
    @ApiResponse(responseCode = "200", description = "Lernfeld wurde erstellt")
    /**
     * Erstellt ein neues Lernfeld
     *
     * @return SubjectDto des erstellten Lernfeldes
     */
    @PostMapping("/create")
    public ResponseEntity<SubjectDto> createSubject(@RequestBody @Valid SubjectCreationRequest request) {
        Subject saved = subjectService.createSubject(request.name(), request.description());
        return ResponseEntity.created(java.net.URI.create("/api/subjects/" + saved.getId()))
                .body(SubjectDto.from(saved));
    }

    @Operation(
            summary = "Bearbeitet ein Lernfeld",
            description = "Nimmt Änderungen an einem bereits bestehendem Lernfeld vor"
    )
    @ApiResponse(responseCode = "200", description = "Lernfeld wurde bearbeitet")
    /**
     * Bearbeitet ein Lernfeld
     *
     * @param request Anfrageobjekt mit geänderten Daten
     * @return SubjectDto des geänderten Lernfeldes
     */
    @PostMapping("{subject_id}")
    public ResponseEntity<SubjectDto> updateSubject(@PathVariable UUID subject_id,
                                                    @RequestBody SubjectCreationRequest request
    ) {
        Subject updated = subjectService.updateSubject(
                subject_id,
                request.name(),
                request.description()
        );
        return ResponseEntity.ok(SubjectDto.from(updated));
    }
}
