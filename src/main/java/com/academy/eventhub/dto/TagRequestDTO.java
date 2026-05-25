package com.academy.eventhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati per la creazione di un tag")
public class TagRequestDTO {

    // request: client -> server (qua non mi serve l'id)
    // dell'argomento prendo solo: nome

    @NotBlank(message = "Il nome del tag è obbligatorio")
    @Schema(description = "Nome del tag", example = "Java")
    private String name;
}