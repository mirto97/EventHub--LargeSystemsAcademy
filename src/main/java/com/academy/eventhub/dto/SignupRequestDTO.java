package com.academy.eventhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati per la registrazione di un nuovo utente")
public class SignupRequestDTO {

    @Email(message = "Email non valida")
    @NotBlank(message = "L'email è obbligatoria")
    @Schema(description = "Email dell'utente", example = "mario@example.com")
    private String email;

    @NotBlank(message = "La password è obbligatoria")
    @Size(min = 8, message = "La password deve essere di almeno 8 caratteri")
    @Schema(description = "Password dell'utente", example = "password123")
    private String password;
}