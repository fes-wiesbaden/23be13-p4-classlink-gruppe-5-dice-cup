package de.dicecup.classlink.features.registration;

import de.dicecup.classlink.features.registration.domain.CreateInviteRequestDto;
import de.dicecup.classlink.features.registration.domain.InviteCreatedResponseDto;
import de.dicecup.classlink.features.registration.domain.InviteRedeemRequestDto;
import de.dicecup.classlink.features.registration.domain.InviteRedeemResponseDto;
import de.dicecup.classlink.features.registration.domain.InviteValidationRequestDto;
import de.dicecup.classlink.features.registration.domain.InviteValidationResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invites")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;


    @Operation(
            summary = "Einladung erstellen",
            description = "Erstellt eine neue Registrierungseinladung."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Einladung wurde erfolgreich erstellt."),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten.")
    })
    /**
     * Erstellt eine neue Einladung.
     *
     * @param request Anfrage mit den Einladungsdaten
     * @return Antwort mit der erstellten Einladung
     */
    @PostMapping("/create")
    public InviteCreatedResponseDto create(@Valid @RequestBody CreateInviteRequestDto request) {
        return invitationService.createInvite(request);
    }

    @Operation(
            summary = "Einladung validieren",
            description = "Validiert eine Einladung anhand des Tokens."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Einladung ist gültig."),
            @ApiResponse(responseCode = "400", description = "Einladung ist ungültig oder abgelaufen.")
    })
    /**
     * Validiert eine Einladung.
     *
     * @param request Anfrage mit Validierungsdaten
     * @return Validierungsantwort
     */
    @PostMapping("/validate")
    public ResponseEntity<InviteValidationResponseDto> validate(@Valid @RequestBody InviteValidationRequestDto request) {
        InviteValidationResponseDto response = invitationService.validate(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Einladung einlösen",
            description = "Löst eine gültige Einladung ein und legt den Benutzer an."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Einladung wurde erfolgreich eingelöst."),
            @ApiResponse(responseCode = "400", description = "Ungültige oder abgelaufene Einladung.")
    })
    /**
     * Löst eine Einladung ein.
     *
     * @param request Anfrage mit Einlöseinformationen
     * @return Antwort mit Ergebnis der Einlösung
     */
    @PostMapping("/redeem")
    public ResponseEntity<InviteRedeemResponseDto> redeem(@Valid @RequestBody InviteRedeemRequestDto request) {
        InviteRedeemResponseDto response = invitationService.redeem(request);
        return ResponseEntity.ok(response);
    }
}
