package com.academy.eventhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati per la creazione o modifica di un relatore")
public class SpeakerRequestDTO {

    @NotBlank(message = "Il nome è obbligatorio")
    @Schema(description = "Nome", example = "Mario")
    private String firstName;

    @NotBlank(message = "Il cognome è obbligatorio")
    @Schema(description = "Cognome", example = "Rossi")
    private String lastName;

    @Schema(description = "Biografia", example = "Senior developer con 10 anni di esperienza")
    private String bio;

    @Schema(description = "URL foto", example = "https://example.com/foto.jpg")
    private String photoUrl;
}