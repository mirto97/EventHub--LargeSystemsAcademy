package com.academy.eventhub.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati per la creazione o modifica di un evento")
public class EventRequestDTO {

    // TODO commentare i dto, magari fare un sunto a parte, non riga per riga

    @NotBlank(message = "Il titolo è obbligatorio")
    @Schema(description = "Titolo dell'evento", example = "Workshop Spring Boot")
    private String title;

    @Schema(description = "Descrizione dell'evento")
    private String description;

    @NotNull(message = "La data di inizio è obbligatoria")
    @Schema(description = "Data e ora di inizio", example = "2026-06-01T09:00:00")
    private LocalDateTime startDate;

    @NotNull(message = "La data di fine è obbligatoria")
    @Schema(description = "Data e ora di fine", example = "2026-06-01T18:00:00")
    private LocalDateTime endDate;

    @NotNull(message = "Il prezzo standard è obbligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "Il prezzo non può essere negativo")
    @Schema(description = "Prezzo biglietto standard", example = "49.99")
    private BigDecimal standardPrice;

    @NotNull(message = "Il prezzo VIP è obbligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "Il prezzo non può essere negativo")
    @Schema(description = "Prezzo biglietto VIP", example = "99.99")
    private BigDecimal vipPrice;

    @NotNull(message = "La sede è obbligatoria")
    @Schema(description = "ID della sede", example = "1")
    private Integer venueId;

    @Schema(description = "Lista ID dei tag", example = "[1, 2]")
    private List<Integer> tagIds;

    @Schema(description = "Lista ID dei relatori", example = "[1, 2]")
    private List<Integer> speakerIds;


    @AssertTrue(message = "La data di fine deve essere successiva alla data di inizio")
    @Schema(hidden = true)  // altrimenti swagger lo considererebbe come un getter, glielo faccio ignorare
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) 
            return true;
        return endDate.isAfter(startDate);
    }
}