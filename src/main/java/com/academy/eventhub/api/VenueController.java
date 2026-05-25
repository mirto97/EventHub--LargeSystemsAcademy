package com.academy.eventhub.api;

import com.academy.eventhub.dto.VenueRequestDTO;
import com.academy.eventhub.dto.VenueResponseDTO;
import com.academy.eventhub.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Venues", description = "Catalogo sedi (lettura pubblica, scrittura solo ADMIN)")
public class VenueController {

    private final VenueService venueService;

    // ─── Pubblici ─────────────────────────────────────────────────────────────

    @GetMapping("/venues")
    @Operation(summary = "Lista tutte le sedi")
    @ApiResponse(responseCode = "200", description = "Lista sedi")
    public ResponseEntity<List<VenueResponseDTO>> getAllVenues() {
        return ResponseEntity.ok(venueService.getAllVenues());
    }

    @GetMapping("/venues/{id}")
    @Operation(summary = "Dettaglio sede per ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sede trovata"),
            @ApiResponse(responseCode = "404", description = "Sede non trovata")
    })
    public ResponseEntity<VenueResponseDTO> getVenueById(
            @Parameter(description = "ID sede") @PathVariable int id) {
        return ResponseEntity.ok(venueService.getVenueById(id));
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    @PostMapping("/admin/venues")
    @Operation(summary = "Crea una nuova sede", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sede creata"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "409", description = "Sede con lo stesso nome già esistente")
    })
    public ResponseEntity<VenueResponseDTO> createVenue(@Valid @RequestBody VenueRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.createVenue(dto));
    }

    @PutMapping("/admin/venues/{id}")
    @Operation(summary = "Modifica una sede esistente", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sede aggiornata"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "404", description = "Sede non trovata")
    })
    public ResponseEntity<VenueResponseDTO> updateVenue(
            @Parameter(description = "ID sede") @PathVariable int id,
            @Valid @RequestBody VenueRequestDTO dto) {
        return ResponseEntity.ok(venueService.updateVenue(id, dto));
    }

    @DeleteMapping("/admin/venues/{id}")
    @Operation(summary = "Elimina una sede", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sede eliminata"),
            @ApiResponse(responseCode = "404", description = "Sede non trovata")
    })
    public ResponseEntity<Void> deleteVenue(
            @Parameter(description = "ID sede") @PathVariable int id) {
        venueService.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }
}
