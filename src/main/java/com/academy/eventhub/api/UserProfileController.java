package com.academy.eventhub.api;

import com.academy.eventhub.dto.UserProfileRequestDTO;
import com.academy.eventhub.dto.UserProfileResponseDTO;
import com.academy.eventhub.security.CustomUserDetails;
import com.academy.eventhub.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Gestione profilo anagrafico dell'utente autenticato")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    @Operation(summary = "Leggi il tuo profilo", description = "Restituisce i dati anagrafici dell'utente autenticato.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profilo trovato"),
            @ApiResponse(responseCode = "404", description = "Profilo non ancora creato"),
            @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    public ResponseEntity<UserProfileResponseDTO> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userProfileService.getProfileByUserId(userDetails.getId()));
    }

    @PostMapping
    @Operation(
            summary = "Crea il tuo profilo",
            description = "Crea il profilo anagrafico per l'utente autenticato. Può essere fatto una sola volta."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profilo creato"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "409", description = "Profilo già esistente"),
            @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    public ResponseEntity<UserProfileResponseDTO> createMyProfile(
            @Valid @RequestBody UserProfileRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userProfileService.createProfile(userDetails.getId(), dto));
    }

    @PutMapping
    @Operation(summary = "Aggiorna il tuo profilo", description = "Modifica i dati anagrafici dell'utente autenticato.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profilo aggiornato"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "404", description = "Profilo non trovato"),
            @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    public ResponseEntity<UserProfileResponseDTO> updateMyProfile(
            @Valid @RequestBody UserProfileRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userProfileService.updateProfile(userDetails.getId(), dto));
    }
}
