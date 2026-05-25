package com.academy.eventhub.api;

import com.academy.eventhub.dto.UserResponseDTO;
import com.academy.eventhub.security.CustomUserDetails;
import com.academy.eventhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Users", description = "Area personale e gestione utenti (ADMIN)")
public class UserController {

    private final UserService userService;

    // ─── Area personale ───────────────────────────────────────────────────────

    @GetMapping("/me")
    @Operation(summary = "Dati utente corrente", description = "Restituisce email, ruolo e status dell'utente autenticato.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dati restituiti"),
            @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    public ResponseEntity<UserResponseDTO> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserById(userDetails.getId()));
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    @GetMapping("/admin/users")
    @Operation(summary = "Lista tutti gli utenti", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista utenti"),
            @ApiResponse(responseCode = "403", description = "Accesso negato")
    })
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/admin/users/{id}")
    @Operation(summary = "Dettaglio utente per ID", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utente trovato"),
            @ApiResponse(responseCode = "404", description = "Utente non trovato"),
            @ApiResponse(responseCode = "403", description = "Accesso negato")
    })
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "ID utente") @PathVariable int id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/admin/users/{id}/promote")
    @Operation(
            summary = "Promuove un utente a ORGANIZER",
            description = "Solo ADMIN. Applicabile solo a utenti con ruolo USER."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ruolo aggiornato"),
            @ApiResponse(responseCode = "404", description = "Utente non trovato"),
            @ApiResponse(responseCode = "409", description = "Utente non è USER o è ADMIN")
    })
    public ResponseEntity<UserResponseDTO> promoteUser(
            @Parameter(description = "ID utente da promuovere") @PathVariable int id) {
        return ResponseEntity.ok(userService.promoteUser(id));
    }

    @PutMapping("/admin/users/{id}/ban")
    @Operation(
            summary = "Banna un utente",
            description = "Solo ADMIN. Non applicabile agli ADMIN. Cancella automaticamente i biglietti futuri dell'utente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utente bannato"),
            @ApiResponse(responseCode = "404", description = "Utente non trovato"),
            @ApiResponse(responseCode = "409", description = "Operazione non consentita (es. target è ADMIN)")
    })
    public ResponseEntity<UserResponseDTO> banUser(
            @Parameter(description = "ID utente da bannare") @PathVariable int id) {
        return ResponseEntity.ok(userService.banUser(id));
    }

    @PutMapping("/admin/users/{id}/reactivate")
    @Operation(summary = "Riattiva un utente bannato", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utente riattivato"),
            @ApiResponse(responseCode = "404", description = "Utente non trovato")
    })
    public ResponseEntity<UserResponseDTO> reactivateUser(
            @Parameter(description = "ID utente da riattivare") @PathVariable int id) {
        return ResponseEntity.ok(userService.reactivateUser(id));
    }
}
