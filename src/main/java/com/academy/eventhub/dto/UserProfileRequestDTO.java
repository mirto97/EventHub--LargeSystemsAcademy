package com.academy.eventhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati per la creazione o aggiornamento del profilo utente")
public class UserProfileRequestDTO {

    // request: client -> server (qua non mi serve l'id)
    // del profilo dell'utente prendo solo:
    // nome, cognome, bio, citàà, link della foto

    @NotBlank(message = "Il nome è obbligatorio")
    @Schema(description = "Nome", example = "Mario")
    private String firstName;

    @NotBlank(message = "Il cognome è obbligatorio")
    @Schema(description = "Cognome", example = "Rossi")
    private String lastName;

    @Schema(description = "Biografia", example = "Appassionato di eventi tech")
    private String bio;

    @Schema(description = "Città", example = "Roma")
    private String city;

    @Schema(description = "URL foto profilo", example = "https://example.com/foto.jpg")
    private String photoUrl;
}