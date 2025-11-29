package de.dicecup.classlink.features.registration;

import de.dicecup.classlink.features.registration.domain.InviteRedeemRequestDto;
import de.dicecup.classlink.features.registration.domain.InviteRedeemResponseDto;
import de.dicecup.classlink.features.registration.domain.InviteValidationRequestDto;
import de.dicecup.classlink.features.registration.domain.InviteValidationResponseDto;
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


    @PostMapping("/validate")
    public ResponseEntity<InviteValidationResponseDto> validate(@Valid @RequestBody InviteValidationRequestDto request) {
        InviteValidationResponseDto response = invitationService.validate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/redeem")
    public ResponseEntity<InviteRedeemResponseDto> redeem(@Valid @RequestBody InviteRedeemRequestDto request) {
        InviteRedeemResponseDto response = invitationService.redeem(request);
        return ResponseEntity.ok(response);
    }
}
