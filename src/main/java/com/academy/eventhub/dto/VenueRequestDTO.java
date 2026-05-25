package com.academy.eventhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati per la creazione o modifica di una sede")
public class VenueRequestDTO {

    @NotBlank(message = "Il nome è obbligatorio")
    @Schema(description = "Nome della sede", example = "Centro Congressi Roma")
    private String name;

    @NotBlank(message = "L'indirizzo è obbligatorio")
    @Schema(description = "Indirizzo", example = "Via Roma 1, Roma")
    private String address;

    @NotNull(message = "La capienza è obbligatoria")
    @Min(value = 1, message = "La capienza deve essere almeno 1")
    @Schema(description = "Capienza massima", example = "200")
    private Integer capacity;
}