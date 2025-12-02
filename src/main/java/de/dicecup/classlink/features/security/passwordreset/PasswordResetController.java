package de.dicecup.classlink.features.security.passwordreset;

import de.dicecup.classlink.features.registration.PdfExportService;
import de.dicecup.classlink.features.registration.QrCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping("/validate")
    public ResponseEntity<PasswordResetValidateResponseDto> validate(@Valid @RequestBody PasswordResetValidateRequestDto request) {
        var response = passwordResetService.validate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/commit")
    public ResponseEntity<Void> commit(@Valid @RequestBody PasswordResetCommitRequestDto request) {
        passwordResetService.commit(request);
        return ResponseEntity.noContent().build();
    }

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
