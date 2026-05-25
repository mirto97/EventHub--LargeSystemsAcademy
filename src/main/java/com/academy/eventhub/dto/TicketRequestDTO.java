package com.academy.eventhub.dto;

import com.academy.eventhub.entity.Ticket;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati per la prenotazione di un biglietto")
public class TicketRequestDTO {

    // request: client -> server (qua non mi serve l'id)
    // del biglietto prendo solo:
    // tipo e id dell'evento

    @NotNull(message = "Il tipo di biglietto è obbligatorio")
    @Schema(description = "Tipo di biglietto", example = "STANDARD")
    private Ticket.TicketType type;

    @NotNull(message = "L'evento è obbligatorio")
    @Schema(description = "ID dell'evento", example = "1")
    private Integer eventId;
}