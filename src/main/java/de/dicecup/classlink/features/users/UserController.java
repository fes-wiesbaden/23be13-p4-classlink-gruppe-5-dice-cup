package de.dicecup.classlink.features.users;

import de.dicecup.classlink.features.users.domain.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @ApiResponse(responseCode = "200", description = "Liste erfolgreich abgerufen")
    /**
     * Gibt alle hinterlegten Benutzer zurück.
     *
     * @return ResponseEntity mit einer Liste von UserDto-Objekten
     */
    @GetMapping("/users")
    public List<UserDto> getUsers() {
        return userService.list();
    }

    @Operation(
            summary = "Benutzer nach ID abrufen",
            description = "Gibt einen Benutzer anhand der übergebenen ID zurück"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Benutzer erfolgreich geladen"),
            @ApiResponse(responseCode = "404", description = "Benutzer wurde nicht gefunden")
    })
    /**
     * Ruft einen einzelnen Benutzer anhand seiner ID ab.
     *
     * @param id Eindeutige ID des Benutzers
     * @return UserDto des gefundenen Benutzers
     */
    @GetMapping("/users/{id}")
    public UserDto getUser(@PathVariable UUID id) {
        return userService.get(id);
    }


    //TODO: implement update and delete endpoint expand DTO
    @Operation(
            summary = "Benutzer löschen",
            description = "Gibt einen Benutzer zur Löschung in zwei Wochen frei."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Benutzer erfolgreich geladen"),
            @ApiResponse(responseCode = "404", description = "Benutzer wurde nicht gefunden")
    })
    /**
     * Gibt einen Benutzer zur Löschung innerhalb von 2 Wochen frei
     *
     * @param id Eindeutige ID des Benutzers
     */
    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable UUID id) {
        userService.delete(id);
    }
}
