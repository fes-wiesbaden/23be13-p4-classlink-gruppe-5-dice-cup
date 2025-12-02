package de.dicecup.classlink.features.registration.web;

import de.dicecup.classlink.features.registration.app.RegistrationService;
import de.dicecup.classlink.features.registration.domain.InviteRequestDto;
import de.dicecup.classlink.features.registration.domain.InviteResponseDto;
import de.dicecup.classlink.features.registration.domain.RegristrationRequesDto;
import de.dicecup.classlink.features.users.dto.UserDto;
import de.dicecup.classlink.features.users.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;
    private final UserMapper userMapper;

    @Operation(
            summary = "Benutzer Regristrierung",
            description = "Ein Nutzer nutzt den Invite-Link um sich zu regristrieren"
    )
    /**
     * Legt einen neuen Benutzer an
     * @return Neues User Objekt
     */
    @GetMapping("/register")
    public UserDto createUser(RegristrationRequesDto requestDto) { return registrationService.create(requestDto, userMapper);}

    @Operation(
            summary = "Benutzer Einladung",
            description = "Ein Benutzer wird anhand seiner Email regristriert und erhält ein One Time Token."
    )
    /**
     * Legt einen neuen Benutzer an
     * @return Neues User Objekt
     */
    @GetMapping("/invite")
    public InviteResponseDto sendInvite(InviteRequestDto requestDto) { return registrationService.invite(requestDto); }
}
