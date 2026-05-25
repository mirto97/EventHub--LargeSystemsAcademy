package com.academy.eventhub.dto;

import com.academy.eventhub.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(description = "Dati pubblici di un utente")
public class UserResponseDTO {

    @Schema(description = "ID utente", example = "1")
    private int id;

    @Schema(description = "Email utente", example = "mario@example.com")
    private String email;

    @Schema(description = "Ruolo utente", example = "USER")
    private User.Role role;

    @Schema(description = "Stato utente", example = "ACTIVE")
    private User.Status status;
}