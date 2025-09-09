package de.dicecup.classlink.features.users.web;

import de.dicecup.classlink.features.users.app.UserService;
import de.dicecup.classlink.features.users.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "Liste aller Benutzer",
            description = "Gibt eine vollständige Liste aller registrierten Benutzer zurück"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Liste erfolgreich abgerufen"
    )
    /**
     * Gibt alle hinterlegten Benutzer zurück
     * @return Liste von UserDTO Objekten
     */
    @GetMapping("/users")
    public List<UserDto> getUsers() {
        return userService.list();
    }

    @Operation(
            summary = "Suche Nutzer nach ID",
            description = "Gibt einen Benutzer anhand mitgegebener ID zurück"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Nutzer erfolgreich geladen"
    )
    /*
     * Gibt alle hinterlegten Benutzer zurück
     * @return Liste von UserDTO Objekten
     */
    @GetMapping("/users/{id}")
    public UserDto getUser(@PathVariable UUID id) {
        return userService.get(id);
    }

}
