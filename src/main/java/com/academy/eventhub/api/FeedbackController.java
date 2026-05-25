package com.academy.eventhub.api;

import com.academy.eventhub.dto.FeedbackRequestDTO;
import com.academy.eventhub.dto.FeedbackResponseDTO;
import com.academy.eventhub.security.CustomUserDetails;
import com.academy.eventhub.service.FeedbackService;
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
@Tag(name = "Feedbacks", description = "Recensioni post-evento")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping("/events/{eventId}/feedbacks")
    @Operation(summary = "Feedback di un evento", description = "Pubblica. Restituisce tutti i feedback lasciati per l'evento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista feedback"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato")
    })
    public ResponseEntity<List<FeedbackResponseDTO>> getEventFeedbacks(
            @Parameter(description = "ID evento") @PathVariable int eventId) {
        return ResponseEntity.ok(feedbackService.getEventFeedbacks(eventId));
    }

    @GetMapping("/events/{eventId}/rating")
    @Operation(summary = "Valutazione media di un evento", description = "Pubblica. Restituisce la media dei voti (1-5).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Media calcolata (null se nessun feedback presente)"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato")
    })
    public ResponseEntity<Double> getEventRating(
            @Parameter(description = "ID evento") @PathVariable int eventId) {
        return ResponseEntity.ok(feedbackService.getEventRating(eventId));
    }

    @PostMapping("/feedbacks")
    @Operation(
            summary = "Lascia un feedback",
            description = """
                    Permette all'utente autenticato di recensire un evento.
                    Vincoli: evento concluso, biglietto attivo presente, nessun feedback duplicato.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Feedback registrato"),
            @ApiResponse(responseCode = "400", description = "Voto non nel range 1-5"),
            @ApiResponse(responseCode = "404", description = "Evento non trovato"),
            @ApiResponse(responseCode = "409", description = "Evento non concluso, nessun biglietto valido o feedback già presente")
    })
    public ResponseEntity<FeedbackResponseDTO> leaveFeedback(
            @Valid @RequestBody FeedbackRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedbackService.leaveFeedback(dto, userDetails.getId()));
    }

    @DeleteMapping("/admin/feedbacks/{id}")
    @Operation(summary = "Elimina un feedback", description = "Solo ADMIN. Moderazione contenuti inappropriati.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Feedback eliminato"),
            @ApiResponse(responseCode = "404", description = "Feedback non trovato"),
            @ApiResponse(responseCode = "403", description = "Accesso negato")
    })
    public ResponseEntity<Void> deleteFeedback(
            @Parameter(description = "ID feedback") @PathVariable int id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }
}
