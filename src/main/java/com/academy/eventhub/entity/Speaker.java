package com.academy.eventhub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Speaker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Il nome è obbligatorio")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Il cognome è obbligatorio")
    @Column(nullable = false)
    private String lastName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String photoUrl;
}