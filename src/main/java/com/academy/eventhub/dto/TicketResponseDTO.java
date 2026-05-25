package com.academy.eventhub.dto;

import com.academy.eventhub.entity.Ticket;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati di un biglietto prenotato")
public class TicketResponseDTO {

    // response: server -> client (solo qua le annotation swagger)
    // del biglietto espongo solo:
    // tipo, prezzo, stato, id e titolo dell'evento

    @Schema(description = "ID biglietto", example = "1")
    private int id;

    @Schema(description = "Tipo di biglietto", example = "STANDARD")
    private Ticket.TicketType type;

    @Schema(description = "Prezzo pagato", example = "49.99")
    private BigDecimal price;

    @Schema(description = "Stato del biglietto", example = "ACTIVE")
    private Ticket.TicketStatus status;

    @Schema(description = "ID evento", example = "1")
    private int eventId;

    @Schema(description = "Titolo evento", example = "Workshop Spring Boot")
    private String eventTitle;
}