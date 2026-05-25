package com.academy.eventhub.api;

import com.academy.eventhub.dto.EventRequestDTO;
import com.academy.eventhub.dto.EventResponseDTO;
import com.academy.eventhub.dto.TicketResponseDTO;
import com.academy.eventhub.security.CustomUserDetails;
import com.academy.eventhub.service.EventService;
import com.academy.eventhub.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Catalogo eventi, CRUD organizer e lista partecipanti")
public class EventController {

    private final EventService eventService;
    private final TicketService ticketService;

    // ─── Pubblici ─────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(
            summary = "Lista eventi pubblici",
            description = """
                    Restituisce tutti gli eventi con posti disponibili calcolati.
                    Filtri opzionali combinabili: date (ISO-8601), tagId, venueId.
                    Esempio: /events?date=2025-06-01T00:00:00&tagId=3
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista eventi"),
            @ApiResponse(responseCode = "400", description = "Formato data non valido")
    })
    public ResponseEntity<List<EventResponseDTO>> getAllEvents(
            @Parameter(description = "Filtra eventi con startDate > questa data (es. 2025-06-01T00:00:00)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,

            @Parameter(description = "Filtra per ID tag")
            @RequestParam(required = false) Integer tagId,

            @Parameter(description = "Filtra per ID sede")
            @RequestParam(required = false) Integer venueId) {
        return ResponseEntity.ok(eventService.getAllEvents(date, tagId, venueId));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Dettaglio evento",
            description = "Include sede, organizer, relatori, tag, posti disponibili e valutazione media."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento trovato"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato")
    })
    public ResponseEntity<EventResponseDTO> getEventById(
            @Parameter(description = "ID evento") @PathVariable int id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    // ─── Organizer / Admin ────────────────────────────────────────────────────

    @GetMapping("/my")
    @Operation(summary = "I miei eventi", description = "Lista degli eventi creati dall'organizer autenticato. ORGANIZER o ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista eventi dell'organizer"),
            @ApiResponse(responseCode = "403", description = "Accesso negato")
    })
    public ResponseEntity<List<EventResponseDTO>> getMyEvents(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(eventService.getEventsByOrganizer(userDetails.getId()));
    }

    @PostMapping
    @Operation(summary = "Crea un nuovo evento", description = "ORGANIZER o ADMIN. La data di fine deve essere successiva alla data di inizio.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento creato"),
            @ApiResponse(responseCode = "400", description = "Dati non validi o date incoerenti"),
            @ApiResponse(responseCode = "404", description = "Venue, tag o speaker non trovati"),
            @ApiResponse(responseCode = "403", description = "Accesso negato")
    })
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody EventRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(dto, userDetails.getId()));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Modifica un evento",
            description = "ORGANIZER (solo i propri eventi) o ADMIN. Stesse validazioni della creazione."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento aggiornato"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "403", description = "Non sei il creatore dell'evento"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato")
    })
    public ResponseEntity<EventResponseDTO> updateEvent(
            @Parameter(description = "ID evento") @PathVariable int id,
            @Valid @RequestBody EventRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(eventService.updateEvent(id, dto, userDetails.getId()));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Elimina un evento",
            description = "ORGANIZER (solo i propri eventi) o ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evento eliminato"),
            @ApiResponse(responseCode = "403", description = "Non sei il creatore dell'evento"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato")
    })
    public ResponseEntity<Void> deleteEvent(
            @Parameter(description = "ID evento") @PathVariable int id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        eventService.deleteEvent(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/participants")
    @Operation(
            summary = "Lista partecipanti di un evento",
            description = "ORGANIZER (solo i propri eventi) o ADMIN. Restituisce i biglietti attivi."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista biglietti attivi"),
            @ApiResponse(responseCode = "403", description = "Non sei il creatore dell'evento"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato")
    })
    public ResponseEntity<List<TicketResponseDTO>> getEventParticipants(
            @Parameter(description = "ID evento") @PathVariable int id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ticketService.getEventParticipants(id, userDetails.getId()));
    }
}
