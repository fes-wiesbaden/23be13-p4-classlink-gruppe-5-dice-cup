package de.dicecup.classlink.features.registration;

import de.dicecup.classlink.features.registration.domain.CreateInviteRequestDto;
import de.dicecup.classlink.features.registration.domain.InviteCreatedResponseDto;
import de.dicecup.classlink.features.registration.domain.RegistrationInvite;
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
@RequestMapping("/admin/invites")
@RequiredArgsConstructor
public class AdminInvitationController {

    private final InvitationService invitationService;
    private final QrCodeService qrCodeService;
    private final PdfExportService pdfExportService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<InviteCreatedResponseDto> create(@Valid @RequestBody CreateInviteRequestDto request) {
        InviteCreatedResponseDto created = invitationService.createInvite(request);
        String qrUrl = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}/qrcode")
                .buildAndExpand(created.inviteId())
                .toUriString();
        InviteCreatedResponseDto body = new InviteCreatedResponseDto(
                created.inviteId(),
                created.token(),
                qrUrl,
                created.expiresAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/{id}/qrcode")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<byte[]> qrCode(@PathVariable UUID id,
                                         @RequestParam(name = "format", defaultValue = "png") String format) {
        RegistrationInvite invite = invitationService.getInvite(id);
        if (invite.getPublicToken() == null) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }
        String filename = "invite-" + id;
        if ("pdf".equalsIgnoreCase(format)) {
            byte[] png = qrCodeService.inviteQr(invite.getPublicToken());
            byte[] pdf = pdfExportService.exportSheet(
                    "Classlink Einladung",
                    List.of(new PdfExportService.QrCodeDescriptor(
                            invite.getEmail(),
                            png,
                            "Rolle: " + invite.getRole()))
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData(filename + ".pdf", filename + ".pdf");
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        }

        byte[] png = qrCodeService.inviteQr(invite.getPublicToken());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData(filename + ".png", filename + ".png");
        return new ResponseEntity<>(png, headers, HttpStatus.OK);
    }
}
