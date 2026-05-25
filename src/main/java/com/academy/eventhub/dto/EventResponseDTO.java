package com.academy.eventhub.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati di un evento")
public class EventResponseDTO {

    // response: server -> client (solo qua le annotation swagger)
    // dell'evento restituisco solo: 
    // id, titolo, descrizione, ora di inizio e di fine, prezzo standard e vip, 
      // posti disponibili, organizzatore, location e gli argomenti dell'evento  

    @Schema(description = "ID evento", example = "1")
    private int id;

    @Schema(description = "Titolo", example = "Workshop Spring Boot")
    private String title;

    @Schema(description = "Descrizione")
    private String description;

    @Schema(description = "Data e ora di inizio", example = "2026-06-01T09:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Data e ora di fine", example = "2026-06-01T18:00:00")
    private LocalDateTime endDate;

    @Schema(description = "Prezzo standard", example = "49.99")
    private BigDecimal standardPrice;

    @Schema(description = "Prezzo VIP", example = "99.99")
    private BigDecimal vipPrice;

    @Schema(description = "Posti ancora disponibili", example = "42")
    private int availableSeats;

    @Schema(description = "Sede dell'evento")
    private VenueResponseDTO venue;

    @Schema(description = "Organizzatore dell'evento")
    private UserResponseDTO organizer;

    @Schema(description = "Tag dell'evento")
    private List<TagResponseDTO> tags;

    @Schema(description = "Relatori dell'evento")
    private List<SpeakerResponseDTO> speakers;
}