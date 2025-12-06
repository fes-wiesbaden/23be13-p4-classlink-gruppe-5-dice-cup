package de.dicecup.classlink.features.security.passwordreset;

import de.dicecup.classlink.features.registration.PdfExportService;
import de.dicecup.classlink.features.registration.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final QrCodeService qrCodeService;
    private final PdfExportService pdfExportService;

    @Operation(
            summary = "Passwort-Reset-Token erstellen",
            description = "Erstellt ein neues Passwort-Reset-Token und liefert die Informationen zurück."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Token wurde erfolgreich erstellt."),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten."),
            @ApiResponse(responseCode = "403", description = "Zugriff verweigert.")
    })
    /**
     * Erstellt ein Passwort-Reset-Token für einen Benutzer.
     *
     * @param request Anfrage mit Benutzerdaten für den Reset
     * @return ResponseEntity mit dem erstellten Token und QR-Code-Header
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PasswordResetCreateResponseDto> create(@Valid @RequestBody PasswordResetCreateRequestDto request) {
        PasswordResetCreateResponseDto created = passwordResetService.createResetToken(request);
        String qrUrl = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}/qrcode")
                .buildAndExpand(created.tokenId())
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-QR-Code-Url", qrUrl);
        return new ResponseEntity<>(created, headers, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Passwort-Reset-Token validieren",
            description = "Prüft, ob ein Passwort-Reset-Token gültig ist."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token ist gültig."),
            @ApiResponse(responseCode = "400", description = "Token ist ungültig oder abgelaufen.")
    })
    /**
     * Validiert ein Passwort-Reset-Token.
     *
     * @param request Anfrage mit Token-Daten
     * @return Validierungsantwort
     */
    @PostMapping("/validate")
    public ResponseEntity<PasswordResetValidateResponseDto> validate(@Valid @RequestBody PasswordResetValidateRequestDto request) {
        var response = passwordResetService.validate(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Passwort-Reset durchführen",
            description = "Bestätigt einen gültigen Passwort-Reset-Token und setzt das Passwort."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Passwort wurde erfolgreich zurückgesetzt."),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten oder Token ungültig.")
    })
    /**
     * Schließt den Passwort-Reset ab.
     *
     * @param request Anfrage mit neuem Passwort und Token
     * @return ResponseEntity ohne Inhalt bei Erfolg
     */
    @PostMapping("/commit")
    public ResponseEntity<Void> commit(@Valid @RequestBody PasswordResetCommitRequestDto request) {
        passwordResetService.commit(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "QR-Code für Passwort-Reset abrufen",
            description = "Liefert den QR-Code für ein Passwort-Reset-Token als PNG oder PDF."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "QR-Code wurde erfolgreich erzeugt."),
            @ApiResponse(responseCode = "403", description = "Zugriff verweigert."),
            @ApiResponse(responseCode = "410", description = "Das Token ist nicht mehr gültig.")
    })
    /**
     * Gibt den QR-Code eines Passwort-Reset-Tokens zurück.
     *
     * @param id     ID des Tokens
     * @param format Ausgabeformat (png oder pdf)
     * @return QR-Code als Byte-Array
     */
    @GetMapping("/{id}/qrcode")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> qrCode(@PathVariable UUID id,
                                         @RequestParam(name = "format", defaultValue = "png") String format) {
        PasswordResetToken token = passwordResetService.getToken(id);
        if (token.getPublicToken() == null || token.getStatus() != PasswordResetTokenStatus.PENDING || token.isExpired()) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }
        String filename = "password-reset-" + id;
        if ("pdf".equalsIgnoreCase(format)) {
            byte[] png = qrCodeService.passwordResetQr(token.getPublicToken());
            byte[] pdf = pdfExportService.exportSheet(
                    "Passwort Reset",
                    List.of(new PdfExportService.QrCodeDescriptor(
                            "Reset für " + token.getUserId(),
                            png,
                            "Gültig bis: " + token.getExpiresAt()))
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData(filename + ".pdf", filename + ".pdf");
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        }
        byte[] png = qrCodeService.passwordResetQr(token.getPublicToken());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData(filename + ".png", filename + ".png");
        return new ResponseEntity<>(png, headers, HttpStatus.OK);
    }
}
