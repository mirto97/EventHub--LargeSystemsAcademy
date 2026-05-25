package com.academy.eventhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati del profilo utente")
public class UserProfileResponseDTO {

    // response: server -> client (solo qua le annotation swagger)
    // del profilo dell'utente espongo solo:
    // nome, cognome, bio, città, link foto

    @Schema(description = "ID profilo", example = "1")
    private int id;

    @Schema(description = "Nome", example = "Mario")
    private String firstName;

    @Schema(description = "Cognome", example = "Rossi")
    private String lastName;

    @Schema(description = "Biografia", example = "Appassionato di eventi tech")
    private String bio;

    @Schema(description = "Città", example = "Roma")
    private String city;

    @Schema(description = "URL foto profilo", example = "https://example.com/foto.jpg")
    private String photoUrl;
}