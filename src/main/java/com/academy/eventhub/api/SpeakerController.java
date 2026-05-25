package com.academy.eventhub.api;

import com.academy.eventhub.dto.SpeakerRequestDTO;
import com.academy.eventhub.dto.SpeakerResponseDTO;
import com.academy.eventhub.service.SpeakerService;
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
@Tag(name = "Speakers", description = "Catalogo relatori (lettura pubblica, scrittura solo ADMIN)")
public class SpeakerController {

    private final SpeakerService speakerService;

    // ─── Pubblici ─────────────────────────────────────────────────────────────

    @GetMapping("/speakers")
    @Operation(summary = "Lista tutti i relatori")
    @ApiResponse(responseCode = "200", description = "Lista relatori")
    public ResponseEntity<List<SpeakerResponseDTO>> getAllSpeakers() {
        return ResponseEntity.ok(speakerService.getAllSpeakers());
    }

    @GetMapping("/speakers/{id}")
    @Operation(summary = "Dettaglio relatore per ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatore trovato"),
            @ApiResponse(responseCode = "404", description = "Relatore non trovato")
    })
    public ResponseEntity<SpeakerResponseDTO> getSpeakerById(
            @Parameter(description = "ID relatore") @PathVariable int id) {
        return ResponseEntity.ok(speakerService.getSpeakerById(id));
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    @PostMapping("/admin/speakers")
    @Operation(summary = "Crea un nuovo relatore", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Relatore creato"),
            @ApiResponse(responseCode = "400", description = "Dati non validi")
    })
    public ResponseEntity<SpeakerResponseDTO> createSpeaker(@Valid @RequestBody SpeakerRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(speakerService.createSpeaker(dto));
    }

    @PutMapping("/admin/speakers/{id}")
    @Operation(summary = "Modifica un relatore esistente", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatore aggiornato"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "404", description = "Relatore non trovato")
    })
    public ResponseEntity<SpeakerResponseDTO> updateSpeaker(
            @Parameter(description = "ID relatore") @PathVariable int id,
            @Valid @RequestBody SpeakerRequestDTO dto) {
        return ResponseEntity.ok(speakerService.updateSpeaker(id, dto));
    }

    @DeleteMapping("/admin/speakers/{id}")
    @Operation(summary = "Elimina un relatore", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Relatore eliminato"),
            @ApiResponse(responseCode = "404", description = "Relatore non trovato")
    })
    public ResponseEntity<Void> deleteSpeaker(
            @Parameter(description = "ID relatore") @PathVariable int id) {
        speakerService.deleteSpeaker(id);
        return ResponseEntity.noContent().build();
    }
}
