package com.academy.eventhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati di un feedback")
public class FeedbackResponseDTO {

    @Schema(description = "ID feedback", example = "1")
    private int id;

    @Schema(description = "Voto", example = "4")
    private int rating;

    @Schema(description = "Commento", example = "Evento molto interessante!")
    private String comment;

    @Schema(description = "Utente che ha lasciato il feedback")
    private UserResponseDTO user;

    @Schema(description = "ID evento recensito", example = "1")
    private int eventId;

    @Schema(description = "Titolo evento recensito", example = "Workshop Spring Boot")
    private String eventTitle;
}