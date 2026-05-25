package com.academy.eventhub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Il nome della sede è obbligatorio")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "L'indirizzo è obbligatorio")
    @Column(nullable = false)
    private String address;

    @Min(value = 1, message = "La capienza deve essere almeno 1")
    @Column(nullable = false)
    private int capacity;
}