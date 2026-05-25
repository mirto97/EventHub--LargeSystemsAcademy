package com.academy.eventhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati di un tag")
public class TagResponseDTO {

    @Schema(description = "ID tag", example = "1")
    private int id;

    @Schema(description = "Nome del tag", example = "Java")
    private String name;
}