package com.academy.eventhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati per lasciare un feedback su un evento")
public class FeedbackRequestDTO {

    @NotNull(message = "Il voto è obbligatorio")
    @Min(value = 1, message = "Il voto minimo è 1")
    @Max(value = 5, message = "Il voto massimo è 5")
    @Schema(description = "Voto da 1 a 5", example = "4")
    private Integer rating;

    @Schema(description = "Commento opzionale", example = "Evento molto interessante!")
    private String comment;

    @NotNull(message = "L'evento è obbligatorio")
    @Schema(description = "ID dell'evento", example = "1")
    private Integer eventId;
}