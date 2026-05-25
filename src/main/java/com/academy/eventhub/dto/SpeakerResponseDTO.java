package com.academy.eventhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati di un relatore")
public class SpeakerResponseDTO {

    // response: server -> client (solo qua le annotation swagger)
    // del relatore espongo solo:
    // nome, cognome, bio, link foto

    @Schema(description = "ID relatore", example = "1")
    private int id;

    @Schema(description = "Nome", example = "Mario")
    private String firstName;

    @Schema(description = "Cognome", example = "Rossi")
    private String lastName;

    @Schema(description = "Biografia", example = "Senior developer con 10 anni di esperienza")
    private String bio;

    @Schema(description = "URL foto", example = "https://example.com/foto.jpg")
    private String photoUrl;
}