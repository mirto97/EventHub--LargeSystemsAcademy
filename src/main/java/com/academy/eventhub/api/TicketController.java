package com.academy.eventhub.api;

import com.academy.eventhub.dto.TicketRequestDTO;
import com.academy.eventhub.dto.TicketResponseDTO;
import com.academy.eventhub.security.CustomUserDetails;
import com.academy.eventhub.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Prenotazione e cancellazione biglietti")
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/tickets/my")
    @Operation(summary = "Le mie prenotazioni", description = "Lista di tutti i biglietti dell'utente autenticato.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista biglietti"),
            @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    public ResponseEntity<List<TicketResponseDTO>> getMyTickets(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ticketService.getUserTickets(userDetails.getId()));
    }

    @PostMapping("/events/{eventId}/book")
    @Operation(
            summary = "Prenota un biglietto",
            description = """
                    Prenota un biglietto (STANDARD o VIP) per l'evento specificato.
                    Il prezzo viene impostato automaticamente in base al tipo scelto.
                    Vincoli: evento futuro, nessuna doppia prenotazione, posti disponibili, utente non bannato.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Biglietto prenotato"),
            @ApiResponse(responseCode = "400", description = "Tipo biglietto non valido"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato"),
            @ApiResponse(responseCode = "409", description = "Evento passato, doppia prenotazione, posti esauriti o utente bannato")
    })
    public ResponseEntity<TicketResponseDTO> bookTicket(
            @Parameter(description = "ID evento") @PathVariable int eventId,
            @Valid @RequestBody TicketRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        dto.setEventId(eventId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketService.bookTicket(dto, userDetails.getId()));
    }

    @DeleteMapping("/tickets/{id}")
    @Operation(
            summary = "Cancella una prenotazione",
            description = "Cancellazione consentita solo prima dell'inizio dell'evento. Il biglietto viene marcato come CANCELLED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Prenotazione cancellata"),
            @ApiResponse(responseCode = "403", description = "Il biglietto non appartiene all'utente"),
            @ApiResponse(responseCode = "404", description = "Biglietto non trovato"),
            @ApiResponse(responseCode = "409", description = "Biglietto già cancellato o evento già iniziato")
    })
    public ResponseEntity<Void> cancelTicket(
            @Parameter(description = "ID biglietto") @PathVariable int id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ticketService.cancelTicket(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
