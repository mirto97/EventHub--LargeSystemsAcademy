package com.academy.eventhub.api;

import com.academy.eventhub.dto.SignupRequestDTO;
import com.academy.eventhub.dto.UserResponseDTO;
import com.academy.eventhub.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registrazione alla piattaforma")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(
            summary = "Registra un nuovo utente",
            description = "Crea un account con ruolo USER e status ACTIVE. Non richiede autenticazione."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utente registrato con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi (email malformata, password vuota)"),
            @ApiResponse(responseCode = "409", description = "Email già in uso")
    })
    public ResponseEntity<UserResponseDTO> signup(@Valid @RequestBody SignupRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(dto));
    }
}
