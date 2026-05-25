package com.academy.eventhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati di una sede")
public class VenueResponseDTO {

    // response: server -> client (solo qua le annotation swagger)
    // della location restituisco solo: 
    // nome, indirizzo e capacità massima

    @Schema(description = "ID sede", example = "1")
    private int id;

    @Schema(description = "Nome della sede", example = "Centro Congressi Roma")
    private String name;

    @Schema(description = "Indirizzo", example = "Via Roma 1, Roma")
    private String address;

    @Schema(description = "Capienza massima", example = "200")
    private int capacity;
}