package com.academy.eventhub.api;

import com.academy.eventhub.dto.TagRequestDTO;
import com.academy.eventhub.dto.TagResponseDTO;
import com.academy.eventhub.service.TagService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Catalogo categorie/argomenti (lettura pubblica, scrittura solo ADMIN)")
public class TagController {

    private final TagService tagService;

    // ─── Pubblico ─────────────────────────────────────────────────────────────

    @GetMapping("/tags")
    @Operation(summary = "Lista tutti i tag")
    @ApiResponse(responseCode = "200", description = "Lista tag")
    public ResponseEntity<List<TagResponseDTO>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    @PostMapping("/admin/tags")
    @Operation(summary = "Crea un nuovo tag", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tag creato"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "409", description = "Tag con lo stesso nome già esistente")
    })
    public ResponseEntity<TagResponseDTO> createTag(@Valid @RequestBody TagRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createTag(dto));
    }

    @DeleteMapping("/admin/tags/{id}")
    @Operation(summary = "Elimina un tag", description = "Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tag eliminato"),
            @ApiResponse(responseCode = "404", description = "Tag non trovato")
    })
    public ResponseEntity<Void> deleteTag(
            @Parameter(description = "ID tag") @PathVariable int id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
